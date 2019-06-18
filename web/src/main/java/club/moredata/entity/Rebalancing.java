package club.moredata.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * @author yeluodev1226
 */
public class Rebalancing implements Serializable {
    private static final long serialVersionUID = -4517804917544556405L;
    private int id;
    private String status;
    @SerializedName("cube_id")
    @JSONField(name="cube_id")
    private int cubeId;
    @SerializedName("prev_bebalancing_id")
    private int preRebalancingId;
    private String category;
    @SerializedName("exe_strategy")
    private String exeStrategy;
    @SerializedName("created_at")
    private long createdAt;
    @SerializedName("updated_at")
    private long updatedAt;
    @SerializedName("cash_value")
    private double cashValue;
    private double cash;
    @SerializedName("error_code")
    @JSONField(name = "error_code")
    private String errorCode;
    @SerializedName("error_message")
    @JSONField(name = "error_message")
    private String errorMessage;
    @SerializedName("error_status")
    @JSONField(name = "error_status")
    private String errorStatus;
    private String comment;
    private int diff;
    @SerializedName("new_buy_count")
    private int newBuyCount;
    private List<Holdings> holdings;
    @SerializedName("rebalancing_histories")
    private List<RebalancingHistory> rebalancingHistories;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCubeId() {
        return cubeId;
    }

    public void setCubeId(int cubeId) {
        this.cubeId = cubeId;
    }

    public int getPreRebalancingId() {
        return preRebalancingId;
    }

    public void setPreRebalancingId(int preRebalancingId) {
        this.preRebalancingId = preRebalancingId;
    }

    public String getCategory() {
        return category == null ? "" : category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getExeStrategy() {
        return exeStrategy == null ? "" : exeStrategy;
    }

    public void setExeStrategy(String exeStrategy) {
        this.exeStrategy = exeStrategy;
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

    public double getCashValue() {
        return cashValue;
    }

    public void setCashValue(double cashValue) {
        this.cashValue = cashValue;
    }

    public double getCash() {
        return cash;
    }

    public void setCash(double cash) {
        this.cash = cash;
    }

    public String getErrorCode() {
        return errorCode == null ? "" : errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage == null ? "" : errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorStatus() {
        return errorStatus == null ? "" : errorStatus;
    }

    public void setErrorStatus(String errorStatus) {
        this.errorStatus = errorStatus;
    }

    public String getComment() {
        return comment == null ? "" : comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getDiff() {
        return diff;
    }

    public void setDiff(int diff) {
        this.diff = diff;
    }

    public int getNewBuyCount() {
        return newBuyCount;
    }

    public void setNewBuyCount(int newBuyCount) {
        this.newBuyCount = newBuyCount;
    }

    public List<Holdings> getHoldings() {
        return holdings;
    }

    public void setHoldings(List<Holdings> holdings) {
        this.holdings = holdings;
    }

    public List<RebalancingHistory> getRebalancingHistories() {
        return rebalancingHistories;
    }

    public void setRebalancingHistories(List<RebalancingHistory> rebalancingHistories) {
        this.rebalancingHistories = rebalancingHistories;
    }


}
