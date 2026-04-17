package cn.cosx.blog.mentor.agent.entity.record;

/**
 * 搜索结果记录
 */
public record SearchResult(
        String url,
        String title,
        String content
) {
}
