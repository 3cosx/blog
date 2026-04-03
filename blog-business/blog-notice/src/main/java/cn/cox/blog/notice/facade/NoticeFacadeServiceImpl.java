package cn.cox.blog.notice.facade;

import cn.cosx.blog.api.notice.constant.CaptchaConstants;
import cn.cosx.blog.api.notice.service.NoticeFacadeService;
import cn.cosx.blog.base.exception.ErrorCode;
import cn.cosx.blog.base.response.Response;
import cn.cosx.blog.sms.service.SmsService;
import cn.cox.blog.notice.exception.NoticeErrorCodeEnum;
import cn.hutool.core.util.RandomUtil;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

@DubboService(version = "1.0.0")
public class NoticeFacadeServiceImpl implements NoticeFacadeService {

    @Resource
    private SmsService smsService;

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public Response<String> sendAndGetCaptcha(String email) {
        String captcha = generateCaptcha();

        //保存验证码到Redis
        redisTemplate.opsForValue().set(CaptchaConstants.CAPTCHA_KEY, captcha, 5, TimeUnit.MINUTES);

        boolean result = smsService.sendCaptcha(captcha);
        //todo 发送成功就保存
        if(result){

        }else {
            return Response.fail(NoticeErrorCodeEnum.NOTICE_FAIL_SEND.getCode(),NoticeErrorCodeEnum.NOTICE_FAIL_SEND.getMessage());
        }
        return Response.of(captcha);
    }

    private String generateCaptcha() {
        return RandomUtil.randomString(4);
    }
}
