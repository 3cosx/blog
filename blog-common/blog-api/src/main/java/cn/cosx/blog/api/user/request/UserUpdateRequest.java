package cn.cosx.blog.api.user.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新请求
 *
 * @author cosx
 */
@Data
public class UserUpdateRequest implements Serializable {

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
     * 用户头像URL
     */
    private String profilePhotoUrl;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 身份证号
     */
    private String idCardNo;
}
