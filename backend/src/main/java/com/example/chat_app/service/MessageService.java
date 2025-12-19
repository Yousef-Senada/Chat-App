package com.example.chat_app.service;

import com.example.chat_app.exceptions.AppException;
import com.example.chat_app.factory.MessageProcessor;
import com.example.chat_app.factory.MessageProcessorFactory;
import com.example.chat_app.model.dto.MessageDisplayDto;
import com.example.chat_app.model.dto.SendMessageRequest;
import com.example.chat_app.model.dto.SenderDto;
import com.example.chat_app.model.dto.UpdateMessageRequest;
import com.example.chat_app.model.entity.*;
import com.example.chat_app.repository.ChatRepository;
import com.example.chat_app.repository.MemberRepository;
import com.example.chat_app.repository.MessageRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class MessageService {

    private MessageRepository messageRepo;
    private ChatRepository chatRepo;
    private MemberRepository memberRepo;
    private SimpMessagingTemplate messagingTemplate;
    private MessageProcessorFactory messageFactory;

    public MessageService(MessageRepository messageRepo, ChatRepository chatRepo, MemberRepository memberRepo,
            SimpMessagingTemplate messagingTemplate, MessageProcessorFactory messageFactory) {
        this.messageRepo = messageRepo;
        this.chatRepo = chatRepo;
        this.memberRepo = memberRepo;
        this.messagingTemplate = messagingTemplate;
        this.messageFactory = messageFactory;
    }

    private MessageDisplayDto convertToDisplayDto(Message message) {
        SenderDto senderDto = new SenderDto(message.getSender().getId(), message.getSender().getUsername());

        return MessageDisplayDto.builder()
                .messageId(message.getId())
                .sender(senderDto)
                .messageType(message.getType().name())
                .content(message.isDeleted() ? "Message has been deleted" : message.getContent())
                .mediaUrl(message.getMediaUrl())
                .timestamp(message.getSentAt())
                .isEdited(message.isEdited())
                .isDeleted(message.isDeleted())
                .build();
    }

    @Transactional
    public void sendMessage(User sender, SendMessageRequest request) {
        Chat chat = chatRepo.findById(request.chatId())
                .orElseThrow(() -> new AppException("Chat not found.", HttpStatus.NOT_FOUND));

        Message message = new Message();
        message.setSender(sender);
        message.setChat(chat);
        message.setType(Enums.valueOf(request.messageType().toUpperCase()));
        message.setSentAt(LocalDateTime.now());

        MessageProcessor processor = messageFactory.getProcessor(request.messageType());
        processor.process(request, message);

        Message savedMessage = messageRepo.save(message);

        MessageDisplayDto messageDto = convertToDisplayDto(savedMessage);
        messagingTemplate.convertAndSend("/topic/chat/" + chat.getId(), messageDto);
    }

    public Page<MessageDisplayDto> getMessages(UUID chatId, User requester, int page, int size) {

        boolean isMember = memberRepo.findByChatIdAndUserId(chatId, requester.getId()).isPresent();

        if (!isMember) {
            throw new AppException("User is not a member of this chat and cannot view messages.", HttpStatus.FORBIDDEN);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAt").descending());

        Page<Message> messagePage = messageRepo.findAllByChatIdOrderBySentAtDesc(chatId, pageable);

        return messagePage.map(this::convertToDisplayDto);
    }

    @Transactional
    public Message editMessage(User editor, UpdateMessageRequest request) {

        Message message = messageRepo.findById(request.messageId())
                .orElseThrow(() -> new AppException("Message not found.", HttpStatus.NOT_FOUND));

        if (!message.getSender().getId().equals(editor.getId())) {
            throw new AppException("User is not authorized to edit this message.", HttpStatus.UNAUTHORIZED);
        }

        if (!Enums.TEXT.equals(message.getType())) {
            throw new AppException("Only text messages can be edited.", HttpStatus.BAD_REQUEST);
        }

        if (request.newContent() == null || request.newContent().trim().isEmpty()) {
            throw new AppException("New message content cannot be empty.", HttpStatus.BAD_REQUEST);
        }

        message.setContent(request.newContent());

        message.setEdited(true);

        Message updatedMessage = messageRepo.save(message);

        MessageDisplayDto messageDto = convertToDisplayDto(updatedMessage);
        messagingTemplate.convertAndSend("/topic/chat/" + updatedMessage.getChat().getId(), messageDto);

        return updatedMessage;
    }

    @Transactional
    public void deleteMessage(User deleter, UUID messageId) {
        Message message = messageRepo.findById(messageId)
                .orElseThrow(() -> new AppException("Message not found.", HttpStatus.NOT_FOUND));

        boolean isSender = message.getSender().getId().equals(deleter.getId());

        boolean isAdmin = memberRepo.findByChatIdAndUserId(message.getChat().getId(), deleter.getId())
                .map(member -> Enums.ADMIN.equals(member.getRole()))
                .orElse(false);

        if (!isSender && !isAdmin) {
            throw new AppException("User is not authorized to delete this message.", HttpStatus.UNAUTHORIZED);
        }

        message.setDeleted(true);

        Message savedMessage = messageRepo.save(message);

        MessageDisplayDto messageDto = convertToDisplayDto(savedMessage);
        messagingTemplate.convertAndSend("/topic/chat/" + savedMessage.getChat().getId(), messageDto);
    }
}
