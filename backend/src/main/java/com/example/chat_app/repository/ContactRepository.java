package com.example.chat_app.repository;

import com.example.chat_app.model.entity.Contact;
import com.example.chat_app.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContactRepository extends JpaRepository<Contact, UUID> {

    Optional<Contact> findByOwnerAndContactUser(User owner, User contactUser);

    List<Contact> findAllByOwner(User owner);
}
