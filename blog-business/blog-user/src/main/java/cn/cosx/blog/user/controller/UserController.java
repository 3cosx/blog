package cn.cosx.blog.user.controller;

import cn.cosx.blog.api.user.vo.UserInfo;
import cn.cosx.blog.api.user.vo.UserProfileInfo;
import cn.cosx.blog.base.result.Result;
import cn.cosx.blog.user.converter.UserConverter;
import cn.cosx.blog.user.domain.entity.User;
import cn.cosx.blog.user.domain.entity.UserProfile;
import cn.cosx.blog.user.domain.service.UserProfileService;
import cn.cosx.blog.user.domain.service.UserService;
import cn.cosx.blog.user.infrastructure.exception.UserErrorCodeEnum;
import cn.cosx.blog.user.infrastructure.exception.UserException;
import cn.cosx.blog.user.param.UserAuthParam;
import cn.cosx.blog.user.param.UserModifyParam;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private UserProfileService userProfileService;


    @PostMapping("/getUserInfo")
    public Result<UserInfo> getUserInfo() {
        String userId =(String) StpUtil.getLoginId();
        User user = userService.findById(userId);
        if(user == null) {
            throw new UserException(UserErrorCodeEnum.USER_NOT_EXIST);
        }
        UserInfo userInfo = UserConverter.INSTANCE.user2UserInfo(user);
        return Result.success(userInfo);
    }


    @PostMapping("/modifyUserInfo")
    public Result<Boolean> modifyUserInfo(@RequestBody UserModifyParam param) {
        String userId =(String) StpUtil.getLoginId();
        User user = userService.findById(userId);
        if(user == null) {
            throw new UserException(UserErrorCodeEnum.USER_NOT_EXIST);
        }
        Boolean result = userService.modify(param);
        return Result.success(result);
    }

@PostMapping("/auth")
    public Result<Boolean> auth(@Valid @RequestBody UserAuthParam param) {
        String userId = (String) StpUtil.getLoginId();
        User user = userService.findById(userId);
        if (user == null) {
            throw new UserException(UserErrorCodeEnum.USER_NOT_EXIST);
        }
        Boolean result = userService.auth(userId, param);
        return Result.success(result);
    }

    @PostMapping("/getUserProfile")
    public Result<UserProfileInfo> getUserProfile(@RequestParam Long userId) {
        UserProfileInfo info = userProfileService.getUserProfileInfo(userId);
        return Result.success(info);
    }

    @PostMapping("/updateUserProfile")
    public Result<Boolean> updateUserProfile(@RequestBody UserProfile param) {
        String userId = (String) StpUtil.getLoginId();
        param.setUserId(Long.parseLong(userId));
        Boolean result = userProfileService.update(param);
        return Result.success(result);
    }
