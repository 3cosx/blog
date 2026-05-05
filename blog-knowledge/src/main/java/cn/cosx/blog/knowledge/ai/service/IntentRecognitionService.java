package cn.cosx.blog.knowledge.ai.service;


import cn.cosx.blog.knowledge.ai.model.IntentRecognitionResult;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface IntentRecognitionService {

    @SystemMessage(fromResource = "prompts/intent-recognition-new-prompt.txt")
    IntentRecognitionResult chat(@UserMessage String question);
}
