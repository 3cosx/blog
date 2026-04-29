package cn.cosx.blog.user.facade;

import cn.cosx.blog.api.user.request.UserQueryRequest;
import cn.cosx.blog.api.user.request.UserRegisterRequest;
import cn.cosx.blog.api.user.request.UserUpdateRequest;
import cn.cosx.blog.api.user.response.UserQueryResponse;
import cn.cosx.blog.api.user.service.UserFacadeService;
import cn.cosx.blog.api.user.vo.UserInfo;
import cn.cosx.blog.base.response.Response;
import cn.cosx.blog.user.converter.UserConverter;
import cn.cosx.blog.user.domain.entity.User;
import cn.cosx.blog.user.domain.service.UserService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;
import java.util.stream.Collectors;

@DubboService(version = "1.0.0")
public class UserFacadeServiceImpl implements UserFacadeService {

    @Resource
    private UserService userService;

    @Override
    public Response<UserInfo> queryUserById(UserQueryRequest request) {
        User user = userService
                .lambdaQuery()
                .eq(StringUtils.isNotBlank(request.getUserId()),User::getId, request.getUserId())
                .one();
        UserInfo userInfo = UserConverter.INSTANCE.user2UserInfo(user);
        return Response.of(userInfo);
    }

    @Override
    public Response<UserInfo> queryUserByTelephone(UserQueryRequest request) {
        User user = userService
                .lambdaQuery()
                .eq(StringUtils.isNotBlank(request.getPhone()),User::getTelephone, request.getPhone())
                .one();
        UserInfo userInfo = UserConverter.INSTANCE.user2UserInfo(user);
        return Response.of(userInfo);
    }

    @Override
    public Response<UserInfo> queryUserByEmail(UserQueryRequest request) {
        User user = userService
                .lambdaQuery()
                .eq(StringUtils.isNotBlank(request.getEmail()),User::getEmail, request.getEmail())
                .one();
        UserInfo userInfo = UserConverter.INSTANCE.user2UserInfo(user);
        return Response.of(userInfo);

    }

    public Response<UserInfo> register(UserRegisterRequest request){
        return userService.register(request.getEmail());
    }

    @Override
    public Response<Boolean> updateUser(UserUpdateRequest request) {

        return null;
    }

    @Override
    public Response<Boolean> updateLastLoginTime(String userId) {
        return null;
    }

    @Override
    public Response<List<UserInfo>> queryUserByIds(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Response.of(List.of());
        }

        List<User> users = userService.lambdaQuery()
                .in(User::getId, userIds)
                .list();

        List<UserInfo> userInfos = users.stream()
                .map(UserConverter.INSTANCE::user2UserInfo)
                .collect(Collectors.toList());

        return Response.of(userInfos);
    }
}
