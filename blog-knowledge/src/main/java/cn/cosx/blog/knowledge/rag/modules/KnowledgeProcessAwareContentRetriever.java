package cn.cosx.blog.knowledge.rag.modules;

import dev.langchain4j.community.rag.content.retriever.neo4j.Neo4jText2CypherRetriever;
import dev.langchain4j.experimental.rag.content.retriever.sql.SqlDatabaseContentRetriever;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.elasticsearch.AbstractElasticsearchEmbeddingStore;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class KnowledgeProcessAwareContentRetriever implements ContentRetriever {

    private final ContentRetriever retriever;

    private final Consumer<String> progressCallback;
    /**
     * 确保路由进度只发送一次
     */
    private final AtomicBoolean embeddingProgressSent = new AtomicBoolean(false);
    private final AtomicBoolean sqlProgressSent = new AtomicBoolean(false);
    private final AtomicBoolean neo4jProgressSent = new AtomicBoolean(false);

    public KnowledgeProcessAwareContentRetriever(ContentRetriever retriever, Consumer<String> progressCallback) {
        this.retriever = retriever;
        this.progressCallback = progressCallback;
    }

    @Override
    public List<Content> retrieve(Query query) {
        if(progressCallback != null){

            switch(retriever){
                case SqlDatabaseContentRetriever sqlDatabaseContentRetriever ->{
                    if(sqlProgressSent.compareAndSet(false, true)){
                        progressCallback.accept("[PROGRESS]:正在从SQL数据库中检索内容...");
                        System.out.println("[PROGRESS]:正在从SQL数据库中检索内容...");
                    }
                }
                case AbstractElasticsearchEmbeddingStore abstractElasticsearchEmbeddingStore ->{
                    if(embeddingProgressSent.compareAndSet(false, true)){
                        progressCallback.accept("[PROGRESS]:正在从Elasticsearch中检索内容...");
                        System.out.println("[PROGRESS]:正在从Elasticsearch中检索内容...");
                    }
                }
                case Neo4jText2CypherRetriever neo4jText2CypherRetriever ->{
                    if(neo4jProgressSent.compareAndSet(false, true)){
                        progressCallback.accept("[PROGRESS]:正在从Neo4j中检索内容...");
                        System.out.println("[PROGRESS]:正在从Neo4j中检索内容...");
                    }
                }
                case null,default -> {
                    if (embeddingProgressSent.compareAndSet(false, true)) {
                        progressCallback.accept("[PROGRESS]:正在检索文档内容...");
                        System.out.println("[PROGRESS]:正在检索文档内容...");
                    }
                }                }
            }
        return retriever.retrieve(query);
        }


        public ContentRetriever getRetriever() {
            return retriever;
        }


}
