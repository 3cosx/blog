package cn.cosx.blog.mentor.agent.document.job;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * xxl-job 配置类
 */
@Configuration
@Slf4j
@ConditionalOnProperty(prefix = "xxl.job", name = "enabled", havingValue = "true")
public class XxlJobConfig {

    @Value("${xxl.job.admin.addresses}")
    private String adminAddresses;

    @Value("${xxl.job.accessToken}")
    private String accessToken;

    @Value("${xxl.job.executor.appName}")
    private String appName;


    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        log.info("========================================");
        log.info("开始初始化 xxl-job 执行器...");
        log.info("Admin 地址: {}", adminAddresses);
        log.info("AccessToken: {}", accessToken);
        log.info("执行器名称: {}", appName);
        
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(adminAddresses);
        xxlJobSpringExecutor.setAccessToken(accessToken);
        xxlJobSpringExecutor.setAppname(appName);

        log.info("========================================");
        return xxlJobSpringExecutor;
    }
}
