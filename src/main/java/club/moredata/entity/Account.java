package club.moredata.entity;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 雪球账户详情
 * @author yeluodev1226
 */
public class Account implements Serializable {

    private static final long serialVersionUID = -4282228272646300857L;
    private boolean subscribeable;
    @SerializedName("common_count")
    private int commonCount;
    private String remark;
    @SerializedName("recommend_reason")
    private String recommendReason;
    private String domain;
    private String description;
    private String name;
    private String location;
    private long id;
    private String type;
    @SerializedName("status_count")
    private int statusCount;
    private int status;
    private String profile;
    @SerializedName("screen_name")
    private String screenName;
    private boolean following;
    private String url;
    @SerializedName("verified_type")
    private int verifiedType;
    @SerializedName("donate_count")
    private int donateCount;
    @SerializedName("verified_description")
    private String verifiedDescription;
    private String city;
    private String gender;
    @SerializedName("last_status_id")
    private int lastStatusId;
    private String province;
    @SerializedName("blog_description")
    private String blogDescription;
    private boolean blocking;
    @SerializedName("friends_count")
    private int friendsCount;
    @SerializedName("followers_count")
    private int followersCount;
    @SerializedName("st_color")
    private String stColor;
    @SerializedName("follow_me")
    private boolean followMe;
    private boolean verified;
    private String step;
    private String recommend;
    private String intro;
    @SerializedName("stock_status_count")
    private Object stockStatusCount;
    @SerializedName("allow_all_stock")
    private boolean allowAllStock;
    @SerializedName("stocks_count")
    private Object stocksCount;
    @SerializedName("photo_domain")
    private String photoDomain;
    @SerializedName("profile_image_url")
    private String profileImageUrl;
    @SerializedName("group_ids")
    private Object groupIds;
    @SerializedName("name_pinyin")
    private String namePinyin;
    @SerializedName("screenname_pinyin")
    private String screennamePinyin;

    public boolean isSubscribeable() {
        return subscribeable;
    }

    public void setSubscribeable(boolean subscribeable) {
        this.subscribeable = subscribeable;
    }

    public int getCommonCount() {
        return commonCount;
    }

    public void setCommonCount(int commonCount) {
        this.commonCount = commonCount;
    }

    public String getRemark() {
        return remark==null?"":remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getRecommendReason() {
        return recommendReason==null?"":recommendReason;
    }

    public void setRecommendReason(String recommendReason) {
        this.recommendReason = recommendReason;
    }

    public String getDomain() {
        return domain==null?"":domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getDescription() {
        return description==null?"":description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name==null?"":remark;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location==null?"":location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getStatusCount() {
        return statusCount;
    }

    public void setStatusCount(int statusCount) {
        this.statusCount = statusCount;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getScreenName() {
        return screenName==null?"":screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public boolean isFollowing() {
        return following;
    }

    public void setFollowing(boolean following) {
        this.following = following;
    }

    public String getUrl() {
        return url==null?"":url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getVerifiedType() {
        return verifiedType;
    }

    public void setVerifiedType(int verifiedType) {
        this.verifiedType = verifiedType;
    }

    public int getDonateCount() {
        return donateCount;
    }

    public void setDonateCount(int donateCount) {
        this.donateCount = donateCount;
    }

    public String getVerifiedDescription() {
        return verifiedDescription;
    }

    public void setVerifiedDescription(String verifiedDescription) {
        this.verifiedDescription = verifiedDescription;
    }

    public String getCity() {
        return city==null?"":city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getGender() {
        return gender==null?"":gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getLastStatusId() {
        return lastStatusId;
    }

    public void setLastStatusId(int lastStatusId) {
        this.lastStatusId = lastStatusId;
    }

    public String getProvince() {
        return province==null?"":province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getBlogDescription() {
        return blogDescription==null?"":blogDescription;
    }

    public void setBlogDescription(String blogDescription) {
        this.blogDescription = blogDescription;
    }

    public boolean isBlocking() {
        return blocking;
    }

    public void setBlocking(boolean blocking) {
        this.blocking = blocking;
    }

    public int getFriendsCount() {
        return friendsCount;
    }

    public void setFriendsCount(int friendsCount) {
        this.friendsCount = friendsCount;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }

    public String getStColor() {
        return stColor;
    }

    public void setStColor(String stColor) {
        this.stColor = stColor;
    }

    public boolean isFollowMe() {
        return followMe;
    }

    public void setFollowMe(boolean followMe) {
        this.followMe = followMe;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public String getRecommend() {
        return recommend==null?"":recommend;
    }

    public void setRecommend(String recommend) {
        this.recommend = recommend;
    }

    public String getIntro() {
        return intro==null?"":intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public Object getStockStatusCount() {
        return stockStatusCount;
    }

    public void setStockStatusCount(Object stockStatusCount) {
        this.stockStatusCount = stockStatusCount;
    }

    public boolean isAllowAllStock() {
        return allowAllStock;
    }

    public void setAllowAllStock(boolean allowAllStock) {
        this.allowAllStock = allowAllStock;
    }

    public Object getStocksCount() {
        return stocksCount;
    }

    public void setStocksCount(Object stocksCount) {
        this.stocksCount = stocksCount;
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

    public Object getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(Object groupIds) {
        this.groupIds = groupIds;
    }

    public String getNamePinyin() {
        return namePinyin==null?"":namePinyin;
    }

    public void setNamePinyin(String namePinyin) {
        this.namePinyin = namePinyin;
    }

    public String getScreennamePinyin() {
        return screennamePinyin==null?"":screennamePinyin;
    }

    public void setScreennamePinyin(String screennamePinyin) {
        this.screennamePinyin = screennamePinyin;
    }
}
