package club.moredata.common.model;

import club.moredata.common.util.DateUtil;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * @author yeluodev1226
 */
public class History<T> implements Serializable {
    private static final long serialVersionUID = 1919571706476317369L;

    private String title;
    private String updatedAt;
    private List<T> list;

    public History() {
        this("", 0L, Collections.emptyList());
    }

    public History(String title, long updatedAt, List<T> list) {
        this(title, "", list);
        this.updatedAt = DateUtil.getInstance().getTimeString(updatedAt);
    }

    public History(String title, String updatedAt, List<T> list) {
        this.title = title;
        this.updatedAt = updatedAt;
        this.list = list;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}
