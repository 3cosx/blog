package cn.cox.blog.notice.exception;

import cn.cosx.blog.base.exception.BizException;
import cn.cosx.blog.base.exception.ErrorCode;

public class NoticeException extends BizException {
    public NoticeException(ErrorCode errorCode) {
        super(errorCode);
    }

    public NoticeException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

    public NoticeException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause, errorCode);
    }

    public NoticeException(Throwable cause, ErrorCode errorCode) {
        super(cause, errorCode);
    }

    public NoticeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, ErrorCode errorCode) {
        super(message, cause, enableSuppression, writableStackTrace, errorCode);
    }
}
