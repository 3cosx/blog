package cn.cosx.blog.notice.exception;


import cn.cosx.blog.base.exception.ErrorCode;

public enum NoticeErrorCodeEnum implements ErrorCode {

    NOTICE_FAIL_SEND("NOTICE_FAIL_SEND", "发送失败"),

    ;

    private String code;

    private String message;

    NoticeErrorCodeEnum(String code, String message) {
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
