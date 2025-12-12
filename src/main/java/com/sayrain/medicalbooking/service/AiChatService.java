package com.sayrain.medicalbooking.service;

import com.sayrain.medicalbooking.dto.AiChatHistoryResponse;
import com.sayrain.medicalbooking.dto.AiChatRequest;
import com.sayrain.medicalbooking.dto.AiChatResponse;

public interface AiChatService {

    AiChatResponse sendMessage(AiChatRequest request);

    AiChatHistoryResponse getHistory(String chatId);

    void clearHistory(String chatId);
}
