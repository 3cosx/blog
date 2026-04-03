package cn.cosx.blog;

import cn.cosx.blog.api.notice.service.NoticeFacadeService;
import cn.cosx.blog.api.product.service.ProductFacadeService;
import cn.cosx.blog.api.user.service.UserFacadeService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableDubbo
public class BusinessDubboConfiguration {


    @DubboReference(version = "1.0.0")
    private UserFacadeService userFacadeService;

    @DubboReference(version = "1.0.0")
    private NoticeFacadeService noticeFacadeService;

    @DubboReference(version = "1.0.0")
    private ProductFacadeService productFacadeService;
    @Bean
    @ConditionalOnMissingBean(name = "userFacadeService")
    public UserFacadeService userFacadeService(){
        return userFacadeService;
    }

    @Bean
    @ConditionalOnMissingBean(name = "noticeFacadeService")
    public NoticeFacadeService noticeFacadeService(){
        return noticeFacadeService;
    }
}
