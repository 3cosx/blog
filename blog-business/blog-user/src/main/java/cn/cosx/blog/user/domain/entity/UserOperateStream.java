package cn.cosx.blog.user.domain.entity;

import cn.cosx.blog.database.domain.entity.BaseEntity;
import cn.cosx.blog.user.param.UserModifyParam;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * 用户操作流水表实体类
 *
 * @author cosx
 */
@Setter
@Getter
@TableName("user_operate_stream")
@ToString
public class UserOperateStream extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private String userId;

    /**
     * 操作类型
     */
    @TableField("type")
    private String type;

    /**
     * 操作时间
     */
    @TableField("operate_time")
    private Date operateTime;

    /**
     * 操作参数
     */
    @TableField("param")
    private String param;

    /**
     * 扩展字段
     */
    @TableField("extend_info")
    private String extendInfo;


}
