package club.moredata.api.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yeluodev1226
 */
public class LeekResult<T> implements Serializable {

    private static final long serialVersionUID = 5746327772564335589L;
    private int count = 0;
    private double cash = 0;
    private String updatedAt = "";
    private List<T> list = new ArrayList<>();

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getCash() {
        return cash;
    }

    public void setCash(double cash) {
        this.cash = cash;
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
