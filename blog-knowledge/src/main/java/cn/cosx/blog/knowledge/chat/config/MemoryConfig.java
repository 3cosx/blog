package cn.cosx.blog.knowledge.chat.config;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MemoryConfig {


    @Bean
    public ChatMemoryProvider chatMemoryProvider() {
        return conversationId -> MessageWindowChatMemory.builder().id(conversationId).maxMessages(10).build();
    }
}
