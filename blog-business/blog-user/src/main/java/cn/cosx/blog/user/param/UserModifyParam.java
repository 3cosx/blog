package cn.cosx.blog.user.param;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Setter
@Getter
@AllArgsConstructor
@ToString
public class UserModifyParam implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;

    private String nickName;

    private String avatar;

    private String email;

    private String phone;

}
