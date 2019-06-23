package club.moredata.common.entity;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 股票信息
 *
 * @author yeluodev1226
 */
public class StockEntity implements Serializable {

    private static final long serialVersionUID = -8371495084342348279L;
    private String symbol;
    @SerializedName("current_year_percent")
    private double currentYearPercent;
    private int followers;
    private String name;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getCurrentYearPercent() {
        return currentYearPercent;
    }

    public void setCurrentYearPercent(double currentYearPercent) {
        this.currentYearPercent = currentYearPercent;
    }

    public int getFollowers() {
        return followers;
    }

    public void setFollowers(int followers) {
        this.followers = followers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
