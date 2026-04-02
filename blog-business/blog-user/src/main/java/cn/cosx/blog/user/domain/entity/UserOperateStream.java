package cn.cosx.blog.user.domain.entity;

import cn.cosx.blog.database.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 用户操作流水表实体类
 *
 * @author cosx
 */
@Setter
@Getter
@TableName("user_operate_stream")
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getOperateTime() {
        return operateTime;
    }

    public void setOperateTime(Date operateTime) {
        this.operateTime = operateTime;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getExtendInfo() {
        return extendInfo;
    }

    public void setExtendInfo(String extendInfo) {
        this.extendInfo = extendInfo;
    }

    @Override
    public String toString() {
        return "UserOperateStream{" +
                "userId='" + userId + '\'' +
                ", type='" + type + '\'' +
                ", operateTime=" + operateTime +
                ", param='" + param + '\'' +
                ", extendInfo='" + extendInfo + '\'' +
                ", id=" + getId() +
                ", deleted=" + getDeleted() +
                ", lockVersion=" + getLockVersion() +
                ", createTime=" + getCreateTime() +
                ", updateTime=" + getUpdateTime() +
                '}';
    }
}
