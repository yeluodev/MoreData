package club.moredata.common.entity;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 雪球组合
 *
 * @author yeluodev1226
 */
public class Cube implements Serializable {

    private static final long serialVersionUID = 5655719825424924485L;
    //表中自增id
    private int tableRowId;
    private int id;
    private String name;
    private String symbol;
    private String description;
    @SerializedName("active_flag")
    private boolean activeFlag;
    private int star;
    private String market;
    @SerializedName("owner_id")
    private long ownerId;
    @SerializedName("created_at")
    private long createdAt;
    @SerializedName("updated_at")
    private long updatedAt;
    @SerializedName("last_rb_id")
    private int lastRbId;
    @SerializedName("daily_gain")
    private float dailyGain;
    @SerializedName("monthly_gain")
    private float monthlyGain;
    @SerializedName("total_gain")
    private float totalGain;
    @SerializedName("net_value")
    private float netValue;
    @SerializedName("rank_percent")
    private float rankPercent;
    @SerializedName("annualized_gain_rate")
    private float annualizedGainRate;
    @SerializedName("bb_rate")
    private float bbRate;
    private boolean following;
    @SerializedName("follower_count")
    private int followerCount;
    @SerializedName("view_rebalancing")
    private Rebalancing viewRebalancing;
    @SerializedName("last_rebalancing")
    private Rebalancing lastRebalancing;
    @SerializedName("last_success_rebalancing")
    private Rebalancing lastSuccessRebalancing;
    private Object style;
    private Object tag;
    @SerializedName("recommend_reason")
    private Object recommendReason;
    @SerializedName("sale_flag")
    private boolean saleFlag;
    @SerializedName("sell_flag")
    private boolean sell_Flag;
    private Object commission;
    @SerializedName("initial_capital")
    private Object initialCapital;
    @SerializedName("listed_flag")
    private boolean listedFlag;
    @SerializedName("contractor_id")
    private Object contractorId;
    @SerializedName("contractor_name")
    private Object contractorName;
    @SerializedName("last_user_rb_gid")
    private int lastUserRbGid;
    private Account owner;
    private Object performance;
    @SerializedName("closed_at")
    private long closedAt;
    @SerializedName("badges_exist")
    private boolean badgesExist;
    private Object rankingData;
    @SerializedName("cube_level")
    private int cubeLevel;
    @SerializedName("gain_on_level")
    private double gainOnLevel;
    private double total_score;
    private int rank;
    private int trend;

    public int getTableRowId() {
        return tableRowId;
    }

