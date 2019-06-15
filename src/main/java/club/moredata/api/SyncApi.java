package club.moredata.api;

import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * 同步方式
 *
 * @author yeluodev1226
 */
public class SyncApi extends ApiManager {

    private static SyncApi instance;

    private SyncApi() {
        super();
    }

    public static SyncApi getInstance() {
        if (null == instance) {
            synchronized (SyncApi.class) {
                if (null == instance) {
                    instance = new SyncApi();
                }
            }
        }
        return instance;
    }

    /**
     * 发起同步请求
     *
     * @param request
     */
    private Response call(Request request) throws IOException {
        return client.newCall(request).execute();
    }

    /**
     * 获取用户粉丝列表
     *
     * @param params uid和pageNo
     */
    public Response fetchFollowerList(String params) throws IOException {
        Request req = buildRequest(FOLLOWERS + "?" + params, null, null);
        return call(req);
    }

}
