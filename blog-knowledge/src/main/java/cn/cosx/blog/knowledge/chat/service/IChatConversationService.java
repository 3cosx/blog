package cn.cosx.blog.knowledge.chat.service;

import cn.cosx.blog.knowledge.chat.entity.ChatConversation;
import cn.cosx.blog.knowledge.chat.enums.ConversationStatus;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 会话Service接口
 */
public interface IChatConversationService extends IService<ChatConversation> {

    /**
     * 保存会话
     */
    String saveConversation(String title);

    /**
     * 根据会话ID查询
     */
    ChatConversation getByConversationId(String conversationId);

    /**
     * 更新会话状态
     */
    boolean updateStatus(String conversationId, ConversationStatus status);

    /**
     * 根据用户ID查询会话列表
     */
    List<ChatConversation> listByUserId(String userId);

    /**
     * 根据用户ID和状态查询会话列表
     */
    List<ChatConversation> listByUserIdAndStatus(String userId, ConversationStatus status);

    boolean updateTitle(String finalConversationId, String aiTitle);
}
