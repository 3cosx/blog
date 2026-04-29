package cn.cosx.blog.knowledge.chat.mapper;

import cn.cosx.blog.knowledge.chat.entity.ChatMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息Mapper
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
