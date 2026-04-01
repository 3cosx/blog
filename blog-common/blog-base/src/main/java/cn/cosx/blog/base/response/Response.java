package cn.cosx.blog.base.response;

import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class Response<T> extends BaseResponse{
    private static final long serialVersionUID = 1L;

    private T data;

    public static <T> Response<T> of(T data) {
        Response<T> response = new Response<>();
        response.setSuccess(true);
        response.setData(data);
        return response;
    }

    public static <T> Response<T> fail(String errorCode, String errorMessage) {
        Response<T> response = new Response<>();
        response.setSuccess(false);
        response.setResponseCode(errorCode);
        response.setResponseMessage(errorMessage);
        return response;
    }
}
