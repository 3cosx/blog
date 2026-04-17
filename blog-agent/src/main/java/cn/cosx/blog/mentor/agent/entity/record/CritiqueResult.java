package cn.cosx.blog.mentor.agent.entity.record;

/**
 * 批评结果记录
 */
public record CritiqueResult(
        boolean passed,
        String feedback
) {
}
