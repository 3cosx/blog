package cn.cosx.blog.knowledge.common;

import lombok.Data;

@Data
public class Page {
    private int cur = 1;
    private int pageSize = 10;
    private Long lastId;
    private boolean hasNext;

    public Page() {
    }

    public Page(int cur, int pageSize) {
        this.cur = cur;
        this.pageSize = pageSize;
    }

    public boolean isFirst() {
        return cur == 1;
    }
}
