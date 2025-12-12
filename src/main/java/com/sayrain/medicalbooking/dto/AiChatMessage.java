package com.sayrain.medicalbooking.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiChatMessage {
    private String role;
    private String content;
    private Instant timestamp;
}
