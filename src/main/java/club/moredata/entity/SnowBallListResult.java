package club.moredata.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 雪球接口返回列表的通用格式
 * @author yeluodev1226
 */
public class SnowBallListResult<T> implements Serializable {

    private static final long serialVersionUID = 4373354420401970665L;
    private int count;
    private int page;
    private int maxPage;
    private int totalCount;
    private List<T> list;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getMaxPage() {
        return maxPage;
    }

    public void setMaxPage(int maxPage) {
        this.maxPage = maxPage;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}
