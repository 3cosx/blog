package cn.cosx.blog.api.user.response;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户查询响应
 *
 * @author cosx
 */
@Data
public class UserQueryResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 用户状态（NORMAL，FROZEN）
     */
    private String state;

    /**
     * 手机号码
     */
    private String telephone;

    /**
     * 最后登录时间
     */
    private Date lastLoginTime;

    /**
     * 用户头像URL
     */
    private String profilePhotoUrl;

    /**
     * 实名认证状态（TRUE或FALSE）
     */
    private Boolean certification;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 身份证号
     */
    private String idCardNo;

    /**
     * 用户角色(CUSTOM,ADMIN)
     */
    private String userRole;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
