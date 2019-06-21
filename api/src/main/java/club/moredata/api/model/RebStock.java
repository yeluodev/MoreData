package club.moredata.api.model;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

/**
 * @author yeluodev1226
 */
public class RebStock implements Serializable,Comparable<RebStock> {

    private static final long serialVersionUID = 3921774512916763132L;
    @JSONField(name = "stock_id")
    private int stockId = 0;
    @JSONField(name = "stock_name")
    private String stockName = "";
    @JSONField(name = "segment_name")
    private String segmentName = "";
    private boolean proactive = true;
    private int weight = 0;
    @JSONField(name ="source_weight")
    private int sourceWeight = 0;
    @JSONField(name = "source_decimal")
    private double sourceDecimal = 0;

    public int getStockId() {
        return stockId;
    }

    public void setStockId(int stockId) {
        this.stockId = stockId;
    }

    public String getStockName() {
        return stockName;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }

    public String getSegmentName() {
        return segmentName;
    }

    public void setSegmentName(String segmentName) {
        this.segmentName = segmentName;
    }

    public boolean isProactive() {
        return proactive;
    }

    public void setProactive(boolean proactive) {
        this.proactive = proactive;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getSourceWeight() {
        return sourceWeight;
    }

    public void setSourceWeight(int sourceWeight) {
        this.sourceWeight = sourceWeight;
    }

    public double getSourceDecimal() {
        return sourceDecimal;
    }

    public void setSourceDecimal(double sourceDecimal) {
        this.sourceDecimal = sourceDecimal;
    }

    @Override
    public int compareTo(RebStock o) {
        return Double.compare(getSourceDecimal(),o.getSourceDecimal());
    }
}
