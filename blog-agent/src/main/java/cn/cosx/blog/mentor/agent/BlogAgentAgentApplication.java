package cn.cosx.blog.mentor.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class BlogAgentAgentApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlogAgentAgentApplication.class, args);
	}

}
