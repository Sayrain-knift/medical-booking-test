package com.sayrain.medicalbooking.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiChatResponse {
    private String chatId;
    private String response;
    private Instant timestamp;
}
