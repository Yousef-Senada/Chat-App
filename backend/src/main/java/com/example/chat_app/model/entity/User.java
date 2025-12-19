package com.example.chat_app.model.entity;

import jakarta.persistence.*;
import com.example.chat_app.model.entity.Enums;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Collection;
import java.util.List;

import java.time.LocalDateTime;

@Table(name = "users")
@Entity
@Getter
@Setter
@NoArgsConstructor

public class User implements UserDetails {
    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    @Column(name = "user_name", unique = true, nullable = false)
    private String username;

    @Column(name = "phone_name")
    private String phoneNumber;

    private String name;

    private String password;

    private String bio;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "sender", fetch = FetchType.EAGER)
    private List<Message> sentMessages;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private List<Member> memberships;

    @OneToMany(mappedBy = "owner", fetch = FetchType.EAGER)
    private List<Contact> myContacts;

    @OneToMany(mappedBy = "caller", fetch = FetchType.EAGER)
    private List<Call_log> initiatedCalls;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String toString() {
        return "User{" + "id=" + id + ", username='" + username + '\'' + '}';
    }
}
