package cn.cosx.blog.knowledge.document.service.impl;

import cn.cosx.blog.knowledge.common.Page;
import cn.cosx.blog.knowledge.document.entity.KnowledgeSegment;
import cn.cosx.blog.knowledge.document.enums.SegmentStatus;
import cn.cosx.blog.knowledge.document.mapper.KnowledgeSegmentMapper;
import cn.cosx.blog.knowledge.document.service.IKnowledgeSegmentService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.List;

/**
 * 文档分片Service实现类
 * 只负责对knowledge_segment表的CRUD操作
 */
@Service
public class KnowledgeSegmentServiceImpl extends ServiceImpl<KnowledgeSegmentMapper, KnowledgeSegment> implements IKnowledgeSegmentService {


    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public String getTextByChunkId(Serializable chunkId) {
        //todo
        String text = stringRedisTemplate.opsForValue().get(chunkId);
        if (StringUtils.hasText(text)) {
            return text;
        }

        KnowledgeSegment segment = super.getById(chunkId);

        if (segment != null) {
            stringRedisTemplate.opsForValue().set(chunkId.toString(), segment.getText());
            return segment.getText();
        } else {
            // 缓存空值，避免缓存击穿，重复查询数据库
            stringRedisTemplate.opsForValue().set(chunkId.toString(), "");
        }

        return null;
    }
    @Override
    @Transactional
    public boolean saveSegments(List<KnowledgeSegment> segments) {
        return this.saveBatch(segments);
    }



    @Override
    public KnowledgeSegment getById(Long segmentId) {
        return this.getById(segmentId);
    }



    @Override
    public List<KnowledgeSegment> listByDocumentIdAndNotVectorStoredWithCursor(Long documentId, Page page) {
        LambdaQueryWrapper<KnowledgeSegment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeSegment::getDocumentId, documentId);
        wrapper.ne(KnowledgeSegment::getStatus, SegmentStatus.VECTOR_STORED);

        if (!page.isFirst()) {
            wrapper.lt(KnowledgeSegment::getId, page.getLastId());
        }

        wrapper.orderByAsc(KnowledgeSegment::getId);
        wrapper.last("LIMIT " + (page.getPageSize() + 1));

        List<KnowledgeSegment> segments = this.list(wrapper);

        if (segments.size() > page.getPageSize()) {
            page.setHasNext(true);
            segments = segments.subList(0, page.getPageSize());
        } else {
            page.setHasNext(false);
        }

        if (!segments.isEmpty()) {
            page.setLastId(segments.get(segments.size() - 1).getId());
        }

        return segments;
    }

    @Override
    public boolean batchUpdateStatus(List<Long> segmentIds, SegmentStatus status) {
        if (segmentIds == null || segmentIds.isEmpty()) {
            return false;
        }
        return this.update(null, new LambdaUpdateWrapper<KnowledgeSegment>()
                .in(KnowledgeSegment::getId, segmentIds)
                .set(KnowledgeSegment::getStatus, status));
    }

    @Override
    public boolean batchUpdateEmbeddingIdAndStatus(List<Long> segmentIds, List<String> embeddingIds, SegmentStatus status) {
        if (segmentIds == null || segmentIds.isEmpty() || embeddingIds == null || embeddingIds.isEmpty()) {
            return false;
        }
        if (segmentIds.size() != embeddingIds.size()) {
            throw new IllegalArgumentException("segmentIds和embeddingIds数量不一致");
        }
        for (int i = 0; i < segmentIds.size(); i++) {
            String embeddingId = embeddingIds.get(i);
            if (embeddingId == null) {
                continue;
            }
            this.update(null, new LambdaUpdateWrapper<KnowledgeSegment>()
                    .eq(KnowledgeSegment::getId, segmentIds.get(i))
                    .set(KnowledgeSegment::getEmbeddingId, embeddingId)
                    .set(KnowledgeSegment::getStatus, status));
        }
        return true;
    }

    @Override
    public List<Long> listDocumentIdsWithUnvectorizedSegments() {
        // 查询状态不等于VECTOR_STORED的segment，按documentId分组
        LambdaQueryWrapper<KnowledgeSegment> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(KnowledgeSegment::getStatus, SegmentStatus.VECTOR_STORED);
        wrapper.select(KnowledgeSegment::getDocumentId);
        wrapper.groupBy(KnowledgeSegment::getDocumentId);

        List<KnowledgeSegment> segments = this.list(wrapper);
        return segments.stream().map(KnowledgeSegment::getDocumentId).distinct().toList();
    }
}
