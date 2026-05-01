package cn.cosx.blog.knowledge.chat.entity;

import cn.cosx.blog.knowledge.chat.enums.RetrievalSource;
import cn.cosx.blog.knowledge.document.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import dev.langchain4j.data.message.ChatMessageType;
import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * 消息实体类
 * 对应数据库表 chat_message
 */
@Data
@TableName(value = "chat_message", autoResultMap = true)
public class ChatMessage extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 消息唯一标识
     */
    private String messageId;

    /**
     * 所属会话ID
     */
    private String conversationId;

    /**
     * 角色：user/assistant
     */
    private ChatMessageType type;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 改写后的内容
     */
    private String transformContent;

    /**
     * Token数量
     */
    private Integer tokenCount;

    /**
     * 使用的模型名称
     */
    private String modelName;

    /**
     * RAG引用内容JSON数组
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<RagReference> ragReferences;

    /**
     * 扩展元数据JSON格式
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> metadata;

    /**
     * RAG引用内容内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RagReference {
        /**
         * 文档ID
         */
        private String documentId;

        /**
         * 文档URL
         */
        private String url;

        /**
         * 文档标题
         */
        private String documentTitle;

        /**
         * 文档块ID
         */
        private String chunkId;

        /**
         * 文档块内容
         */
        private String chunkContent;

        /**
         * 相似度分数
         */
        private Double similarityScore;

        private Double rerankScore;

        /**
         * 检索来源：vector/keyword/hybrid/rerank
         */
        private RetrievalSource retrievalSource;

        /**
         * 扩展元数据
         */
        private Map<String, Object> metadata;
    }
}
