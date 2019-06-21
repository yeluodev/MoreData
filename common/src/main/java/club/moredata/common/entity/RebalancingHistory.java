package club.moredata.common.entity;

import com.google.gson.annotations.SerializedName;

public class RebalancingHistory {


    /**
     * id : 230966122
     * rebalancing_id : 53788845
     * stock_id : 1001860
     * stock_name : 洋河股份
     * stock_symbol : SZ002304
     * volume : 2.541E-4
     * price : null
     * net_value : 0.0291
     * weight : 2.95
     * target_weight : 3
     * prev_weight : 2.96
     * prev_target_weight : 3
     * prev_weight_adjusted : 2.95
     * prev_volume : 2.541E-4
     * prev_price : 114.31
     * prev_net_value : 0.02904617
     * proactive : true
     * created_at : 1559280776739
     * updated_at : 1559280776739
     * target_volume : 2.5839E-4
     * prev_target_volume : 2.5748E-4
     */

    private int id;
    @SerializedName("rebalancing_id")
    private int rebalancingId;
    @SerializedName("stock_id")
    private int stockId;
    @SerializedName("stock_name")
    private String stockName;
    @SerializedName("stock_symbol")
    private String stockSymbol;
    private double volume;
    private double price;
    @SerializedName("net_value")
    private double netValue;
    private double weight;
    @SerializedName("target_weight")
    private double targetWeight;
    @SerializedName("prev_weight")
    private double prevWeight;
    @SerializedName("prev_target_weight")
    private double prevTargetWeight;
    @SerializedName("prev_weight_adjusted")
    private double prevWeightAdjusted;
    @SerializedName("prev_volume")
    private double prevVolume;
    @SerializedName("prev_price")
    private double prevPrice;
    @SerializedName("prev_net_value")
    private double prevNetValue;
    private boolean proactive;
    @SerializedName("created_at")
    private long createdAt;
    @SerializedName("updated_at")
    private long updatedAt;
    @SerializedName("target_volume")
    private double targetVolume;
    @SerializedName("prev_target_volume")
    private double prevTargetVolume;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRebalancingId() {
        return rebalancingId;
    }

    public void setRebalancingId(int rebalancingId) {
        this.rebalancingId = rebalancingId;
    }

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

    public String getStockSymbol() {
        return stockSymbol;
    }

    public void setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getNetValue() {
        return netValue;
    }

    public void setNetValue(double netValue) {
        this.netValue = netValue;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getTargetWeight() {
        return targetWeight;
    }

    public void setTargetWeight(double targetWeight) {
        this.targetWeight = targetWeight;
    }

    public double getPrevWeight() {
        return prevWeight;
    }

    public void setPrevWeight(double prevWeight) {
        this.prevWeight = prevWeight;
    }

    public double getPrevTargetWeight() {
        return prevTargetWeight;
    }

    public void setPrevTargetWeight(double prevTargetWeight) {
        this.prevTargetWeight = prevTargetWeight;
    }

    public double getPrevWeightAdjusted() {
        return prevWeightAdjusted;
    }

    public void setPrevWeightAdjusted(double prevWeightAdjusted) {
        this.prevWeightAdjusted = prevWeightAdjusted;
    }

    public double getPrevVolume() {
        return prevVolume;
    }

    public void setPrevVolume(double prevVolume) {
        this.prevVolume = prevVolume;
    }

    public double getPrevPrice() {
        return prevPrice;
    }

    public void setPrevPrice(double prevPrice) {
        this.prevPrice = prevPrice;
    }

    public double getPrevNetValue() {
        return prevNetValue;
    }

    public void setPrevNetValue(double prevNetValue) {
        this.prevNetValue = prevNetValue;
    }

    public boolean isProactive() {
        return proactive;
    }

    public void setProactive(boolean proactive) {
        this.proactive = proactive;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public double getTargetVolume() {
        return targetVolume;
    }

    public void setTargetVolume(double targetVolume) {
        this.targetVolume = targetVolume;
    }

    public double getPrevTargetVolume() {
        return prevTargetVolume;
    }

    public void setPrevTargetVolume(double prevTargetVolume) {
        this.prevTargetVolume = prevTargetVolume;
    }
}
