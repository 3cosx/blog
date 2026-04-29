package cn.cosx.blog.knowledge.chat.mapper;

import cn.cosx.blog.knowledge.chat.entity.ChatConversation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会话Mapper
 */
@Mapper
public interface ChatConversationMapper extends BaseMapper<ChatConversation> {
}
