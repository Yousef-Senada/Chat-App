package com.example.chat_app.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.example.chat_app.exceptions.AppException;
import com.example.chat_app.model.dto.*;

import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.chat_app.model.entity.Chat;
import com.example.chat_app.model.entity.Enums;
import com.example.chat_app.model.entity.Member;
import com.example.chat_app.model.entity.User;
import com.example.chat_app.repository.ChatRepository;
import com.example.chat_app.repository.UserRepository;
import com.example.chat_app.repository.MemberRepository;


@Service
public class ChatService {
    private ChatRepository chatRepo;
    private UserRepository userRepo;
    private MemberRepository memberRepo;
    private SimpMessagingTemplate messagingTemplate;


    public ChatService(ChatRepository chatRepo, UserRepository userRepo, MemberRepository memberRepo, SimpMessagingTemplate messagingTemplate) {
        this.chatRepo = chatRepo;
        this.userRepo = userRepo;
        this.memberRepo = memberRepo;
        this.messagingTemplate = messagingTemplate;
    }

    private MemberDisplayDto convertToMemberDisplayDto(Member member) {
        return new MemberDisplayDto(
            member.getUser().getId(),
            member.getUser().getUsername(),
            member.getRole().name()
        );
    }

    private ChatDisplayDto convertToDisplayDto(Chat chat, List<Member> members) {
    
    List<MemberDisplayDto> memberDtos = members.stream()
        .map(this::convertToMemberDisplayDto)
        .collect(Collectors.toList());
    
        return new ChatDisplayDto(
            chat.getId(),
            chat.getChatType().name(),
            chat.getGroupName(),
            chat.getGroupImage(),
            memberDtos
        );
    }


    @Transactional
    public ChatDisplayDto createChat(User owner, CreateChatRequest request){
        
        List<User> usersToAdd = userRepo.findAllById(request.membersId());

        if(usersToAdd.size() != request.membersId().size()) {
            throw new AppException("Some users were not found", HttpStatus.BAD_REQUEST);
        }

        if(usersToAdd.stream().noneMatch(user -> user.getId().equals(owner.getId()))) {
            usersToAdd.add(owner);
        }

        if("P2P".equals(request.chatType())) {
            if(usersToAdd.size() != 2 ) {
                throw new AppException("P2P chat must have exactly 2 users", HttpStatus.BAD_REQUEST);
            }
        }
        else if("GROUP".equals(request.chatType())) {
            if(request.groupName() == null || request.groupName().trim().isEmpty()) {
                throw new AppException("Group name is required", HttpStatus.BAD_REQUEST);
            }
            
            if(usersToAdd.size() < 3 ) {
                throw new AppException("Group chat must have at least 3 users", HttpStatus.BAD_REQUEST);
            }
        }
        else {
            throw new AppException("Invalid chat type", HttpStatus.BAD_REQUEST);
        }

        Chat newChat = new Chat();
        Enums type = Enums.valueOf(request.chatType().toUpperCase());
        newChat.setChatType(type);
        newChat.setGroupName(request.groupName());
        newChat.setGroupImage(request.groupImage());
        Chat savedChat = chatRepo.save(newChat);

        List<Member> members = usersToAdd.stream()
            .map(user -> {
                Member member = new Member();
                member.setChat(savedChat);
                member.setUser(user);
                
                if(user.getId().equals(owner.getId())) {
                    member.setRole(Enums.ADMIN);
                }
                else {
                    member.setRole(Enums.MEMBER);
                }
                return member;
            })
            .collect(Collectors.toList());

        memberRepo.saveAll(members);

        ChatDisplayDto chatDto = convertToDisplayDto(savedChat, members);

        usersToAdd.forEach(user -> {
            messagingTemplate.convertAndSendToUser(
                user.getUsername(),
                "/queue/newChat",
                chatDto
            );
        });
            
        return chatDto;    
    }


  public List<ChatDisplayDto> getUserChats(User owner) {
    
    List<Member> members = memberRepo.findAllByUser(owner);
    List<Chat> chats = members.stream()
            .map(Member::getChat)
            .filter(chat -> chat.getDeletedAt() == null)
            .collect(Collectors.toList());

    return chats.stream()   
            .map(chat -> {
                List<Member> chatMembers = memberRepo.findAllByChatId(chat.getId()); 
                return convertToDisplayDto(chat, chatMembers);
            })
            .collect(Collectors.toList());
    }


    public List<MemberDisplayDto> getChatMembers(UUID chatId, User requester) {

        boolean isMember = memberRepo.findByChatIdAndUserId(chatId, requester.getId()).isPresent();
        if (!isMember) {
            throw new AppException("User is not authorized to view the members of this chat.", HttpStatus.FORBIDDEN);
        }

        List<Member> members = memberRepo.findAllByChatId(chatId);

        return members.stream()
            .map(this::convertToMemberDisplayDto)
            .collect(Collectors.toList());
    }


    public void authorizeGroupAdmin(UUID chatId, User user) {
        Member member = memberRepo.findByChatIdAndUserId(chatId, user.getId())
            .orElseThrow(() -> new AppException("User is not a member of the chat", HttpStatus.FORBIDDEN));

        Enums role = member.getRole();

        if(!Enums.ADMIN.equals(role)) {
            throw new AppException("User does not have administrative privileges for this group.", HttpStatus.FORBIDDEN);
        }
    }