    public void setTableRowId(int tableRowId) {
        this.tableRowId = tableRowId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getDescription() {
        return description == null ? "" : description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActiveFlag() {
        return activeFlag;
    }

    public void setActiveFlag(boolean activeFlag) {
        this.activeFlag = activeFlag;
    }

    public int getStar() {
        return star;
    }

    public void setStar(int star) {
        this.star = star;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
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

    public int getLastRbId() {
        return lastRbId;
    }

    public void setLastRbId(int lastRbId) {
        this.lastRbId = lastRbId;
    }

    public float getDailyGain() {
        return dailyGain;
    }

    public void setDailyGain(float dailyGain) {
        this.dailyGain = dailyGain;
    }

    public float getMonthlyGain() {
        return monthlyGain;
    }

    public void setMonthlyGain(float monthlyGain) {
        this.monthlyGain = monthlyGain;
    }

    public float getTotalGain() {
        return totalGain;
    }

    public void setTotalGain(float totalGain) {
        this.totalGain = totalGain;
    }

    public float getNetValue() {
        return netValue;
    }

    public void setNetValue(float netValue) {
        this.netValue = netValue;
    }

    public float getRankPercent() {
        return rankPercent;
    }

    public void setRankPercent(float rankPercent) {
        this.rankPercent = rankPercent;
    }

    public float getAnnualizedGainRate() {
        return annualizedGainRate;
    }

    public void setAnnualizedGainRate(float annualizedGainRate) {
        this.annualizedGainRate = annualizedGainRate;
    }

    public float getBbRate() {
        return bbRate;
    }

    public void setBbRate(float bbRate) {
        this.bbRate = bbRate;
    }

    public boolean isFollowing() {
        return following;
    }

    public void setFollowing(boolean following) {
        this.following = following;
    }

    public int getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(int followerCount) {
        this.followerCount = followerCount;
    }

    public Rebalancing getViewRebalancing() {
        return viewRebalancing;
    }

    public void setViewRebalancing(Rebalancing viewRebalancing) {
        this.viewRebalancing = viewRebalancing;
    }

    public Rebalancing getLastRebalancing() {
        return lastRebalancing;
    }

    public void setLastRebalancing(Rebalancing lastRebalancing) {
        this.lastRebalancing = lastRebalancing;
    }

    public Rebalancing getLastSuccessRebalancing() {
        return lastSuccessRebalancing;
    }

    public void setLastSuccessRebalancing(Rebalancing lastSuccessRebalancing) {
        this.lastSuccessRebalancing = lastSuccessRebalancing;
    }

    public Object getStyle() {
        return style;
    }

    public void setStyle(Object style) {
        this.style = style;
    }

    public Object getTag() {
        return tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

    public Object getRecommendReason() {
        return recommendReason;
    }

    public void setRecommendReason(Object recommendReason) {
        this.recommendReason = recommendReason;
    }

    public boolean isSaleFlag() {
        return saleFlag;
    }

    public void setSaleFlag(boolean saleFlag) {
        this.saleFlag = saleFlag;
    }

    public boolean isSell_Flag() {
        return sell_Flag;
    }

    public void setSell_Flag(boolean sell_Flag) {
        this.sell_Flag = sell_Flag;
    }

    public Object getCommission() {
        return commission;
    }

    public void setCommission(Object commission) {
        this.commission = commission;
    }

    public Object getInitialCapital() {
        return initialCapital;
    }

    public void setInitialCapital(Object initialCapital) {
        this.initialCapital = initialCapital;
    }

    public boolean isListedFlag() {
        return listedFlag;
    }

    public void setListedFlag(boolean listedFlag) {
        this.listedFlag = listedFlag;
    }

    public Object getContractorId() {
        return contractorId;
    }

    public void setContractorId(Object contractorId) {
        this.contractorId = contractorId;
    }

    public Object getContractorName() {
        return contractorName;
    }

    public void setContractorName(Object contractorName) {
        this.contractorName = contractorName;
    }

    public int getLastUserRbGid() {
        return lastUserRbGid;
    }

    public void setLastUserRbGid(int lastUserRbGid) {
        this.lastUserRbGid = lastUserRbGid;
    }

    public Account getOwner() {
        return owner;
    }

    public void setOwner(Account owner) {
        this.owner = owner;
    }

    public Object getPerformance() {
        return performance;
    }

    public void setPerformance(Object performance) {
        this.performance = performance;
    }

    public long getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(long closedAt) {
        this.closedAt = closedAt;
    }

    public boolean isBadgesExist() {
        return badgesExist;
    }

    public void setBadgesExist(boolean badgesExist) {
        this.badgesExist = badgesExist;
    }

    public Object getRankingData() {
        return rankingData;
    }

    public void setRankingData(Object rankingData) {
        this.rankingData = rankingData;
    }

    public int getCubeLevel() {
        return cubeLevel;
    }

    public void setCubeLevel(int cubeLevel) {
        this.cubeLevel = cubeLevel;
    }

    public double getGainOnLevel() {
        return gainOnLevel;
    }

    public void setGainOnLevel(double gainOnLevel) {
        this.gainOnLevel = gainOnLevel;
    }

    public double getTotal_score() {
        return total_score;
    }

    public void setTotal_score(double total_score) {
        this.total_score = total_score;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getTrend() {
        return trend;
    }

    public void setTrend(int trend) {
        this.trend = trend;
    }
}
