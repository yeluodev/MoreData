package club.moredata.common.entity;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * @author yeluodev1226
 */
public class StockList implements Serializable {


    private static final long serialVersionUID = -227988109165847320L;
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

    public static class DataBean implements Serializable {

        private static final long serialVersionUID = 7309795224181071280L;
        private int count;
        private List<StockEntity> list;

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public List<StockEntity> getList() {
            return list;
        }

        public void setList(List<StockEntity> list) {
            this.list = list;
        }

    }
}
