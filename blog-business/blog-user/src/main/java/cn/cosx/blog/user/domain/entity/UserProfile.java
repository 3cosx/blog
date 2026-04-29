package cn.cosx.blog.user.domain.entity;

import cn.cosx.blog.database.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户扩展信息表实体类
 */
@Setter
@Getter
@TableName("user_profile")
public class UserProfile extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 个人简介
     */
    @TableField("bio")
    private String bio;

    /**
     * 技能标签（JSON数组）
     */
    @TableField("skills")
    private String skills;

    /**
     * 项目经历（JSON数组）
     */
    @TableField("projects")
    private String projects;

    /**
     * 教育背景（JSON数组）
     */
    @TableField("education")
    private String education;

    /**
     * 社交链接（JSON）
     */
    @TableField("social_links")
    private String socialLinks;
}