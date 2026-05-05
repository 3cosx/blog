package cn.cosx.blog.knowledge.chat.controller;


import cn.cosx.blog.knowledge.chat.entity.ChatConversation;
import cn.cosx.blog.knowledge.chat.entity.ChatMessage;
import cn.cosx.blog.knowledge.chat.service.ChatMessageService;
import cn.cosx.blog.knowledge.common.BaseResult;
import cn.cosx.blog.knowledge.common.result.Result;
import cn.cosx.blog.knowledge.chat.enums.ConversationStatus;
import cn.cosx.blog.knowledge.chat.service.ChatConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/knowledge/conversations")
public class ChatConversationController {

    @Autowired
    private ChatConversationService chatConversationService;

    @Autowired
    private ChatMessageService chatMessageService;

    /**
     * 查询当前用户的所有会话，按创建时间逆序
     */
    @GetMapping
    public BaseResult<List<ChatConversation>> listConversations(@RequestParam(value = "userId", defaultValue = "user") String userId) {
        List<ChatConversation> conversations = chatConversationService.listByUserId(userId);
        return BaseResult.newSuccess(conversations);
    }

    /**
     * 根据状态查询会话
     */
    @GetMapping("/status/{status}")
    public BaseResult<List<ChatConversation>> listByStatus(@RequestParam(value = "userId", defaultValue = "user") String userId,
                                                        @PathVariable ConversationStatus status) {
        List<ChatConversation> conversations = chatConversationService.listByUserIdAndStatus(userId, status);
        return BaseResult.newSuccess(conversations);
    }

    /**
     * 获取单个会话
     */
    @GetMapping("/{conversationId}")
    public BaseResult<ChatConversation> getConversation(@PathVariable String conversationId) {
        ChatConversation conversation = chatConversationService.getByConversationId(conversationId);
        return BaseResult.newSuccess(conversation);
    }

    /**
     * 查询某个会话的消息列表，按创建时间逆序
     */
    @GetMapping("/{conversationId}/messages")
    public BaseResult<List<ChatMessage>> listMessages(@PathVariable String conversationId) {
        List<ChatMessage> messages = chatMessageService.listByConversationIdDesc(conversationId);
        return BaseResult.newSuccess(messages);
    }

    /**
     * 更新会话标题
     */
    @PutMapping("/{conversationId}/title")
    public BaseResult<Boolean> updateTitle(@PathVariable String conversationId, @RequestParam("title") String title) {
        boolean result = chatConversationService.updateTitle(conversationId, title);
        return BaseResult.newSuccess(result);
    }

    /**
     * 更新会话状态
     */
    @PutMapping("/{conversationId}/status")
    public BaseResult<Boolean> updateStatus(@PathVariable String conversationId, @RequestParam("status") ConversationStatus status) {
        boolean result = chatConversationService.updateStatus(conversationId, status);
        return BaseResult.newSuccess(result);
    }
}
