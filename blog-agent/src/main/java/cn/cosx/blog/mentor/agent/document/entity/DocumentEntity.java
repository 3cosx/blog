package cn.cosx.blog.mentor.agent.document.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
public class DocumentEntity extends BaseEntity {

    /**
     * 文档ID（自增主键）
     */
    @TableId(value = "doc_id", type = IdType.AUTO)
    private Long docId;

    /**
     * 文档标题
     */
    @TableField("doc_title")
    private String docTitle;

    /**
     * 上传用户
     */
    @TableField("upload_user")
    private String uploadUser;

    /**
     * 文档存储URL
     */
    @TableField("doc_url")
    private String docUrl;

    /**
     * 解析后文档存储URL
     */
    @TableField("converted_doc_url")
    private String convertedDocUrl;

    /**
     * 文档状态
     */
    @TableField("status")
    private String status;

    /**
     * 可见范围权限控制（如角色名称）
     */
    @TableField("accessible_by")
    private String accessibleBy;
}
