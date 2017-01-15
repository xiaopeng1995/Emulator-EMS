package io.j1st.utils.http.entity;

/**
 * 分页实体类
 */
public class PageResponse {
    private long count;         //总数量
    private long totalPage;     //总页数

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
    public long getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(long totalPage) {
        this.totalPage = totalPage;
    }

}
