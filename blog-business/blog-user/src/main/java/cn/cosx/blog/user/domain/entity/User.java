package cn.cosx.blog.user.domain.entity;

import cn.cosx.blog.api.user.enums.UserRoleEnum;
import cn.cosx.blog.api.user.enums.UserStateEnum;
import cn.cosx.blog.api.user.request.UserRegisterRequest;
import cn.cosx.blog.api.user.vo.UserInfo;
import cn.cosx.blog.database.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 用户信息表实体类
 *
 * @author cosx
 */
@Setter
@Getter
@TableName("users")
public class User extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 用户昵称
     */
    @TableField("nick_name")
    private String nickName;

    /**
     * 用户状态（NORMAL，FROZEN）
     */
    @TableField("state")
    private String state;

    /**
     * 手机号码
     */
    @TableField("telephone")
    private String telephone;

    /**
     * 最后登录时间
     */
    @TableField("last_login_time")
    private Date lastLoginTime;

    /**
     * 用户头像URL
     */
    @TableField("profile_photo_url")
    private String profilePhotoUrl;

    /**
     * 实名认证状态（TRUE或FALSE）
     */
    @TableField("certification")
    private Boolean certification;

    /**
     * 真实姓名
     */
    @TableField("real_name")
    private String realName;

    /**
     * 身份证号
     */
    @TableField("id_card_no")
    private String idCardNo;

    /**
     * 用户角色(CUSTOM,ADMIN)
     */
    @TableField("user_role")
    private String userRole;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;


    public User register(String email,String nickName) {
        this.setEmail(email);
        this.setState(UserStateEnum.INIT.getCode());
        this.setNickName(nickName);
        this.setUserRole(UserRoleEnum.NORMAL.getCode());
        return this;
    }

}