    @Transactional
    public ChatDisplayDto addMember(User owner, UpdateMembershipRequest request) {
        authorizeGroupAdmin(request.chatId(), owner);

        Chat chat = chatRepo.findById(request.chatId())
                .orElseThrow(() -> new AppException("chat not found", HttpStatus.NOT_FOUND));

        List<User> usersToAdd = userRepo.findAllById(request.memberUserIds());

        if(usersToAdd.size() != request.memberUserIds().size()) {
            throw new AppException("One or more user IDs to add are invalid.", HttpStatus.BAD_REQUEST);
        }

        List<UUID> existingMembersIds = memberRepo.findAllByChatId(request.chatId())
                .stream()
                .map(member -> member.getUser().getId())
                .collect(Collectors.toList());

        List<Member> newMembers = usersToAdd.stream()
                .filter(user -> !existingMembersIds.contains(user.getId()))
                .map(user -> {
                    Member member = new Member();
                    member.setChat(chat);
                    member.setUser(user);
                    member.setRole(Enums.MEMBER);
                    return member;
                })
                .collect(Collectors.toList());

        memberRepo.saveAll(newMembers);

        List<MemberDisplayDto> addedMembersDto = newMembers.stream()
            .map(this::convertToMemberDisplayDto) 
            .collect(Collectors.toList());

        MemberUpdateDto updateDto = new MemberUpdateDto(
            request.chatId(),
            addedMembersDto,
            "MEMBER_ADDED"
        );

        messagingTemplate.convertAndSend("/topic/chat/" + chat.getId() + "/members", updateDto);

        List<Member> allMembers = memberRepo.findAllByChatId(chat.getId());
        return convertToDisplayDto(chat, allMembers);
    }


    @Transactional
    public ChatDisplayDto updateGroupProperties(User owner, UpdateGroupPropertiesRequest request) {
        
        authorizeGroupAdmin(request.chatId(), owner);

        Chat chat = chatRepo.findById(request.chatId())
            .orElseThrow(() -> new AppException("Chat not found", HttpStatus.NOT_FOUND));

    
        if(request.newGroupName() != null && !request.newGroupName().trim().isEmpty()) {
            chat.setGroupName(request.newGroupName());
        }

        if(request.newGroupImageUrl() != null) {
            chat.setGroupImage(request.newGroupImageUrl());
        }

        Chat savedChat = chatRepo.save(chat);

        List<Member> members = memberRepo.findAllByChatId(savedChat.getId());
        ChatDisplayDto chatDto = convertToDisplayDto(savedChat, members);

        messagingTemplate.convertAndSend("/topic/chat/" + savedChat.getId() + "/updates", chatDto);

        return chatDto;
    }


    @Transactional
    public MemberDisplayDto updateMemberRole(User owner, UpdateMemberRoleRequest request) {

        authorizeGroupAdmin(request.chatId(), owner);


        Member targetMember = memberRepo.findByChatIdAndUserId(request.chatId(), request.targetUserId())
                .orElseThrow(() -> new AppException("Target user is not a member of this chat.", HttpStatus.FORBIDDEN));

        if (Enums.ADMIN.equals(targetMember.getRole()) && !owner.getId().equals(targetMember.getUser().getId())) {
            throw new AppException("Cannot modify the role of an existing group ADMIN.", HttpStatus.FORBIDDEN);
        }

        String newRoleUpper = request.newRole().toUpperCase();

        if (!"ADMIN".equalsIgnoreCase(newRoleUpper) && !"MEMBER".equalsIgnoreCase(newRoleUpper)) {
                throw new AppException("Invalid role provided. Role must be ADMIN or MEMBER.", HttpStatus.BAD_REQUEST);
        }

        Enums newRoleEnum = Enums.valueOf(newRoleUpper);
        targetMember.setRole(newRoleEnum);

        Member updatedMember = memberRepo.save(targetMember);

        MemberDisplayDto memberDto = convertToMemberDisplayDto(updatedMember);
    
        MemberUpdateDto updateDto = new MemberUpdateDto(
        request.chatId(),
        List.of(memberDto), 
        "ROLE_UPDATED"
        );
    
        messagingTemplate.convertAndSend("/topic/chat/" + request.chatId() + "/members", updateDto);

        return memberDto;

    }


    public void deleteMember(User owner, UpdateMembershipRequest request) {
            List<Member> membersToRemove = memberRepo.findByChatIdAndUserIdIn(request.chatId(), request.memberUserIds());

        if (membersToRemove.isEmpty()) {
            throw new AppException("No matching members found to remove from the chat.", HttpStatus.NOT_FOUND);
        }

        for(Member targetMember : membersToRemove) {

            if(targetMember.getUser().getId().equals(owner.getId()) && Enums.ADMIN.equals(targetMember.getRole())) {
                throw new AppException("Group owner cannot remove themselves using this function.", HttpStatus.FORBIDDEN);
            }

            boolean isAdmin = memberRepo.findByChatIdAndUserId(request.chatId(), owner.getId())
                    .map(m -> Enums.ADMIN.equals(m.getRole()))
                    .orElse(false);

            boolean isRemovable = targetMember.getUser().getId().equals(owner.getId());

            if (!isAdmin && !isRemovable) {
                throw new AppException("User does not have permission to remove one of the specified members.", HttpStatus.FORBIDDEN);
            }
        }

        memberRepo.deleteAll(membersToRemove);

        List<UUID> removedUserIds = membersToRemove.stream()
            .map(m -> m.getUser().getId())
            .collect(Collectors.toList());

        removedUserIds.forEach(userId -> {
            String username = userRepo.getReferenceById(userId).getUsername();
            messagingTemplate.convertAndSendToUser(
                username,
                "/queue/chatRemoved", 
                request.chatId() 
            );
        });
    
        MemberUpdateDto updateDto = new MemberUpdateDto(
        request.chatId(),
        null, 
        "MEMBER_REMOVED"
        );
        
        messagingTemplate.convertAndSend("/topic/chat/" + request.chatId() + "/members", updateDto);
    }
}
