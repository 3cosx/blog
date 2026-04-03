package cn.cosx.blog.auth.param;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class LoginParam implements Serializable {

    @NotNull
    public String email;

    @NotNull
    public String captcha;

    public Boolean rememberMe;

}
