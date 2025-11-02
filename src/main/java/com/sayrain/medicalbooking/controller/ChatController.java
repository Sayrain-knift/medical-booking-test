package com.sayrain.medicalbooking.controller;

import com.sayrain.medicalbooking.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

@RequiredArgsConstructor
@CrossOrigin("*")
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final ChatHistoryRepository chatHistoryRepository;

    @GetMapping(value = "/ask", produces = "text/html;charset=UTF-8")
    public Flux<String> chat(@RequestParam(value = "prompt", defaultValue = "你好世界") String prompt,
                             @RequestParam(value = "chatId", required = false) String chatId) {
        // 修复空指针问题
        String conversationId = chatId != null ? chatId : "default-conversation";
        chatHistoryRepository.save("default", conversationId);

        return chatClient
                .prompt(prompt)
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId))
                .stream()
                .content();
    }
}