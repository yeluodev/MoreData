package club.moredata.api.model;

import java.io.Serializable;

/**
 * @author yeluodev1226
 */
public class AccountSection implements Serializable, Comparable<AccountSection> {

    private static final long serialVersionUID = -7655242468100265449L;
    private String name = "";
    private int count = 0;
    private double percent = 0;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getPercent() {
        return percent;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }

    @Override
    public int compareTo(AccountSection o) {
        return getCount() - o.getCount();
    }
}
