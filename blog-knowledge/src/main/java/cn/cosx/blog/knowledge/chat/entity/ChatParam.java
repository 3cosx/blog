package cn.cosx.blog.knowledge.chat.entity;

import cn.cosx.blog.knowledge.ai.model.IntentRecognitionResult;

public record ChatParam(String conversationId,
                        String question,
                        String messageId,
                        IntentRecognitionResult intentRecognitionResult) {
}
