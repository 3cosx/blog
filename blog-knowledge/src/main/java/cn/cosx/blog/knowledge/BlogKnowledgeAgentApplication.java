package cn.cosx.blog.knowledge;

import cn.cosx.blog.knowledge.document.service.KnowledgeSegmentService;
import cn.cosx.blog.knowledge.rag.modules.KnowEngineQueryTransformer;
import dev.langchain4j.community.neo4j.spring.Neo4jEmbeddingStoreAutoConfiguration;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication
public class BlogKnowledgeAgentApplication implements ApplicationContextAware {

	public static void main(String[] args) {
		SpringApplication.run(BlogKnowledgeAgentApplication.class, args);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		KnowEngineQueryTransformer.setApplicationContext(applicationContext);
	}
}
