package com.example.chat_app.repository;

import com.example.chat_app.model.entity.Call_log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CallLogsRepository extends JpaRepository<Call_log, UUID> {
}
