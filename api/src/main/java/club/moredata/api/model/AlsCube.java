package club.moredata.api.model;

import java.io.Serializable;

/**
 * 组合天数排行
 * @author yeluodev1226
 */
public class AlsCube implements Serializable {

    private static final long serialVersionUID = 8656770933262325913L;
    private int rank = 0;
    private String name = "";
    private String symbol = "";
    private double netValue = 0;
    private double gainOnLevel = 0;
    private long ownerId = 0;
    private String screenName = "";
    private String photoDomain = "";
    private String profileImageUrl = "";
    private int showDaysCount = 0;

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
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

    public double getNetValue() {
        return netValue;
    }

    public void setNetValue(double netValue) {
        this.netValue = netValue;
    }

    public double getGainOnLevel() {
        return gainOnLevel;
    }

    public void setGainOnLevel(double gainOnLevel) {
        this.gainOnLevel = gainOnLevel;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getPhotoDomain() {
        return photoDomain;
    }

    public void setPhotoDomain(String photoDomain) {
        this.photoDomain = photoDomain;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public int getShowDaysCount() {
        return showDaysCount;
    }

    public void setShowDaysCount(int showDaysCount) {
        this.showDaysCount = showDaysCount;
    }
}
