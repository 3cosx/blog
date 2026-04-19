package cn.cosx.blog.mentor.agent.document.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 实体基类
 * 包含公共字段
 */
@Getter
@Setter
public abstract class BaseEntity {

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */

    private LocalDateTime updateTime;

    /**
     * 乐观锁版本号
     */
    //@Version
    protected Integer lockVersion;
    /**
     * 删除标记（0-未删除 1-已删除）
     */
    @TableLogic
    private Integer deleted;
}
