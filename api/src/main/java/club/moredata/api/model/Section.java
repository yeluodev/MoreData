package club.moredata.api.model;

import java.io.Serializable;
import java.util.List;

/**
 * @author yeluodev1226
 */
public class Section<T> implements Serializable {

    private static final long serialVersionUID = 8865849711049736954L;
    private int id = 0;
    private String title = "";
    private String subtitle = "";
    private String color = "";
    private String icon = "";
    private String updatedAt = "";
    private List<T> list;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
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
