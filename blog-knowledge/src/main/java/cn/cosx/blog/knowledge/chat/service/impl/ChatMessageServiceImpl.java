package cn.cosx.blog.knowledge.chat.service.impl;

import cn.cosx.blog.knowledge.chat.entity.ChatMessage;
import cn.cosx.blog.knowledge.chat.mapper.ChatMessageMapper;
import cn.cosx.blog.knowledge.chat.service.IChatMessageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.langchain4j.data.message.ChatMessageType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * 消息Service实现类
 */
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements IChatMessageService {

    @Override
    public String saveUserMessage(String conversationId, String question) {
        String messageId = UUID.randomUUID().toString().replace("-", "");
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setConversationId(conversationId);
        chatMessage.setContent(question);
        chatMessage.setType(ChatMessageType.USER);

        chatMessage.setMessageId(UUID.randomUUID().toString());

        this.save(chatMessage);
        return messageId;
    }

    @Override
    public ChatMessage getByMessageId(String messageId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getMessageId, messageId);
        return this.getOne(wrapper);
    }

    @Override
    public List<ChatMessage> listByConversationId(String conversationId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getConversationId, conversationId)
                .orderByAsc(ChatMessage::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public boolean updateRagReferences(String messageId, String ragReferences) {
        LambdaUpdateWrapper<ChatMessage> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ChatMessage::getMessageId, messageId)
                .set(ChatMessage::getRagReferences, ragReferences);
        return this.update(wrapper);
    }


    @Override
    public void updateTransformContent(String messageId, String transformContent) {
        ChatMessage update = new ChatMessage();
        update.setTransformContent(transformContent);
        this.update(update, new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getMessageId, messageId));
    }
}
