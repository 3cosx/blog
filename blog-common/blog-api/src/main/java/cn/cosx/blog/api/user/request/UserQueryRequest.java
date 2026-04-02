package cn.cosx.blog.api.user.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户查询请求
 *
 * @author cosx
 */
@Data
public class UserQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private String userId;
}
