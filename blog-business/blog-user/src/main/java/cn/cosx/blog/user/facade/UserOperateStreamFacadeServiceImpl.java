package cn.cosx.blog.user.facade;

import cn.cosx.blog.api.user.request.UserQueryRequest;
import cn.cosx.blog.api.user.request.UserUpdateRequest;
import cn.cosx.blog.api.user.service.UserOperateStreamFacadeService;
import cn.cosx.blog.api.user.vo.UserInfo;
import cn.cosx.blog.base.response.Response;

import org.apache.dubbo.config.annotation.DubboService;

@DubboService(version = "1.0.0")
public class UserOperateStreamFacadeServiceImpl implements UserOperateStreamFacadeService {

    @Override
    public Response<UserInfo> queryUserById(UserQueryRequest request) {
        return null;
    }

    @Override
    public Response<Boolean> updateUser(UserUpdateRequest request) {
        return null;
    }
}
