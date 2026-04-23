package cn.cosx.blog.mentor.agent.service;

import cn.cosx.blog.mentor.agent.document.entity.KnowledgeSegment;
import cn.cosx.blog.mentor.agent.document.rag.constant.MetadataKeyConstant;
import cn.cosx.blog.mentor.agent.document.service.KnowledgeSegmentService;
import cn.cosx.blog.mentor.agent.utils.DynamicPgVectorStoreFactory;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.expansion.QueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EmbeddingService {
    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private DynamicPgVectorStoreFactory pgVectorStoreFactory;

    @Autowired
    private ChatModel chatModel;

    @Autowired
    private KnowledgeSegmentService segmentService;


    private PgVectorStore vectorStore;

    private static final int EMBEDDING_BATCH_SIZE = 9;

    @PostConstruct
    public void init(){
        vectorStore = pgVectorStoreFactory.createPgVectorStore("vector_file_info");
    }

    /**
     * 向量化
     */
    public List<float[]> embed(List<Document> documents) {
        return documents.stream().map(document -> embeddingModel.embed(document.getText())).collect(Collectors.toList());
    }

    /**
     * 存储向量库
     */
    public void embedAndStore(List<Document> documents) {
        for (int i = 0; i < documents.size(); i += EMBEDDING_BATCH_SIZE) {
            List<Document> batches = documents.subList(i, Math.min(i + EMBEDDING_BATCH_SIZE, documents.size()));
            vectorStore.doAdd(batches);
        }
    }

    /**
     * RAG 检索 - 根据文件ID和问题检索相关文档
     *
     * @param documentId   文件ID
     * @param question 用户问题
     * @return 相关文档内容列表
     */
    public List<String> ragRetrieve(String documentId, String question) {
        log.info("RAG 检索开始: fileId={}, question={}", documentId, question);

        if (StringUtils.isBlank(documentId) || StringUtils.isBlank(question)) {
            log.warn("RAG 检索参数为空: fileId={}, question={}", documentId, question);
            return Collections.singletonList("检索参数不能为空");
        }

        try {
            Query query = Query.builder().text(question).build();

            // 1. 问题压缩重写
            ChatClient chatClient = ChatClient.builder(chatModel).build();
            CompressionQueryTransformer queryTransformer = CompressionQueryTransformer.builder()
                    .chatClientBuilder(chatClient.mutate())
                    .build();

            Query compressed = queryTransformer.transform(query);
            log.info("压缩重写后的Query: {}", compressed.text());

            // 2. 问题扩展
            QueryExpander queryExpander = MultiQueryExpander.builder()
                    .chatClientBuilder(chatClient.mutate())
                    .numberOfQueries(3)
                    .includeOriginal(true)
                    .build();

            List<Query> expandedQueries = queryExpander.expand(compressed);
            log.info("扩展后的Query：{}", expandedQueries);

            // 3. 语义向量检索 - 使用 documentId 过滤
            List<Document> allDocs = new ArrayList<>();
            Set<String> seenIds = new HashSet<>();

            FilterExpressionBuilder builder = new FilterExpressionBuilder();
            Filter.Expression filter = builder.eq("documentId", documentId).build();

            for (Query eq : expandedQueries) {
                List<Document> docs = vectorStore.similaritySearch(
                        SearchRequest.builder()
                                .query(eq.text())
                                .topK(5)
                                .filterExpression(filter)
                                .build());

                for (Document doc : docs) {
                    if (seenIds.add(doc.getId())) {
                        allDocs.add(doc);
                    }
                }
            }

            // 4. 收集所有需要查询的 parentChunkId，统一查询数据库
            Set<String> parentChunkIds = new HashSet<>();
            for (Document doc : allDocs) {
                if (doc.getMetadata().containsKey(MetadataKeyConstant.PARENT_CHUNK_ID)) {
                    parentChunkIds.add(String.valueOf(doc.getMetadata().get(MetadataKeyConstant.PARENT_CHUNK_ID)));
                }
            }

            // 5. 批量查询父分段
            Map<String, String> parentChunkTextMap = new HashMap<>();
            if (!parentChunkIds.isEmpty()) {
                log.info("批量查询父分段: parentChunkIds={}", parentChunkIds);
                List<KnowledgeSegment> parentSegments = segmentService.lambdaQuery()
                        .in(KnowledgeSegment::getChunkId, parentChunkIds)
                        .list();
                for (KnowledgeSegment segment : parentSegments) {
                    parentChunkTextMap.put(segment.getChunkId(), segment.getText());
                }
                log.info("父分段批量查询完成: 查询数量={}, 返回数量={}", parentChunkIds.size(), parentChunkTextMap.size());
            }

            // 6. 构建返回结果，如果有parentChunkId则使用父分段内容，同一父分段只返回一次
            List<String> results = new ArrayList<>();
            Set<String> returnedParentChunkIds = new HashSet<>();
            for (Document doc : allDocs) {
                String text = doc.getText();
                if (doc.getMetadata().containsKey(MetadataKeyConstant.PARENT_CHUNK_ID)) {
                    String parentChunkId = String.valueOf(doc.getMetadata().get(MetadataKeyConstant.PARENT_CHUNK_ID));
                    // 同一父分段只返回一次，避免重复
                    if (returnedParentChunkIds.contains(parentChunkId)) {
                        log.debug("跳过重复父分段: parentChunkId={}", parentChunkId);
                        continue;
                    }
                    String parentText = parentChunkTextMap.get(parentChunkId);
                    if (StringUtils.isNotBlank(parentText)) {
                        text = parentText;
                        returnedParentChunkIds.add(parentChunkId);
                        log.debug("使用父分段内容: parentChunkId={}", parentChunkId);
                    }
                }
                results.add(text);
            }

            log.info("RAG 检索完成: fileId={}, 返回结果数={}", documentId, results.size());
            return results;

        } catch (Exception e) {
            log.error("RAG 检索失败: fileId={}, question={}", documentId, question, e);
            return Collections.singletonList("RAG 检索失败: " + e.getMessage());
        }
    }
}
