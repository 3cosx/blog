package cn.cosx.blog.auth.controller;


import cn.cosx.blog.api.notice.constant.CaptchaConstants;
import cn.cosx.blog.api.notice.service.NoticeFacadeService;
import cn.cosx.blog.api.user.request.UserQueryRequest;
import cn.cosx.blog.api.user.request.UserRegisterRequest;
import cn.cosx.blog.api.user.service.UserFacadeService;
import cn.cosx.blog.api.user.vo.UserInfo;
import cn.cosx.blog.auth.exception.AuthException;
import cn.cosx.blog.auth.param.LoginParam;
import cn.cosx.blog.auth.param.NoticeParam;
import cn.cosx.blog.auth.vo.LoginVO;
import cn.cosx.blog.base.response.Response;
import cn.cosx.blog.base.result.Result;
import cn.cosx.blog.base.utils.RemoteCallWrapper;
import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.druid.util.StringUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

import static cn.cosx.blog.auth.constant.LoginParamConstants.DEFAULT_EXPIRE_TIME;
import static cn.cosx.blog.auth.exception.AuthErrorCode.VERIFICATION_CODE_WRONG;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    @Resource
    private RedisTemplate redisTemplate;

    @DubboReference(version = "1.0.0")
    private UserFacadeService userFacadeService;

    @DubboReference(version = "1.0.0")
    private NoticeFacadeService noticeFacadeService;

    @PostMapping("/sendAndGetCaptcha")
    public Result<Boolean> sendAndGetCaptcha(@Validated @RequestBody NoticeParam param) {
        String captcha = (String) redisTemplate.opsForValue().get(CaptchaConstants.CAPTCHA_KEY + param.getEmail());

        log.info("captcha: {}", captcha);
        if(!StringUtils.isEmpty(captcha)) {
            return Result.success(true);
        }
        Response<String> response = RemoteCallWrapper.call(req -> noticeFacadeService.sendAndGetCaptcha(param.getEmail()), param, "response");
        log.info("response: {}", response);

        if (!response.getSuccess()) {
            return Result.error(response.getResponseMessage());
        }
        return Result.success(true);
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Validated @RequestBody LoginParam param){

        //获取验证码
        String captcha = (String) redisTemplate.opsForValue().get(CaptchaConstants.CAPTCHA_KEY + param.getEmail());

        if (!StringUtils.equalsIgnoreCase(captcha, param.getCaptcha())) {
            throw new AuthException(VERIFICATION_CODE_WRONG);
        }
        UserQueryRequest userQueryRequest = new UserQueryRequest();
        userQueryRequest.setEmail(param.getEmail());
        Response<UserInfo> response = RemoteCallWrapper.call(req -> userFacadeService.queryUserByEmail(req), userQueryRequest, "response");
        UserInfo userInfo = response.getData();

        //判断是否需要注册
        if(userInfo == null ){
            UserRegisterRequest userRegisterRequest = new UserRegisterRequest();
            userRegisterRequest.setEmail(param.getEmail());

            Response<UserInfo> registerResponse = userFacadeService.register(userRegisterRequest);
            if(!response.getSuccess()){
                return Result.error("注册失败，重新尝试");
            }

            userInfo = registerResponse.getData();
        }
        //登录
        StpUtil.login(userInfo.getUserId(),
                new SaLoginModel().setIsLastingCookie(param.getRememberMe()).setTimeout(DEFAULT_EXPIRE_TIME));
        //保存信息到用户级Session（与StpInterfaceImpl读取的Session一致）
        StpUtil.getSessionByLoginId(userInfo.getUserId()).set(userInfo.getUserId(), userInfo);
        LoginVO loginVO = new LoginVO(userInfo);
        return Result.success(loginVO);

    }

    @PostMapping("/logout")
    public Result logout() {
        StpUtil.logout();
        return Result.success();
    }


}
