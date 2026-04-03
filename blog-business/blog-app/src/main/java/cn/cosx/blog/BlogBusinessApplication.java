package cn.cosx.blog;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class BlogBusinessApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogBusinessApplication.class, args);
    }
}
