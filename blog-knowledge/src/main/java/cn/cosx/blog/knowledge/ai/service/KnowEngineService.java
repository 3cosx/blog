package cn.cosx.blog.knowledge.ai.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

public interface KnowEngineService {

    String chat(@MemoryId String conversationId, @UserMessage String message);


    Flux<String> chatStream(@MemoryId String conversationId, @UserMessage String message);



}
