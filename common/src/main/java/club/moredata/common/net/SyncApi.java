package club.moredata.common.net;

import club.moredata.common.entity.Cube;
import club.moredata.common.entity.SearchResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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

    /**
     * 获取组合调仓历史
     *
     * @param cubeId
     */
    public String fetchRebalancingHistory(int cubeId, int page) throws IOException {
        String params = "?count=20&page=" + page + "&cube_id=" + cubeId;
        Request request = buildRequest(CUBES_REBALANCING_HISTORY + params, null, null);
        Response response = call(request);
        if (response == null) {
            return null;
        }
        String res = response.body().string();
        System.out.println(res);
        if (response.code() >= 200 && response.code() < 300) {
            return res;
        }
        return null;
    }

    /**
     * 搜索雪球组合
     *
     * @param key 关键词
     * @return 搜索结果
     */
    public SearchResult<Cube> searchCube(String key) {
        String params = String.format("?count=20&q=%s", key);
        Request request = buildRequest(CUBES_SEARCH + params, null, null);
        try {
            Response response = call(request);
            if (response == null) {
                return null;
            }
            String res = response.body().string();
            System.out.println(res);
            if (response.code() >= 200 && response.code() < 300) {
                Gson gson = new Gson();
                return gson.fromJson(res, new TypeToken<SearchResult<Cube>>() {
                }.getType());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}
