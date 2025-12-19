package com.example.chat_app.repository;

import com.example.chat_app.model.entity.Member;
import com.example.chat_app.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemberRepository extends JpaRepository<Member, UUID> {
    List<Member> findAllByUser(User user);
    List<Member> findAllByChatId(UUID chatId);
    List<Member> findByChatIdAndUserIdIn(UUID chatId, List<UUID> userIds);
    Optional<Member> findByChatIdAndUserId(UUID chatId, UUID userId);
    long countByChatId(UUID chatId);
}
