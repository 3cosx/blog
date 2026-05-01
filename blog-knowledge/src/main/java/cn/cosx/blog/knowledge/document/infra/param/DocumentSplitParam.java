package cn.cosx.blog.knowledge.document.infra.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSplitParam {

    private Long docId;

    private String splitType;

    @Builder.Default
    private Integer chunkSize = 500;

    @Builder.Default
    private Integer overlapSize = 50;

    private Integer headerLevel;

    private String regex;

    private String separator;

}
