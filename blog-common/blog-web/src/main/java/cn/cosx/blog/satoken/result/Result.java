package cn.cosx.blog.satoken.result;


import cn.cosx.blog.base.response.Response;
import lombok.Data;

import java.io.Serializable;

import static cn.cosx.blog.base.response.ResponseCode.SUCCESS;

/**
 * 统一响应结果
 */
@Data
public class Result<T> implements Serializable {
    /**
     * 状态吗
     */
    private String code;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 消息描述
     */
    private String message;

    /**
     * 数据，可以是任何类型的VO
     */
    private T data;

    public Result() {
    }

    public Result(Boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public Result(Boolean success, String code, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.code = code;
    }

    public Result(Response<T> singleResponse){
        this.success = singleResponse.getSuccess();
        this.data = singleResponse.getData();
        this.code = singleResponse.getResponseCode();
        this.message = singleResponse.getResponseMessage();
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(true, SUCCESS.name(), SUCCESS.name(), data);
    }

    public static <T> Result<T> error(String errorCode,String errorMsg) {
        return new Result<>(false, errorCode, errorMsg, null);
    }
}
