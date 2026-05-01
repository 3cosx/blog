package cn.cosx.blog.knowledge.document.domain.entity;


import cn.cosx.blog.knowledge.document.infra.enums.DocumentStatus;
import cn.cosx.blog.knowledge.document.infra.enums.FileType;
import cn.cosx.blog.knowledge.document.infra.enums.UseTypeEnums;
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

    private UseTypeEnums useType;

    /**
     * 文件类型
     */
    private FileType fileType;

    /**
     * 原始文件名（含扩展名，用于推断文件类型）
     */
    private String originalFilename;

    /**
     * 扩展字段，保存JSON字符串
     */
    private String extension;

}
