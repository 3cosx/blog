package cn.cosx.blog.knowledge.rag.modules;

import cn.cosx.blog.knowledge.chat.entity.ChatMessage;
import cn.cosx.blog.knowledge.chat.service.ChatMessageService;
import com.alibaba.fastjson2.JSON;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.aggregator.ContentAggregator;
import dev.langchain4j.rag.query.Query;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.ApplicationContext;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static cn.cosx.blog.knowledge.rag.constant.MetadataKeyConstant.*;
import static dev.langchain4j.rag.content.ContentMetadata.RERANKED_SCORE;

@Slf4j
public class KnowledgeProcessAwareContentAggregator implements ContentAggregator {

    private final ContentAggregator contentAggregator;

    private final Consumer<String> progressCallback;

    private final String messageId;

    private final ChatMessageService chatMessageService;


    public KnowledgeProcessAwareContentAggregator(ContentAggregator contentAggregator, Consumer<String> progressCallback,
                                                  String messageId, ChatMessageService chatMessageService) {
        this.contentAggregator = contentAggregator;
        this.progressCallback = progressCallback;
        this.messageId = messageId;
        this.chatMessageService = chatMessageService;

    }


    @Override
    public List<Content> aggregate(Map<Query, Collection<List<Content>>> queryToContents) {

        // 发送进度：开始重排序/聚合
        if (progressCallback != null) {
            progressCallback.accept("[PROGRESS]:正在排序筛选结果...");
            System.out.println("[PROGRESS]:正在排序筛选结果...");
        }
        List<Content> aggregates = contentAggregator.aggregate(queryToContents);

        try {
            List<ChatMessage.RagReference> ragReferences = aggregates.stream()
                    .collect(Collectors.toMap(
                            content -> content.textSegment().metadata().getInteger(DOC_ID),
                            content -> content,
                            (existing, replacement) -> existing
                    )).values().stream().map(content -> {
                        ChatMessage.RagReference reference = new ChatMessage.RagReference();
                        reference.setDocumentId(content.textSegment().metadata().getInteger(DOC_ID) + "");
                        reference.setChunkId(content.textSegment().metadata().getString(CHUNK_ID));
                        reference.setUrl(content.textSegment().metadata().getString(URL));
                        reference.setDocumentTitle(content.textSegment().metadata().getString(FILE_NAME));
                        reference.setChunkContent(content.textSegment().text());
                        reference.setRerankScore((double) content.metadata().get(RERANKED_SCORE));
                        return reference;
                    }).collect(Collectors.toList());

            if(CollectionUtils.isNotEmpty(ragReferences)){
                log.info("RAG引用信息回写: assistantMsgId={}, references={}", messageId, JSON.toJSONString(ragReferences));
                chatMessageService.updateRagReferences(messageId, ragReferences);
            }


            if(progressCallback != null){
                progressCallback.accept("[REFERENCE]:" + JSON.toJSONString(ragReferences));
            }
        } catch (Exception e) {
            log.warn("RAG引用信息回写失败: assistantMsgId={}", messageId, e);
        }
        // 发送进度：聚合完成，即将进入LLM生成
        if (progressCallback != null) {
            progressCallback.accept("[PROGRESS]:正在生成回答...");
            System.out.println("[PROGRESS]:正在生成回答...");
        }
        return List.of();
    }


}
