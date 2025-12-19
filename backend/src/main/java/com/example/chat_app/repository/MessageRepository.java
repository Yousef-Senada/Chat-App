package com.example.chat_app.repository;

import com.example.chat_app.model.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
    Page<Message> findAllByChatIdOrderBySentAtDesc(UUID chatId, Pageable pageable);

    Optional<Message> findById(UUID id);
}
