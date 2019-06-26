package club.moredata.common.net;

import club.moredata.common.entity.Cube;
import club.moredata.common.entity.SearchResult;
import club.moredata.common.model.Stock;
import club.moredata.common.util.Arith;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * 通过新浪股票接口查询涨跌幅等交易信息
     *
     * @param symbols 股票代码，形如sz002624,sh600016
     * @return list
     */
    public List<Stock> fetchStockTradeInfos(String symbols) {
        String params = "?list=" + symbols;
        Request req = buildRequest(STOCK_TRADE_INFO + params, null, null);
        try {
            Response response = call(req);
            if (response == null) {
                return null;
            }
            String res = response.body().string();
            System.out.println(res);
            if (response.code() >= 200 && response.code() < 300) {
                res = res.replaceAll("\n", "");
                String[] stocks = res.split(";");
                List<Stock> stockList = new ArrayList<>();
                for (String str : stocks) {
                    String[] arr = str.split("=");
                    if (arr.length >= 2) {
                        String symbol = arr[0].split("_")[2];
                        if (arr[1].replaceAll("\"", "").equals("")) {
                            continue;
                        }
                        String[] stockInfoArr = arr[1].replaceAll("\"", "").split(",");
                        Stock stock = new Stock();
                        stock.setSymbol(symbol);
                        stock.setName(stockInfoArr[0].replaceAll(" ", ""));
                        stock.setOpen(Double.valueOf(stockInfoArr[1]));
                        stock.setYesterday(Double.valueOf(stockInfoArr[2]));
                        stock.setNow(Double.valueOf(stockInfoArr[3]));
                        stock.setHigh(Double.valueOf(stockInfoArr[4]));
                        stock.setLow(Double.valueOf(stockInfoArr[5]));
                        stock.setVolume(Integer.valueOf(stockInfoArr[8]));
                        stock.setSuspension(Integer.valueOf(stockInfoArr[8]) <= 0 ? 1 : 0);
                        stock.setTurnover(Double.valueOf(stockInfoArr[9]));
                        try {
                            stock.setRate(Arith.sub(Arith.div(Double.valueOf(stockInfoArr[3]),
                                    Double.valueOf(stockInfoArr[2])), 1));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        stock.setTime(stockInfoArr[stockInfoArr.length - 3] + "_" + stockInfoArr[stockInfoArr.length - 2]);
                        stockList.add(stock);
                    }
                }
                return stockList;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 雪球组合详情
     *
     * @param symbol   股票代码
     * @return 结果
     */
    public String fetchCubeDetail(String symbol) {
        String params = "?mix_rebalancing=true&ret_last_buy_rb_gid=true&symbol=" + symbol;
        Request req = buildRequest(CUBES_DETAIL + params, null, null);
        try {
            Response response = call(req);
            if (response == null) {
                return null;
            }
            String res = response.body().string();
            System.out.println(res);
            if (response.code() >= 200 && response.code() < 300) {
                return res;
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }

}
