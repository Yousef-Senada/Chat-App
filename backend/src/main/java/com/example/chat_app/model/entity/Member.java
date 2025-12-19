package com.example.chat_app.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(
        name = "members",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"chat_id", "user_id"})
        }
)

public class Member {
    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private Enums role;

    @CreationTimestamp
    @Column(name = "joined_at", updatable = false, nullable = false)
    private LocalDateTime joindAt;
}
