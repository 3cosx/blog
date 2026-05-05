package cn.cosx.blog.knowledge.chat.controller;

import cn.cosx.blog.knowledge.chat.service.ChatMessageService;
import cn.cosx.blog.knowledge.common.BaseResult;
import cn.cosx.blog.knowledge.common.result.Result;
import cn.cosx.blog.knowledge.chat.entity.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/knowledge/messages")
public class ChatMessageController {

    @Autowired
    private ChatMessageService chatMessageService;

    /**
     * 根据消息ID查询
     */
    @GetMapping("/{messageId}")
    public BaseResult<ChatMessage> getByMessageId(@PathVariable String messageId) {
        ChatMessage message = chatMessageService.getByMessageId(messageId);
        return BaseResult.newSuccess(message);
    }

    /**
     * 根据会话ID查询消息列表（升序）
     */
    @GetMapping
    public BaseResult<List<ChatMessage>> listByConversationId(@RequestParam("conversationId") String conversationId) {
        List<ChatMessage> messages = chatMessageService.listByConversationId(conversationId);
        return BaseResult.newSuccess(messages);
    }

}
