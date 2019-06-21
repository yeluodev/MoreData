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
}
