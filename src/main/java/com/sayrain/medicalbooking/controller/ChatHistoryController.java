package com.sayrain.medicalbooking.controller;

import com.sayrain.medicalbooking.repository.ChatHistoryRepository;
import com.sayrain.medicalbooking.util.MessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/history")
@CrossOrigin("*")
public class ChatHistoryController {
    private final ChatHistoryRepository chatHistoryRepository;
    private final ChatMemory chatMemory;

    @GetMapping("/{type}")
    public ResponseEntity<List<String>> getChatIds(@PathVariable("type") String type) {
        try {
            List<String> chatIds = chatHistoryRepository.getChatIds(type);
            return ResponseEntity.ok(chatIds);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(List.of());
        }
    }

    @GetMapping("/{type}/{chatId}")
    public ResponseEntity<List<MessageVO>> getChatHistory(@PathVariable("type") String type,
                                                          @PathVariable("chatId") String chatId) {
        try {
            List<Message> messages = chatMemory.get(chatId, Integer.MAX_VALUE);
            if (messages == null) {
                return ResponseEntity.ok(List.of());
            }
            List<MessageVO> messageVOs = messages.stream()
                    .map(MessageVO::new)
                    .toList();
            return ResponseEntity.ok(messageVOs);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(List.of());
        }
    }
}