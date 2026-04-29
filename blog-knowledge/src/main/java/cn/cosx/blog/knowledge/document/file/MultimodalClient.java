package cn.cosx.blog.knowledge.document.file;


import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;

import dev.langchain4j.model.openai.OpenAiChatModel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

/**
 * 多模态大模型客户端
 * 用于处理文档中的图片，生成描述
 */
@Slf4j
@Component
public class MultimodalClient {

    @Value("${langchain4j.open-ai.chat-model.api-key:}")
    private String apiKey;

    @Value("${langchain4j.open-ai.chat-model.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1}")
    private String baseUrl;

    public String generateImageDescription(Path imagePath) {
        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName("qwen3-vl-plus")
                .temperature(0.7)
                .logResponses(true)
                .logRequests(true)
                .build();

        // 读取图片并转为base64
        byte[] imageBytes = null;
        try {
            imageBytes = Files.readAllBytes(imagePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        UserMessage userMessage = UserMessage.from(
                new TextContent("请描述这张图片的内容，包括场景、对象、布局、颜色、文字信息，直接输出纯文本描述，不要多余说明。"),
                ImageContent.from(base64Image, "image/png"));
        return chatModel.chat(userMessage).aiMessage().text();
    }

}