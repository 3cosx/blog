package cn.cosx.blog.knowledge.chat.entity;

import cn.cosx.blog.knowledge.chat.enums.ConversationStatus;
import cn.cosx.blog.knowledge.document.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会话实体类
 * 对应数据库表 chat_conversation
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("chat_conversation")
public class ChatConversation extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会话唯一标识
     */
    private String conversationId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 会话标题
     */
    private String title;

    /**
     * 会话状态：ACTIVE-活跃，ARCHIVED-已归档，DELETED-已删除
     */
    private ConversationStatus status;
}
