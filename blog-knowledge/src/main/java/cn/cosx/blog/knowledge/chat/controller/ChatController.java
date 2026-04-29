package cn.cosx.blog.knowledge.chat.controller;

import cn.cosx.blog.knowledge.ai.service.TitleSummaryService;
import cn.cosx.blog.knowledge.chat.service.IChatConversationService;
import cn.cosx.blog.knowledge.chat.service.IChatMessageService;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/knowledge")
public class ChatController {

    @Value("${langchain4j.open-ai.chat-model.api-key}")
    private String chatModelApiKey;

    @Value("${langchain4j.open-ai.chat-model.base-url}")
    private String chatModelBaseUrl;
    @Autowired
    private IChatConversationService chatConversationService;

    @Autowired
    private IChatMessageService chatMessageService;

    @PostMapping("/chat")
    public Flux<String> chat(@RequestParam(required = false)String conversationId, String question){

        final String finalConversationId;
        if(conversationId == null || conversationId.isBlank()){
            String title = question.substring(0, Math.min(question.length(), 20));
            finalConversationId = chatConversationService.saveConversation(title);

            Thread.ofVirtual().name("chat-thread-" + finalConversationId).start(()->{
                String aiTitle = generateTitle(question);
                chatConversationService.updateTitle(finalConversationId, aiTitle);
            });
        }else{
            finalConversationId = conversationId;
        }

        //保存用户信息
        chatMessageService.saveUserMessage(finalConversationId,question);

        //意图识别

        //llm调用


        return null;

    }
    
    public String generateTitle(String question){
        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .apiKey(chatModelApiKey)
                .baseUrl(chatModelBaseUrl)
                .modelName("gpt-3.5-flash")
                .temperature(0.7)
                .customParameters(Map.of("enable_thinking", false))
                .build();

        TitleSummaryService titleSummaryService = AiServices.builder(TitleSummaryService.class)
                .chatModel(chatModel)
                .build();

        return titleSummaryService.generateTitle(question);


    }
}
