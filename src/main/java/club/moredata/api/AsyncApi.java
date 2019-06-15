package club.moredata.api;

import okhttp3.Callback;
import okhttp3.Request;

/**
 * 异步方式
 *
 * @author yeluodev1226
 */
public class AsyncApi extends ApiManager {

    private static AsyncApi instance;

    private AsyncApi() {
        super();
    }

    public static AsyncApi getInstance() {
        if (null == instance) {
            synchronized (AsyncApi.class) {
                if (null == instance) {
                    instance = new AsyncApi();
                }
            }
        }
        return instance;
    }

    /**
     * 发起异步请求
     *
     * @param request
     * @param callback
     */
    private void call(Request request, Callback callback) {
        client.newCall(request).enqueue(callback);
    }

    /**
     * 获取雪球用户信息
     *
     * @param uid 用户id
     */
    public void getAccount(String uid, ApiCallback callback) {
        Request req = buildRequest(USER + "?id=" + uid, null, null);
        call(req, callback);
    }

    /**
     * 获取用户粉丝列表
     *
     * @param params   uid和pageNo
     * @param callback
     */
    public void fetchFollowerList(String params, ApiCallback callback) {
        Request req = buildRequest(FOLLOWERS + "?" + params, null, null);
        call(req, callback);
    }

}
