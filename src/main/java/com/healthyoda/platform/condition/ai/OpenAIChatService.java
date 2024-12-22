package com.healthyoda.platform.condition.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

@Component
public class OpenAIChatService implements ChatService {


    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());



    private final ChatClient chatClient;


    public OpenAIChatService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }


    @Override
    public String chat(Message message) {
        return
        chatClient.prompt()
                .user(message.getContent())
                .call().content();
    }
}
