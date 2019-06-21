package club.moredata.common.entity;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * @author yeluodev1226
 */
public class FollowerResult implements Serializable {

    private static final long serialVersionUID = 2487897343990275729L;
    private int count;
    private int page;
    private int maxPage;
    @SerializedName("anonymous_count")
    private int anonymousCount;
    private List<Account> followers;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getMaxPage() {
        return maxPage;
    }

    public void setMaxPage(int maxPage) {
        this.maxPage = maxPage;
    }

    public int getAnonymousCount() {
        return anonymousCount;
    }

    public void setAnonymousCount(int anonymousCount) {
        this.anonymousCount = anonymousCount;
    }

    public List<Account> getFollowers() {
        return followers;
    }

    public void setFollowers(List<Account> followers) {
        this.followers = followers;
    }
}
