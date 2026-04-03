package cn.cosx.blog.auth;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author hollis
 */
@SpringBootApplication(scanBasePackages = {"cn.cosx.blog.auth"})
@EnableDubbo
public class BlogAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogAuthApplication.class, args);
    }

}
