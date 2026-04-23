package cn.cosx.blog.knowledge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class BlogKnowledgeAgentApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlogKnowledgeAgentApplication.class, args);
	}

}
