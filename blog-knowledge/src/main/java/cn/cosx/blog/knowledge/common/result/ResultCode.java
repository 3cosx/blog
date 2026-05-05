package cn.cosx.blog.knowledge.common.result;

public enum ResultCode {
    SUCCESS(200, "成功"),
    ERROR(500, "失败");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() { return code; }
    public String getMessage() { return message; }
}
