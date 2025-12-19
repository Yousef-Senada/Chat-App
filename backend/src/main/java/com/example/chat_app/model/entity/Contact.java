package com.example.chat_app.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "contacts",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"owner_user_id", "contact_user_id"})
        }
)

public class Contact {
    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "contact_user_id", nullable = false)
    private User contactUser;

    @Column(name = "saved_name", length = 100)
    private String displayName;
}
