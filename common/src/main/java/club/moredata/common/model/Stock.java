package club.moredata.common.model;

import java.io.Serializable;

/**
 * 对应数据库stock表
 *
 * @author yeluodev1226
 */
public class Stock implements Serializable {

    private static final long serialVersionUID = 7645783626573225429L;
    /**
     * 股票名称
     */
    private String name = "";
    /**
     * 股票代码
     */
    private String symbol = "";
    /**
     * 股票关注人数
     */
    private int followers = 0;
    /**
     * 股票相关讨论数
     */
    private int statuses = 0;
    /**
     * 股票关注人数变化
     */
    private int followersChange = 0;
    /**
     * 股票相关讨论数变化
     */
    private int statusesChange = 0;

    private double open = 0;
    private double yesterday = 0;
    private double now = 0;
    private double high = 0;
    private double low = 0;
    private int volume = 0;
    private int suspension = 0;
    private double turnover = 0;
    private double rate = 0;
    private String time = "";

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

    public int getFollowers() {
        return followers;
    }

    public void setFollowers(int followers) {
        this.followers = followers;
    }

    public int getStatuses() {
        return statuses;
    }

    public void setStatuses(int statuses) {
        this.statuses = statuses;
    }

    public int getFollowersChange() {
        return followersChange;
    }

    public void setFollowersChange(int followersChange) {
        this.followersChange = followersChange;
    }

    public int getStatusesChange() {
        return statusesChange;
    }

    public void setStatusesChange(int statusesChange) {
        this.statusesChange = statusesChange;
    }

    public double getOpen() {
        return open;
    }

    public void setOpen(double open) {
        this.open = open;
    }

    public double getYesterday() {
        return yesterday;
    }

    public void setYesterday(double yesterday) {
        this.yesterday = yesterday;
    }

    public double getNow() {
        return now;
    }

    public void setNow(double now) {
        this.now = now;
    }

    public double getHigh() {
        return high;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public double getLow() {
        return low;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int getSuspension() {
        return suspension;
    }

    public void setSuspension(int suspension) {
        this.suspension = suspension;
    }

    public double getTurnover() {
        return turnover;
    }

    public void setTurnover(double turnover) {
        this.turnover = turnover;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
