package cn.cosx.blog.auth.exception;


import cn.cosx.blog.base.exception.ErrorCode;

/**
 * 认证错误码
 *
 * @author Hollis
 */
public enum AuthErrorCode implements ErrorCode {


    /**
     * 验证码错误
     */
    VERIFICATION_CODE_WRONG("VERIFICATION_CODE_WRONG", "验证码错误"),
    ;


    private String code;

    private String message;

    AuthErrorCode(String code, String message) {
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
