package club.moredata.common.net;

import club.moredata.common.entity.Cube;
import club.moredata.common.entity.SearchResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 网络请求封装
 *
 * @author yeluodev1226
 */
public class ApiManager {

    static String ROOT_API_HOST = "https://api.xueqiu.com";
    static String USER = ROOT_API_HOST + "/user/show.json";
    static String CUBES_RANK = ROOT_API_HOST + "/cubes/rank/arena_cubes.json";
    static String CUBES_DETAIL = ROOT_API_HOST + "/cubes/show.json";
    static String CUBES_REBALANCING_HISTORY = ROOT_API_HOST + "/cubes/rebalancing/history.json";
    static String STOCK_QUOTEP = ROOT_API_HOST + "/stock/quotep.json";
    static String STATUSES_UPDATE = ROOT_API_HOST + "/statuses/update.json";
    static String PHOTO_UPLOAD = ROOT_API_HOST + "/photo/upload.json";
    static String CUBES_REBALANCING_CREATE = ROOT_API_HOST + "/cubes/rebalancing/create.json";
    static String CUBES_SEARCH = ROOT_API_HOST + "/cube/search.json";
    static String FOLLOWERS = ROOT_API_HOST + "/friendships/followers.json";
    static String STATUSES_SEARCH = ROOT_API_HOST + "/statuses/search.json";

    static String ROOT_STOCK_HOST = "https://stock.xueqiu.com";
    static String STOCK_FOLLOWERS = ROOT_STOCK_HOST + "/v5/stock/portfolio/stock/hasexist.json";
    static String STOCK_LIST = ROOT_STOCK_HOST + "/v5/stock/screener/quote/list.json";

    static String STOCK_TRADE_INFO = "http://hq.sinajs.cn";

    private static ApiManager instance;
    protected OkHttpClient client;

    public ApiManager() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequestsPerHost(60);

        client = new OkHttpClient.Builder()
                .dispatcher(dispatcher)
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(new CommonHeadersInterceptor())
                .build();
    }

    public static ApiManager getInstance() {
        if (null == instance) {
            synchronized (ApiManager.class) {
                if (null == instance) {
                    instance = new ApiManager();
                }
            }
        }
        return instance;
    }

    protected Request buildRequest(String url, Map<String, String> headers, RequestBody body) {
        Request.Builder requestBuilder = new Request.Builder()
                .url(url);
        if (headers != null && !headers.isEmpty()) {
            headers.forEach(requestBuilder::header);
        }
        if (body != null) {
            requestBuilder.post(body);
        }

        return requestBuilder.build();
    }

    /**
     * 雪球组合风云榜
     *
     * @param level 1-近三个月，2-近半年，3-近一年
     */
    public Response cubeRankList(int level) {
        String params = "?count=100&list_param=list_overall&page=1&market=cn&cube_level=" + level;
        Request request = new Request.Builder()
                .url(CUBES_RANK + params)
                .build();
        try {
            return client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void fetchRebalancingHistory(int cubeId, BaseApiCallback callback) {
        String params = "?count=20&page=1&cube_id=" + cubeId;
        Request request = new Request.Builder()
                .url(CUBES_REBALANCING_HISTORY + params)
                .build();
        client.newCall(request).enqueue(callback);
    }

    /**
     * 获取股票简略信息
     *
     * @param stockIds
     */
    public Response fetchStockQuotep(String stockIds) {
        String params = "?stockid=" + stockIds;
        Request request = new Request.Builder()
                .url(STOCK_QUOTEP + params)
                .build();
        try {
            return client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 调仓
     *
     * @param cubeId
     * @param cash
     * @param holdings
     * @param comment
     */
    public Response rebalancingCube(int cubeId, int cash, String holdings, String comment) {
        String params = String.format("cash=%d&comment=%s&cube_id=%d&holdings=%s&market=cn", cash, comment, cubeId, holdings);
        RequestBody body = RequestBody.create(MediaType.parse("text;charset=utf-8"), params);
        //自动调仓账号是固定的，因此只能是QQ登录的账号Cookie
        Request request = new Request.Builder()
                .url(CUBES_REBALANCING_CREATE)
                .header("Cookie", "xq_a_token=6a8cac4e9ea074035410b8ecea901a9053e2eaa6;u=9084578148")
                .post(body)
                .build();
        try {
            return client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 上传图片
     *
     * @return
     */
    public Response uploadImage(File file) {
        MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
        RequestBody filebody = MultipartBody.create(MEDIA_TYPE_PNG, file);
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.addFormDataPart("file", "file.png", filebody);
        builder.setType(MultipartBody.FORM);

        Request request = new Request.Builder()
                .url(PHOTO_UPLOAD)
                .header("Cookie", "xq_a_token=6a8cac4e9ea074035410b8ecea901a9053e2eaa6;u=9084578148")
                .post(builder.build())
                .build();
        try {
            return client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 发帖
     *
     * @param content
     * @return
     */
    public Response newPost(String content) {
        String params = String.format("status=%s&original=0&right=0&is_private=0", content);
        RequestBody body = RequestBody.create(MediaType.parse("text;charset=utf-8"), params);

        Request request = new Request.Builder()
                .url(STATUSES_UPDATE)
                .header("Cookie", "xq_a_token=6a8cac4e9ea074035410b8ecea901a9053e2eaa6;u=9084578148")
                .post(body)
                .build();
        try {
            return client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }



}
