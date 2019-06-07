package club.moredata.entity;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * 雪球风云榜接口数据
 *
 * @author yeluodev1226
 */
public class RankCubeResult implements Serializable {

    private static final long serialVersionUID = 2690015403147912306L;
    @SerializedName("total_count")
    private int totalCount;
    @SerializedName("list_param")
    private String listParam;
    @SerializedName("new_count")
    private int newCount;
    private int count;
    private int page;
    private List<Cube> list;
    private int maxPage;

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public String getListParam() {
        return listParam;
    }

    public void setListParam(String listParam) {
        this.listParam = listParam;
    }

    public int getNewCount() {
        return newCount;
    }

    public void setNewCount(int newCount) {
        this.newCount = newCount;
    }

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

    public List<Cube> getList() {
        return list;
    }

    public void setList(List<Cube> list) {
        this.list = list;
    }

    public int getMaxPage() {
        return maxPage;
    }

    public void setMaxPage(int maxPage) {
        this.maxPage = maxPage;
    }
}
