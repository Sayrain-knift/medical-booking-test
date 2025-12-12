package com.sayrain.medicalbooking.service.impl;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.sayrain.medicalbooking.dto.AiChatHistoryResponse;
import com.sayrain.medicalbooking.dto.AiChatMessage;
import com.sayrain.medicalbooking.dto.AiChatRequest;
import com.sayrain.medicalbooking.dto.AiChatResponse;
import com.sayrain.medicalbooking.repository.ChatHistoryRepository;
import com.sayrain.medicalbooking.service.AiChatService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private static final String DEFAULT_CHAT_ID = "default";

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final ChatHistoryRepository chatHistoryRepository;

    private final Map<String, List<AiChatMessage>> conversationHistory = new ConcurrentHashMap<>();

    @Override
    public AiChatResponse sendMessage(AiChatRequest request) {
        if (request == null || !StringUtils.hasText(request.getMessage())) {
            throw new IllegalArgumentException("消息内容不能为空");
        }

        String chatId = resolveChatId(request.getChatId());
        chatHistoryRepository.save("ai", chatId);

        // 记录用户消息
        addMessage(chatId, "user", request.getMessage());

        String responseText = chatClient
                .prompt(request.getMessage())
                .advisors(advisor -> advisor.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .call()
                .content();

        Instant assistantTimestamp = addMessage(chatId, "assistant", responseText);

        log.debug("AI chat response generated for chatId={}", chatId);
        return new AiChatResponse(chatId, responseText, assistantTimestamp);
    }

    @Override
    public AiChatHistoryResponse getHistory(String chatId) {
        String resolvedChatId = resolveChatId(chatId);
        List<AiChatMessage> history = conversationHistory.getOrDefault(resolvedChatId, List.of());
        return new AiChatHistoryResponse(resolvedChatId, List.copyOf(history));
    }

    @Override
    public void clearHistory(String chatId) {
        String resolvedChatId = resolveChatId(chatId);
        conversationHistory.remove(resolvedChatId);
        chatMemory.clear(resolvedChatId);
        log.info("Cleared AI chat history for chatId={}", resolvedChatId);
    }

    private Instant addMessage(String chatId, String role, String content) {
        Instant timestamp = Instant.now();
        conversationHistory
                .computeIfAbsent(chatId, key -> new CopyOnWriteArrayList<>())
                .add(new AiChatMessage(role, content, timestamp));
        return timestamp;
    }

    private String resolveChatId(String chatId) {
        return StringUtils.hasText(chatId) ? chatId : DEFAULT_CHAT_ID;
    }
}
