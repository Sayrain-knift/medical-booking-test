package com.sayrain.medicalbooking.controller;

import com.sayrain.medicalbooking.dto.AiChatHistoryResponse;
import com.sayrain.medicalbooking.dto.AiChatRequest;
import com.sayrain.medicalbooking.dto.AiChatResponse;
import com.sayrain.medicalbooking.service.AiChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/api/ai/chat")
public class AiChatController {

    private final AiChatService aiChatService;

    @PostMapping
    public ResponseEntity<AiChatResponse> sendMessage(@Valid @RequestBody AiChatRequest request) {
        AiChatResponse response = aiChatService.sendMessage(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<AiChatHistoryResponse> getHistory(@RequestParam(value = "chatId", required = false) String chatId) {
        return ResponseEntity.ok(aiChatService.getHistory(chatId));
    }

    @DeleteMapping("/history")
    public ResponseEntity<Void> clearHistory(@RequestParam(value = "chatId", required = false) String chatId) {
        aiChatService.clearHistory(chatId);
        return ResponseEntity.noContent().build();
    }
}
