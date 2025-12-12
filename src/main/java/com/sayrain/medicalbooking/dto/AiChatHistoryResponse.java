package com.sayrain.medicalbooking.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiChatHistoryResponse {
    private String chatId;
    private List<AiChatMessage> messages;
}
