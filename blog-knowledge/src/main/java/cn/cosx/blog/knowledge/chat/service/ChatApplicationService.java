package cn.cosx.blog.knowledge.chat.service;

import cn.cosx.blog.knowledge.ai.service.KnowEngineService;
import cn.cosx.blog.knowledge.ai.service.PromptService;
import cn.cosx.blog.knowledge.chat.entity.ChatParam;
import cn.cosx.blog.knowledge.rag.modules.*;
import dev.langchain4j.community.rag.content.retriever.neo4j.Neo4jGraph;
import dev.langchain4j.community.rag.content.retriever.neo4j.Neo4jText2CypherRetriever;
import dev.langchain4j.experimental.rag.content.retriever.sql.SqlDatabaseContentRetriever;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.scoring.onnx.OnnxScoringModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.aggregator.ReRankingContentAggregator;
import dev.langchain4j.rag.content.injector.DefaultContentInjector;
import dev.langchain4j.rag.content.retriever.elasticsearch.ElasticsearchContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchConfigurationFullText;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchConfigurationKnn;
import org.elasticsearch.client.RestClient;
import org.neo4j.driver.Driver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import javax.sql.DataSource;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Service
public class ChatApplicationService {


    @Autowired
    private ChatModel chatModel;

    @Autowired
    private StreamingChatModel streamingChatModel;

    @Autowired
    private RestClient restClient;

    @Autowired
    private OpenAiEmbeddingModel openAiEmbeddingModel;

    @Autowired
    private Driver neo4jDriver;
    public static final String INDEX_NAME = "know-engine-vector";

    @Autowired
    private DataSource dataSource;
    @Autowired
    private PromptService promptService;
    @Autowired
    private ChatMessageService chatMessageService;

    public Flux<String> chat(ChatParam chatParam) {
        return chat(chatParam, null);
    }


