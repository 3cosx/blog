package cn.cosx.blog.knowledge;

import dev.langchain4j.community.neo4j.spring.Neo4jEmbeddingStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication(exclude = {Neo4jEmbeddingStoreAutoConfiguration.class})
public class BlogKnowledgeAgentApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlogKnowledgeAgentApplication.class, args);
	}

}
