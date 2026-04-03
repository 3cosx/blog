package cn.cosx.blog.user.infrastructure.exception;


import cn.cosx.blog.base.exception.ErrorCode;

public enum UserErrorCodeEnum implements ErrorCode {

    USER_NOT_EXIST("USER_NOT_EXIST", "用户不存在"),
    NICK_NAME_CAN_NOT_BE_EMPTY("NICK_NAME_CAN_NOT_BE_EMPTY", "昵称不能为空"),
    UPDATE_FAILED("UPDATE_FAILED", "更新失败"),
    ;

    private String code;

    private String message;

    UserErrorCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }
    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
