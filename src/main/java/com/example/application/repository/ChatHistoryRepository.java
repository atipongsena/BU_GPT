package com.example.application.repository;

import com.example.application.model.ChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {
    List<ChatHistory> findBySenderIdOrReceiverId(String senderId, String receiverId);
}
