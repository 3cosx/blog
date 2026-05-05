package cn.cosx.blog.knowledge.chat.controller;

import cn.cosx.blog.knowledge.ai.service.IntentRecognitionService;
import cn.cosx.blog.knowledge.ai.service.TitleSummaryService;
import cn.cosx.blog.knowledge.chat.entity.ChatParam;
import cn.cosx.blog.knowledge.chat.service.ChatApplicationService;
import cn.cosx.blog.knowledge.chat.service.ChatConversationService;
import cn.cosx.blog.knowledge.chat.service.ChatMessageService;
import cn.cosx.blog.knowledge.chat.service.CommonChatService;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/knowledge")
@Slf4j
public class ChatController {

    @Value("${langchain4j.open-ai.chat-model.api-key}")
    private String chatModelApiKey;

    @Value("${langchain4j.open-ai.chat-model.base-url}")
    private String chatModelBaseUrl;
    @Autowired
    private ChatConversationService chatConversationService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ChatApplicationService chatApplicationService;
    @Autowired
    private ChatModel chatModel;

    @Autowired
    private CommonChatService commonChatService;

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestParam(value = "conversationId", required = false)String conversationId, @RequestParam(value= "question") String question){

        SseEmitter emitter = new SseEmitter(0L); // 0 = no timeout

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

        String messageId = chatMessageService.saveUserMessage(finalConversationId, question);

        Flux.just("[PROGRESS]:正在识别您的意图...")
                .concatWith(
                        Mono.fromCallable(() -> {
                                    IntentRecognitionService intentRecognitionService = AiServices.builder(IntentRecognitionService.class)
                                            .chatModel(chatModel).build();
                                    return intentRecognitionService.chat(question);
                                })
                                .subscribeOn(Schedulers.boundedElastic())
                                .flatMapMany(intentRecognitionResult -> {

                                    if (!intentRecognitionResult.related()) {
                                        StringBuilder answer = new StringBuilder();
                                        return Flux.concat(
                                                Flux.just("[PROGRESS]:正在为您生成回答..."),

                                                commonChatService.streamChat(finalConversationId, question)
                                                        .doOnNext(answer::append)
                                                        .doOnComplete(() ->{
                                                            chatMessageService.saveAssisantMessage(finalConversationId,answer.toString());
                                                        })
                                        );
                                    }
                                    return chatApplicationService.chat(new ChatParam(finalConversationId, question, messageId, intentRecognitionResult));
                                })
                )
                .concatWith(Mono.just("[DONE]:" + finalConversationId))
                .onErrorResume(e -> {
                    log.error("流式对话异常: conversationId={}", finalConversationId, e);
                    return Flux.just("[ERROR]:" + e.getMessage(), "[DONE]:" + finalConversationId);
                })
                .subscribe(
                        data -> {
                            try {
                                emitter.send(SseEmitter.event().data(data));
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        },
                        error -> {
                            log.error("SSE 流出错: conversationId={}", finalConversationId, error);
                            emitter.completeWithError(error);
                        },
                        () -> emitter.complete()
                );

        return emitter;
    }
    
    public String generateTitle(String question){
        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .apiKey(chatModelApiKey)
                .baseUrl(chatModelBaseUrl)
                .modelName("qwen3.6-flash")
                .temperature(0.7)
                .customParameters(Map.of("enable_thinking", false))
                .build();

        TitleSummaryService titleSummaryService = AiServices.builder(TitleSummaryService.class)
                .chatModel(chatModel)
                .build();

        return titleSummaryService.generateTitle(question);


    }
}
