package cn.cosx.blog.knowledge.chat.service;

import cn.cosx.blog.knowledge.chat.entity.ChatMessage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 消息Service接口
 */
public interface IChatMessageService extends IService<ChatMessage> {

    /**
     * 保存消息
     */
    String saveUserMessage(String conversationId, String question);

    /**
     * 根据消息ID查询
     */
    ChatMessage getByMessageId(String messageId);

    /**
     * 根据会话ID查询消息列表
     */
    List<ChatMessage> listByConversationId(String conversationId);

    public void updateTransformContent(String messageId, String transformContent);
    /**
     * 更新RAG引用
     */
    boolean updateRagReferences(String messageId, String ragReferences);
}
