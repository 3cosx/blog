package cn.cosx.blog.api.user.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class UserInfo implements Serializable {

    private static final long serialVersionUID = 1L;


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
     * 邮箱
     */
    private String email;

}
