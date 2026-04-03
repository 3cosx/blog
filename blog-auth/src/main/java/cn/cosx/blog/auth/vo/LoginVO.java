package cn.cosx.blog.auth.vo;


import cn.cosx.blog.api.user.response.UserQueryResponse;
import cn.cosx.blog.api.user.vo.UserInfo;
import cn.dev33.satoken.stp.StpUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;


@Getter
@Setter
@ToString
@NoArgsConstructor
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;

    private String token;

    private Long tokenExpiration;


    public LoginVO(String userId, String token, Long tokenExpiration) {
        this.userId = userId;
        this.token = token;
        this.tokenExpiration = tokenExpiration;
    }

    public LoginVO(UserInfo userInfo){
        this.userId = userInfo.getUserId();
        this.token = StpUtil.getTokenValue();
        this.tokenExpiration = StpUtil.getSessionTimeout();
    }
}
