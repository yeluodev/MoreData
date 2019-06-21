package club.moredata.common.entity;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * @author yeluodev1226
 */
public class StockFollower implements Serializable {

    private static final long serialVersionUID = 3272929350882588713L;
    private DataBean data;
    @SerializedName("error_code")
    private int errorCode;
    @SerializedName("error_description")
    private String errorDescription;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public static class DataBean {
        /**
         * hasExist : false
         * count : 15155
         */

        private boolean hasExist;
        private int count;

        public boolean isHasExist() {
            return hasExist;
        }

        public void setHasExist(boolean hasExist) {
            this.hasExist = hasExist;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }
}
