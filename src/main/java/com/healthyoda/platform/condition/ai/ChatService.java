package com.healthyoda.platform.condition.ai;

import org.springframework.ai.chat.messages.Message;

public interface ChatService {
    String chat(Message message);
}
