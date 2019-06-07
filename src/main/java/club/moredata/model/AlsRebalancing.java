package club.moredata.model;

import java.io.Serializable;

/**
 * @author yeluodev1226
 */
public class AlsRebalancing implements Serializable {

    private static final long serialVersionUID = 3169449248233803032L;
    private int rank = 0;
    private String stockName = "";
    private String stockSymbol = "";
    private double changWeight = 0;
    private double percent = 0;

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getStockName() {
        return stockName;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    public void setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
    }

    public double getChangWeight() {
        return changWeight;
    }

    public void setChangWeight(double changWeight) {
        this.changWeight = changWeight;
    }

    public double getPercent() {
        return percent;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }
}
