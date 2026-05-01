package cn.cosx.blog.sms.config;

import cn.cosx.blog.sms.service.SmsService;
import cn.cosx.blog.sms.service.impl.SmsServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SmsConfiguration {

    @Bean
    public SmsService smsService() {
        return new SmsServiceImpl();
    }
}
