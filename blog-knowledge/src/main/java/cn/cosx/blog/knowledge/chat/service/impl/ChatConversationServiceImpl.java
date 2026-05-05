package cn.cosx.blog.knowledge.chat.service.impl;

import cn.cosx.blog.knowledge.chat.entity.ChatConversation;
import cn.cosx.blog.knowledge.chat.enums.ConversationStatus;
import cn.cosx.blog.knowledge.chat.mapper.ChatConversationMapper;
import cn.cosx.blog.knowledge.chat.service.ChatConversationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 会话Service实现类
 */
@Service
public class ChatConversationServiceImpl extends ServiceImpl<ChatConversationMapper, ChatConversation> implements ChatConversationService {

    @Override
    public String saveConversation(String title) {
        String conversationId = UUID.randomUUID().toString();
        ChatConversation conversation = new ChatConversation();
        conversation.setConversationId(conversationId);
        conversation.setTitle(title);
        conversation.setStatus(ConversationStatus.ACTIVE);
        conversation.setUserId("user");
        this.save(conversation);
        return conversation.getConversationId();
    }

    @Override
    public ChatConversation getByConversationId(String conversationId) {
        LambdaQueryWrapper<ChatConversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatConversation::getConversationId, conversationId);
        return this.getOne(wrapper);
    }

    @Override
    public boolean updateStatus(String conversationId, ConversationStatus status) {
        LambdaUpdateWrapper<ChatConversation> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ChatConversation::getConversationId, conversationId)
                .set(ChatConversation::getStatus, status);
        return this.update(wrapper);
    }

    @Override
    public List<ChatConversation> listByUserId(String userId) {
        LambdaQueryWrapper<ChatConversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatConversation::getUserId, userId)
                .orderByDesc(ChatConversation::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public List<ChatConversation> listByUserIdAndStatus(String userId, ConversationStatus status) {
        LambdaQueryWrapper<ChatConversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatConversation::getUserId, userId)
                .eq(ChatConversation::getStatus, status)
                .orderByDesc(ChatConversation::getCreateTime);
        return this.list(wrapper);
    }


    @Override
    public boolean updateTitle(String conversationId, String title) {
        return this.update(new LambdaUpdateWrapper<ChatConversation>()
                .eq(ChatConversation::getConversationId, conversationId)
                .set(ChatConversation::getTitle, title)
                .set(ChatConversation::getUpdateTime, LocalDateTime.now()));
    }
}
