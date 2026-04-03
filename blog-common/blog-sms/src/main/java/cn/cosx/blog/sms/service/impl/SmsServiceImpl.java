package cn.cosx.blog.sms.service.impl;

import cn.cosx.blog.sms.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
public class SmsServiceImpl implements SmsService {


    @Override
    public boolean sendCaptcha(String captcha) {
        return true;
    }
}
