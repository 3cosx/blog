package cn.cosx.blog.knowledge.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务线程池配置
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncTaskConfig {

    @Bean(name = "documentTaskExecutor")
    public Executor documentTaskExecutor() {
        log.info("创建文档处理线程池...");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int corePoolSize = Runtime.getRuntime().availableProcessors() * 2;
        executor.setCorePoolSize(corePoolSize);
        int maxPoolSize = Runtime.getRuntime().availableProcessors() * 4;
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("doc-task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();

        log.info("文档处理线程池创建完成 - corePoolSize: {}, maxPoolSize: {}", corePoolSize, maxPoolSize);
        return executor;
    }
}
