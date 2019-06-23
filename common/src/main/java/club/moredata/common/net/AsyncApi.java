package club.moredata.common.net;

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
     * @param request  请求
     * @param callback 回调
     */
    private void call(Request request, Callback callback) {
        client.newCall(request).enqueue(callback);
    }

    /**
     * 获取雪球用户信息
     *
     * @param uid 用户id
     */
    public void getAccount(String uid, BaseApiCallback callback) {
        Request req = buildRequest(USER + "?id=" + uid, null, null);
        call(req, callback);
    }

    /**
     * 获取用户粉丝列表
     *
     * @param params   uid和pageNo
     * @param callback 回调
     */
    public void fetchFollowerList(String params, BaseApiCallback callback) {
        Request req = buildRequest(FOLLOWERS + "?" + params, null, null);
        call(req, callback);
    }

    /**
     * 获取股票关注人数
     *
     * @param symbol   股票代码
     * @param callback 回调
     */
    public void fetchStockFollowers(String symbol, BaseApiCallback callback) {
        Request req = buildRequest(STOCK_FOLLOWERS + "?symbol=" + symbol, null, null);
        call(req, callback);
    }

    /**
     * 搜索股票相关讨论
     *
     * @param symbol   股票代码
     * @param callback 回调
     */
    public void searchSymbolStatuses(String symbol, BaseApiCallback callback) {
        Request req = buildRequest(STATUSES_SEARCH + "?symbol=" + symbol, null, null);
        call(req, callback);
    }

    /**
     * 雪球风云榜组合TOP100
     *
     * @param level    时间段 1-近三个月/2-近半年/3-近一年
     * @param callback 回调
     */
    public void cubeRankList(int level, BaseApiCallback callback) {
        String params = "?count=100&list_param=list_overall&page=1&market=cn&cube_level=" + level;
        Request req = buildRequest(CUBES_RANK + params, null, null);
        client.newCall(req).enqueue(callback);
    }

    /**
     * 雪球组合详情
     *
     * @param symbol   股票代码
     * @param callback 回调
     */
    public void fetchCubeDetail(String symbol, BaseApiCallback callback) {
        String params = "?mix_rebalancing=true&ret_last_buy_rb_gid=true&symbol=" + symbol;
        Request req = buildRequest(CUBES_DETAIL + params, null, null);
        client.newCall(req).enqueue(callback);
    }


    /**
     * 股票列表
     *
     * @param page 页数
     * @param callback 回调
     */
    public void fetchStockList(int page, BaseApiCallback callback) {
        String params = "?market=CN&order=desc&order_by=percent&size=60&type=sh_sz&page=" + page;
        Request req = buildRequest(STOCK_LIST + params, null, null);
        client.newCall(req).enqueue(callback);
    }

    public static void main(String[] args) {
        getInstance().searchSymbolStatuses("SH600036", new BaseApiCallback() {
            @Override
            public void onSuccess(String response) {

            }
        });
    }

}
