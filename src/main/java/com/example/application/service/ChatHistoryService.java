package com.example.application.service;

import com.example.application.model.ChatHistory;
import com.example.application.repository.ChatHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.CompletableFuture;


@Service
@EnableAsync
public class ChatHistoryService {
    private final ChatHistoryRepository chatHistoryRepository;

    @Autowired
    public ChatHistoryService(ChatHistoryRepository chatHistoryRepository) {
        this.chatHistoryRepository = chatHistoryRepository;
    }

    public void saveChatHistory(ChatHistory chatHistory) {
        chatHistoryRepository.save(chatHistory);
    }

    public List<ChatHistory> getChatHistoryForUser(String username) {
        return chatHistoryRepository.findBySenderIdOrReceiverId(username, username);
    }

    @Async
    public CompletableFuture<Void> saveChatHistoryAsync(ChatHistory chatHistory) {
        return CompletableFuture.runAsync(() -> chatHistoryRepository.save(chatHistory));
    }
}
