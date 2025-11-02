package com.sayrain.medicalbooking.service;

import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public interface ChatMemory {
    default void add(String conversationId, Message  message){
        this.add(conversationId, List.of(message));
    }
    //添加会话信息到指定的conversationId的历史会话中
    void add(String conversationId, List<Message> messages);

    //根据conversationId查询历史会话
    List<Message> get(String conversationId,int lastN);

    //清除指定conversationId的会话历史
    void clear(String conversationId);
}
