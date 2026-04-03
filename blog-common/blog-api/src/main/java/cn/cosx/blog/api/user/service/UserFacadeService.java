package cn.cosx.blog.api.user.service;

import cn.cosx.blog.api.user.request.UserQueryRequest;
import cn.cosx.blog.api.user.request.UserRegisterRequest;
import cn.cosx.blog.api.user.request.UserUpdateRequest;
import cn.cosx.blog.api.user.response.UserQueryResponse;
import cn.cosx.blog.api.user.vo.UserInfo;
import cn.cosx.blog.base.response.Response;

/**
 * 用户服务 Dubbo 接口
 *
 * @author cosx
 */
public interface UserFacadeService {

    /**
     * 根据用户ID查询用户信息
     *
     * @param request 用户查询请求
     * @return 用户信息响应
     */
    Response<UserInfo> queryUserById(UserQueryRequest request);

    /**
     * 根据手机号查询用户信息
     *
     * @param request 用户查询请求
     * @return 用户信息响应
     */
    Response<UserInfo> queryUserByTelephone(UserQueryRequest request);

    Response<UserInfo> queryUserByEmail(UserQueryRequest request);

    /**
     * 更新用户信息
     *
     * @param request 用户更新请求
     * @return 更新结果
     */
    Response<Boolean> updateUser(UserUpdateRequest request);

    /**
     * 更新用户最后登录时间
     *
     * @param userId 用户ID
     * @return 更新结果
     */
    Response<Boolean> updateLastLoginTime(String userId);


    Response<UserInfo> register(UserRegisterRequest request);
}