    public Flux<String> chat(ChatParam chatParam, Consumer<String> progressCallback) {

        return Flux.<String>create(sink -> {
                    // 进度回调：同时写入 sink 和外部回调
                    Consumer<String> callback = msg -> {
                        sink.next(msg);
                        if (progressCallback != null) {
                            progressCallback.accept(msg);
                        }
                    };

                    String assistantMessageId = chatMessageService.saveAssisantMessage(chatParam.conversationId(),"");
                    //查询改写
                    KnowEngineQueryTransformer knowEngineQueryTransformer =
                            new KnowEngineQueryTransformer(chatModel, assistantMessageId,callback);


                    //路由
//        KnowEngineElasticsearchContentRetriever embeddingContentRetriever = KnowEngineElasticsearchContentRetriever.builder()
//                .configuration(ElasticsearchConfigurationKnn.builder().build())
//                .embeddingModel(openAiEmbeddingModel)
//                .indexName(INDEX_NAME)
//                .restClient(restClient)
//                .maxResults(5)
//                .minScore(0.5)
//                .build();

                    KnowledgeProcessAwareContentRetriever knowledgeProcessAwareEmbeddingContentRetriever = new KnowledgeProcessAwareContentRetriever(
                            KnowEngineElasticsearchContentRetriever.builder()
                                    .configuration(ElasticsearchConfigurationKnn.builder().build())
                                    .embeddingModel(openAiEmbeddingModel)
                                    .indexName(INDEX_NAME)
                                    .restClient(restClient)
                                    .maxResults(5)
                                    .minScore(0.5)
                                    .build(),callback
                    );

//        ElasticsearchContentRetriever.builder().configuration(ElasticsearchConfigurationFullText.builder().build())
//                .indexName(INDEX_NAME)
//                .restClient(restClient)
//                .maxResults(5)
//                .build();
                    KnowledgeProcessAwareContentRetriever knowledgeProcessAwareFullTextContentRetriever = new KnowledgeProcessAwareContentRetriever(
                            ElasticsearchContentRetriever.builder().configuration(ElasticsearchConfigurationFullText.builder().build())
                                    .indexName(INDEX_NAME)
                                    .restClient(restClient)
                                    .maxResults(5)
                                    .build(), callback
                    );
//        SqlDatabaseContentRetriever sqlDatabaseContentRetriever = SqlDatabaseContentRetriever.builder().dataSource(dataSource)
//                //todo
//                .promptTemplate(new PromptTemplate("textToSqlPrompt.getContentAsString(UTF_8)"))
//                .databaseStructure("tablesSql.getContentAsString(UTF_8)")
//                .chatModel(chatModel)
//                .build();
                    KnowledgeProcessAwareContentRetriever processAwareSqlDatabaseContentRetriever = new KnowledgeProcessAwareContentRetriever(
                            SqlDatabaseContentRetriever.builder().dataSource(dataSource)
                                    //todo
                                    .promptTemplate(new PromptTemplate("textToSqlPrompt.getContentAsString(UTF_8)"))
                                    .databaseStructure("tablesSql.getContentAsString(UTF_8)")
                                    .chatModel(chatModel)
                                    .build(), callback
                    );
//        Neo4jText2CypherRetriever neo4jText2CypherRetriever = Neo4jText2CypherRetriever.builder().graph(Neo4jGraph.builder().driver(neo4jDriver).build())
//                .chatModel(chatModel).build();

                    KnowledgeProcessAwareContentRetriever processAwareNeo4jText2CypherRetriever = new KnowledgeProcessAwareContentRetriever(
                            Neo4jText2CypherRetriever.builder().graph(Neo4jGraph.builder().driver(neo4jDriver).build())
                                    .chatModel(chatModel).build(), callback

                    );
                    KnowEngineQueryRouter knowEngineQueryRouter = new KnowEngineQueryRouter(List.of(knowledgeProcessAwareEmbeddingContentRetriever,knowledgeProcessAwareFullTextContentRetriever,
                            processAwareSqlDatabaseContentRetriever, processAwareNeo4jText2CypherRetriever), chatModel,callback);

                    //排序聚合

                    OnnxScoringModel oonxScoringModel = BgeScoringModel.getInstance();
//                    ReRankingContentAggregator reRankingContentAggregator = ReRankingContentAggregator.builder()
//                            .scoringModel(oonxScoringModel)
//                            .querySelector(queryContents -> queryContents.keySet().iterator().next())
//                            .build();
                    KnowledgeProcessAwareContentAggregator knowledgeProcessAwareContentAggregator = new KnowledgeProcessAwareContentAggregator(ReRankingContentAggregator.builder()
                                    .scoringModel(oonxScoringModel)
                                    .querySelector(queryContents -> queryContents.keySet().iterator().next())
                                    .build(), callback,assistantMessageId,chatMessageService);

                    //模板注入
                    String prompt = promptService.getPrompt(chatParam.intentRecognitionResult());
                    DefaultContentInjector contentInjector = new DefaultContentInjector(PromptTemplate.from(prompt));

                    RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                            .queryTransformer(knowEngineQueryTransformer)
                            .queryRouter(knowEngineQueryRouter)
                            .contentAggregator(knowledgeProcessAwareContentAggregator)
                            .contentInjector(contentInjector)
                            .build();
                    KnowEngineService knowEngineService = AiServices.builder(KnowEngineService.class)
                            .chatModel(chatModel)
                            .streamingChatModel(streamingChatModel)
                            .retrievalAugmentor(retrievalAugmentor)
                            .chatMemoryProvider(conversationId-> MessageWindowChatMemory.withMaxMessages(10))
                            .build();

                            // 订阅 LLM 流式输出，桥接到 sink
                    AtomicBoolean firstToken = new AtomicBoolean(true);
                    StringBuilder finalAnswer = new StringBuilder();
                    Disposable disposable = knowEngineService.chatStream(chatParam.conversationId(), chatParam.question())
                            .doOnNext(chunk ->{
                                if (firstToken.compareAndSet(true, false)) {

                                }
                                finalAnswer.append(chunk);
                            })
                            .doOnComplete(() -> chatMessageService.updateAssistantMessage(assistantMessageId, finalAnswer.toString()))
                            .subscribe();
                    sink.onCancel(disposable::dispose);
                        })

                .subscribeOn(Schedulers.boundedElastic())
                .publishOn(Schedulers.parallel());
    }
}
