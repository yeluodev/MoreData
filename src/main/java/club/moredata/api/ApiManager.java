package club.moredata.api;

import club.moredata.entity.Cube;
import club.moredata.entity.SearchResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 网络请求封装
 *
 * @author yeluodev1226
 */
public class ApiManager {

    private static String ROOT_HOST = "https://api.xueqiu.com";
    private static String USER = ROOT_HOST + "/user/show.json";
    private static String CUBES_RANK = ROOT_HOST + "/cubes/rank/arena_cubes.json";
    private static String CUBES_DETAIL = ROOT_HOST + "/cubes/show.json";
    private static String CUBES_REBALANCING_HISTORY = ROOT_HOST + "/cubes/rebalancing/history.json";
    private static String STOCK_QUOTEP = ROOT_HOST + "/stock/quotep.json";
    private static String STATUSES_UPDATE = ROOT_HOST + "/statuses/update.json";
    private static String PHOTO_UPLOAD = ROOT_HOST + "/photo/upload.json";
    private static String CUBES_REBALANCING_CREATE = ROOT_HOST + "/cubes/rebalancing/create.json";
    private static String CUBES_SEARCH = ROOT_HOST + "/cube/search.json";

    private static ApiManager instance;
    private OkHttpClient client;

    private ApiManager() {
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

    private Headers commonHeaders() {
        Headers.Builder headersBuilder = new Headers.Builder();
        headersBuilder.add("Host", "api.xueqiu.com");
        headersBuilder.add("Accept", "application/json");
        headersBuilder.add("Cookie", "xq_a_token=26e7dfb7dd01d9ce96e44aa198c6518fd2ca30d6;u=9084578148");
        headersBuilder.add("User-Agent", "Xueqiu iPhone 11.19.1");
        headersBuilder.add("Accept-Language", "zh-Hans-CN;q=1");
        headersBuilder.add("Connection", "keep-alive");

        return headersBuilder.build();
    }

    /**
     * 获取雪球用户信息
     *
     * @param uid 用户id
     */
    public void getAccount(String uid) {
        Request request = new Request.Builder()
                .url(USER + "?id=" + uid)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() >= 200 && response.code() < 300) {
                    System.out.println(response.body().string());
                }
            }
        });
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

    /**
     * 获取组合详情
     *
     * @param symbol
     * @param callback
     */
    public void fetchCubeDetail(String symbol, ApiCallback callback) {
        String params = "?mix_rebalancing=true&ret_last_buy_rb_gid=true&symbol=" + symbol;
        Request request = new Request.Builder()
//                .addHeader("Cookie","xq_a_token=df8524b38974fce13d20b7ee39af90871c03be91;u=5414038194")
//                .addHeader("Cookie","xq_a_token=0aebf2e32b55e41f30f0042f3e556b5e26206173;u=411560226561917")
                .url(CUBES_DETAIL + params)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public Response fetchCubeDetail(String symbol) {
        String params = "?mix_rebalancing=true&ret_last_buy_rb_gid=true&symbol=" + symbol;
        Request request = new Request.Builder()
//                .addHeader("Cookie","xq_a_token=df8524b38974fce13d20b7ee39af90871c03be91;u=5414038194")
//                .addHeader("Cookie","xq_a_token=0aebf2e32b55e41f30f0042f3e556b5e26206173;u=411560226561917")
                .url(CUBES_DETAIL + params)
                .build();
        try {
            return client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取组合调仓历史
     *
     * @param cubeId
     */
    public Response fetchRebalancingHistory(int cubeId, int page) {
        String params = "?count=20&page=" + page + "&cube_id=" + cubeId;
        Request request = new Request.Builder()
                .url(CUBES_REBALANCING_HISTORY + params)
                .build();
        try {
            return client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void fetchRebalancingHistory(int cubeId, ApiCallback callback) {
        String params = "?count=20&page=1&cube_id=" + cubeId;
        Request request = new Request.Builder()
                .url(CUBES_REBALANCING_HISTORY + params)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public void fetchRebalancingHistory(int cubeId, int page, ApiCallback callback) {
        String params = "?count=20&page=" + page + "&cube_id=" + cubeId;
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

        Request request = new Request.Builder()
                .url(CUBES_REBALANCING_CREATE)
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
//        String params = String.format("status=%s&original=0&right=0&is_private=0", content);
//        RequestBody body = RequestBody.create(MediaType.parse("text;charset=utf-8"),params);
        MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
        RequestBody filebody = MultipartBody.create(MEDIA_TYPE_PNG, file);
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.addFormDataPart("file", "file.png", filebody);
        builder.setType(MultipartBody.FORM);


        Request request = new Request.Builder()
                .url(PHOTO_UPLOAD)
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
     * 搜索雪球组合
     *
     * @param key
     * @return
     */
    public SearchResult<Cube> searchCube(String key) {
        String params = String.format("?count=20&q=%s", key);
        Request request = new Request.Builder()
                .url(CUBES_SEARCH + params)
                .build();
        SearchResult<Cube> searchResult = null;
        try {
            Response response = client.newCall(request).execute();
            if (response.code() >= 200 && response.code() < 300) {
                try {
                    String res = response.body().string();
                    System.out.println(res);
                    Gson gson = new Gson();
                    searchResult = gson.fromJson(res, new TypeToken<SearchResult<Cube>>() {
                    }.getType());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return searchResult;
    }


    public static void main(String[] args) {
//        getInstance().cubeRankList(1);
//        Response response = getInstance().searchCube("1738676");
//        if(response.code()>=200 && response.code()<300){
//            try {
//                String res = response.body().string();
//                System.out.println(res);
//                Gson gson = new Gson();
//                SearchResult<Cube> searchResult = gson.fromJson(res,new TypeToken<SearchResult<Cube>>() {
//                }.getType());
//                System.out.println(searchResult.getQ());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

        for (int i = 0; i < 10; i++) {
            getInstance().fetchRebalancingHistory(1773329, new ApiCallback() {
                @Override
                public void onSuccess(String response) {
                }
            });
        }

    }

}
