package cn.cosx.blog.mentor.agent.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务线程池配置
 * 用于优化文档切分和向量存储的异步事件处理
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncTaskConfig {

    /**
     * 文档处理线程池
     * 用于处理文档切分和向量存储等耗时操作
     */
    @Bean(name = "documentTaskExecutor")
    public Executor documentTaskExecutor() {
        log.info("开始创建文档处理线程池...");
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数：CPU核心数 * 2
        int corePoolSize = Runtime.getRuntime().availableProcessors() * 2;
        executor.setCorePoolSize(corePoolSize);
        
        // 最大线程数：CPU核心数 * 4
        int maxPoolSize = Runtime.getRuntime().availableProcessors() * 4;
        executor.setMaxPoolSize(maxPoolSize);
        
        // 队列容量：最多缓存100个待处理任务
        executor.setQueueCapacity(100);
        
        // 线程空闲时间：60秒后回收多余线程
        executor.setKeepAliveSeconds(60);
        
        // 线程名称前缀：便于日志追踪
        executor.setThreadNamePrefix("doc-task-");
        
        // 拒绝策略：由调用线程处理，避免任务丢失
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 等待时间：最多等待60秒
        executor.setAwaitTerminationSeconds(60);
        
        // 初始化线程池
        executor.initialize();
        
        log.info("文档处理线程池创建完成 - corePoolSize: {}, maxPoolSize: {}, queueCapacity: {}", 
                corePoolSize, maxPoolSize, executor.getQueueCapacity());
        
        return executor;
    }

    /**
     * 向量存储专用线程池
     * 用于处理embedding生成和向量存储等CPU密集型操作
     */
    @Bean(name = "vectorStoreTaskExecutor")
    public Executor vectorStoreTaskExecutor() {
        log.info("开始创建向量存储线程池...");
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数：CPU核心数（embedding生成是CPU密集型）
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(corePoolSize);
        
        // 最大线程数：CPU核心数 * 2
        int maxPoolSize = Runtime.getRuntime().availableProcessors() * 2;
        executor.setMaxPoolSize(maxPoolSize);
        
        // 队列容量：最多缓存50个待处理任务
        executor.setQueueCapacity(50);
        
        // 线程空闲时间：120秒后回收多余线程
        executor.setKeepAliveSeconds(120);
        
        // 线程名称前缀：便于日志追踪
        executor.setThreadNamePrefix("vector-store-");
        
        // 拒绝策略：由调用线程处理，避免任务丢失
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 等待时间：最多等待120秒
        executor.setAwaitTerminationSeconds(120);
        
        // 初始化线程池
        executor.initialize();
        
        log.info("向量存储线程池创建完成 - corePoolSize: {}, maxPoolSize: {}, queueCapacity: {}", 
                corePoolSize, maxPoolSize, executor.getQueueCapacity());
        
        return executor;
    }
}
