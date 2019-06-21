package club.moredata.common.entity;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * @author yeluodev1226
 */
public class ErrorResponse implements Serializable {

    private static final long serialVersionUID = 3691271380460088791L;
    @SerializedName("error_description")
    private String errorDescription;
    @SerializedName("error_uri")
    private String errorUri;
    @SerializedName("error_code")
    private String errorCode;

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public String getErrorUri() {
        return errorUri;
    }

    public void setErrorUri(String errorUri) {
        this.errorUri = errorUri;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}
