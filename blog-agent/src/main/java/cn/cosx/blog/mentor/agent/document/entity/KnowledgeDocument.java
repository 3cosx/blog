package cn.cosx.blog.mentor.agent.document.entity;

import cn.cosx.blog.mentor.agent.document.enums.DocumentStatus;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文档信息实体类
 * 对应数据库表 knowledge_document
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("knowledge_document")
public class KnowledgeDocument extends BaseEntity {

    /**
     * 文档ID
     */
    @TableId(type = IdType.AUTO)
    private Long docId;

    /**
     * 文档标题
     */
    private String docTitle;

    /**
     * 上传用户
     */
    private String uploadUser;

    /**
     * 文档URL
     */
    private String docUrl;

    /**
     * 转换后的文档URL
     */
    private String convertedDocUrl;

    /**
     * 状态：INIT, UPLOADED, CONVERTING, CONVERTED, CHUNKED, VECTOR_STORED
     */
    private DocumentStatus status;

    /**
     * 可见范围
     */
    private String accessibleBy;

    /**
     * 文档描述
     */
    private String description;


    /**
     * 扩展字段，保存JSON字符串
     */
    private String extension;

}
