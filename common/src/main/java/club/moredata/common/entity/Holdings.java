package club.moredata.common.entity;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * @author yeluodev1226
 */
public class Holdings implements Serializable {

    private static final long serialVersionUID = -7373926979483072310L;
    @SerializedName("stock_id")
    private int stockId;
    private double weight;
    @SerializedName("segment_name")
    private String segmentName;
    @SerializedName("segment_id")
    private int segmentId;
    @SerializedName("stock_name")
    private String stockName;
    @SerializedName("stock_symbol")
    private String stockSymbol;
    @SerializedName("segment_color")
    private String segmentColor;
    private boolean proactive;
    private double volume;

    public int getStockId() {
        return stockId;
    }

    public void setStockId(int stockId) {
        this.stockId = stockId;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getSegmentName() {
        return segmentName;
    }

    public void setSegmentName(String segmentName) {
        this.segmentName = segmentName;
    }

    public int getSegmentId() {
        return segmentId;
    }

    public void setSegmentId(int segmentId) {
        this.segmentId = segmentId;
    }

    public String getStockName() {
        return stockName == null ? "" : stockName;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }

    public String getStockSymbol() {
        return stockSymbol == null ? "" : stockSymbol;
    }

    public void setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
    }

    public String getSegmentColor() {
        return segmentColor;
    }

    public void setSegmentColor(String segmentColor) {
        this.segmentColor = segmentColor;
    }

    public boolean isProactive() {
        return proactive;
    }

    public void setProactive(boolean proactive) {
        this.proactive = proactive;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }
}