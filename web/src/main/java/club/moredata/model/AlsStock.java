package club.moredata.model;

import java.io.Serializable;

/**
 * @author yeluodev1226
 */
public class AlsStock implements Serializable {

    private static final long serialVersionUID = 4956661090665753012L;
    private int rank = 0;
    private String stockName = "";
    private int stockId = 0;
    private String stockSymbol = "";
    private String segmentName = "";
    private String segmentColor = "";
    private double weight = 0;
    private int count = 0;
    private double percent = 0;
    private double percentWithCash = 0;

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

    public int getStockId() {
        return stockId;
    }

    public void setStockId(int stockId) {
        this.stockId = stockId;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    public void setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
    }

    public String getSegmentName() {
        return segmentName;
    }

    public void setSegmentName(String segmentName) {
        this.segmentName = segmentName;
    }

    public String getSegmentColor() {
        return segmentColor;
    }

    public void setSegmentColor(String segmentColor) {
        this.segmentColor = segmentColor;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
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

    public double getPercentWithCash() {
        return percentWithCash;
    }

    public void setPercentWithCash(double percentWithCash) {
        this.percentWithCash = percentWithCash;
    }
}
