package cn.cosx.blog.mentor.agent.document.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文档分片实体类
 * 对应数据库表 knowledge_segment
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("knowledge_segment")
public class SegmentEntity extends BaseEntity {

    /**
     * 分片ID（自增主键）
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 分片唯一标识（用于向量化存储关联）
     */
    @TableField("chunk_id")
    private String chunkId;

    /**
     * 文本内容
     */
    @TableField("text")
    private String text;

    /**
     * 所属文档ID（关联knowledge_document.doc_id）
     */
    @TableField("document_id")
    private Long documentId;

    /**
     * 分片顺序（文档内排序）
     */
    @TableField("chunk_order")
    private Integer chunkOrder;

    /**
     * 嵌入向量ID（Elasticsearch中的向量ID）
     */
    @TableField("embedding_id")
    private String embeddingId;

    /**
     * 分片状态：INIT(初始化)、VECTOR_STORED(已向量化)
     */
    @TableField("status")
    private String status;

    /**
     * 元数据JSON（包含parent_chunk_id、brother_chunk_id等关联信息）
     */
    @TableField("metadata")
    private String metadata;

    /**
     * 是否跳过嵌入向量生成 0-否 1-是
     */
    @TableField("skip_embedding")
    private Integer skipEmbedding;
}
