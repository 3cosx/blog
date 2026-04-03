package cn.cosx.blog.api.user.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserRoleEnum {

    NORMAL("NORMAL"),



    ADMIN("ADMIN")

    ;

    String code;
}
