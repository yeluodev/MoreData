package club.moredata.api.model;

import java.io.Serializable;

/**
 * @author yeluodev1226
 */
public class LeekResponse<T> implements Serializable {

    private static final long serialVersionUID = -3770142039336990905L;

    public static final int SUCCESS = 1;
    public static final int MISSING_PARAMETER = 101;
    public static final int ERROR_PARAMETER = 102;
    public static final int ERROR_DATABASE = 103;
    public static final int ERROR_SNOWBALL = 104;
    public static final int ERROR_OTHER = 105;
    public static final int ERROR_URL = 106;

    public static final String MSG_SUCCESS = "success";
    public static final String MSG_MISSING_PARAMETER = "缺少必要参数，请检查参数";
    public static final String MSG_ERROR_PARAMETER = "参数错误，请检查参数";
    public static final String MSG_ERROR_DATABASE = "数据库异常，请稍后再试";
    public static final String MSG_ERROR_OTHER = "服务异常，请稍后再试";
    public static final String MSG_ERROR_URL = "接口地址有误";

    private int code = 0;
    private String message = "";
    private T data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static <T> LeekResponse<T> errorResponse(int code, String msg) {
        LeekResponse<T> leekResponse = new LeekResponse<>();
        leekResponse.setCode(code);
        leekResponse.setMessage(msg);
        leekResponse.setData(null);
        return leekResponse;
    }

    public static LeekResponse missingParameterResponse() {
        return errorResponse(MISSING_PARAMETER, MSG_MISSING_PARAMETER);
    }

    public static LeekResponse errorParameterResponse() {
        return errorResponse(ERROR_PARAMETER, MSG_ERROR_PARAMETER);
    }

    public static LeekResponse errorDatabaseResponse() {
        return errorResponse(ERROR_DATABASE, MSG_ERROR_DATABASE);
    }

    public static LeekResponse errorOtherResponse() {
        return errorResponse(ERROR_OTHER, MSG_ERROR_OTHER);
    }

    public static LeekResponse errorURLResponse() {
        return errorResponse(ERROR_URL, MSG_ERROR_URL);
    }

    public static <T> LeekResponse generateResponse(T t) {
        if (t == null) {
            return errorDatabaseResponse();
        }
        return successResponse(t);
    }

    public static <T> LeekResponse<T> successResponse(T t) {
        LeekResponse<T> leekResponse = new LeekResponse<>();
        leekResponse.setCode(LeekResponse.SUCCESS);
        leekResponse.setMessage(LeekResponse.MSG_SUCCESS);
        leekResponse.setData(t);
        return leekResponse;
    }

    public static <T> LeekResponse<T> successResponse(T t, String msg) {
        LeekResponse<T> leekResponse = new LeekResponse<>();
        leekResponse.setCode(LeekResponse.SUCCESS);
        leekResponse.setMessage(msg);
        leekResponse.setData(t);
        return leekResponse;
    }


}
