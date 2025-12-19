package com.example.chat_app.model.entity;


import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "call_logs")
public class Call_log {
    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "caller_id", nullable = false)
    private User caller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Chat conversation;

    @Enumerated(EnumType.STRING)
    private Enums call_type;

    @Enumerated(EnumType.STRING)
    private Enums call_status;

    @CreationTimestamp
    @Column(name = "start_time", updatable = false, nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    private long duration_seconds;
}
