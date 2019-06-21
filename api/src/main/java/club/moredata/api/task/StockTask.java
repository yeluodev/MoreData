package club.moredata.api.task;

import club.moredata.common.model.History;
import club.moredata.common.model.Stock;
import club.moredata.common.util.RedisUtil;
import com.alibaba.fastjson.JSON;
import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 股票数据处理任务
 *
 * @author yeluodev1226
 */
public class StockTask {

    private static final String KEY_ANYTHING = "*";

    /**
     * 获取股票相关信息的历史数据
     *
     * @return 历史数据
     */
    public List<History<Stock>> stockHistoryList() {
        List<History<Stock>> historyList = new ArrayList<>();

        Jedis jedis = RedisUtil.getJedis();
        for (String key : jedis.keys(KEY_ANYTHING)) {
            Map<String, String> map = jedis.hgetAll(key);
            List<Stock> stockList = convertMapToList(map);
            stockList.sort(Comparator.comparingInt(Stock::getFollowers));
            Collections.reverse(stockList);
            historyList.add(new History<>("股票信息", Long.valueOf(key), stockList));
        }
        jedis.close();
        return historyList;
    }

    /**
     * 返回某个时间点的股票相关信息
     *
     * @param timestamp 时间戳
     * @return 股票历史数据
     */
    public History<Stock> stockHistoryAt(long timestamp) {
        AtomicReference<History<Stock>> history = new AtomicReference<>();
        stockHistoryTimeList().forEach(historyTime -> {
            if (historyTime == timestamp) {
                Jedis jedis = RedisUtil.getJedis();
                Map<String, String> map = jedis.hgetAll(String.valueOf(historyTime));
                history.set(new History<>("股票信息", historyTime, convertMapToList(map)));
                jedis.close();
            }
        });
        return history.get();
    }

    /**
     * 将Redis数据库中通过hash保存的stock历史数据转为stock列表
     *
     * @param map redis hash
     * @return stock list
     */
    private List<Stock> convertMapToList(Map<String, String> map) {
        List<Stock> stockList = new ArrayList<>();
        map.forEach((symbol, value) -> {
            String[] arr = value.split("-");
            Stock stock = new Stock();
            stock.setSymbol(symbol);
            stock.setName(arr[0]);
            stock.setFollowers(Integer.valueOf(arr[1]));
            stock.setStatuses(Integer.valueOf(arr[2]));
            stockList.add(stock);
        });
        return stockList;
    }

    /**
     * 获取股票信息历史数据的时间戳列表
     *
     * @return 时间戳列表，由近及远
     */
    public List<Long> stockHistoryTimeList() {
        List<Long> timeList = new ArrayList<>();
        Jedis jedis = RedisUtil.getJedis();
        Set<String> keys = jedis.keys(KEY_ANYTHING);
        keys.forEach(key -> timeList.add(Long.valueOf(key)));
        jedis.close();
        Collections.sort(timeList);
        Collections.reverse(timeList);
        return timeList;
    }

    /**
     * 一小时内数据变化
     */
    public History<Stock> oneHourChange() {
        List<Long> timeList = stockHistoryTimeList();
        if (timeList.size() < 2) {
            System.out.println("暂无历史数据，无法展示变化情况");
            return null;
        }

        History<Stock> latestHistory = stockHistoryAt(timeList.get(0));
        History<Stock> lastHistory = stockHistoryAt(timeList.get(1));

        latestHistory.getList().forEach(latestStock -> {
            for (Stock lastStock : lastHistory.getList()) {
                if (lastStock.getSymbol().equals(latestStock.getSymbol())) {
                    latestStock.setFollowersChange(latestStock.getFollowers() - lastStock.getFollowers());
                    latestStock.setStatusesChange(latestStock.getStatuses() - lastStock.getStatuses());
                    break;
                }
            }
        });

        latestHistory.getList().sort(Comparator.comparingInt(Stock::getFollowersChange));
        Collections.reverse(latestHistory.getList());
        return latestHistory;
    }

    /**
     * 一天内数据变化
     * TODO 等待实际数据爬取后做
     */
    public History<Stock> oneDayChange() {
        List<Long> timeList = stockHistoryTimeList();
        //TODO 判断时间跨度，数据库中数据是否满足改条件

        History<Stock> latestHistory = stockHistoryAt(timeList.get(0));
        History<Stock> lastHistory = stockHistoryAt(timeList.get(1));

        latestHistory.getList().forEach(latestStock -> {
            for (Stock lastStock : lastHistory.getList()) {
                if (lastStock.getSymbol().equals(latestStock.getSymbol())) {
                    latestStock.setFollowersChange(latestStock.getFollowers() - lastStock.getFollowers());
                    latestStock.setStatusesChange(latestStock.getStatuses() - lastStock.getStatuses());
                    break;
                }
            }
        });

        latestHistory.getList().sort(Comparator.comparingInt(Stock::getFollowersChange));
        return latestHistory;
    }

    /**
     * 一周内数据变化
     * TODO 假设当前已做到每小时更新一次数据入库
     */
    public History<Stock> oneWeekChange() {
        List<Long> timeList = stockHistoryTimeList();
        //TODO 判断时间跨度，数据库中数据是否满足改条件

        History<Stock> latestHistory = stockHistoryAt(timeList.get(0));
        History<Stock> lastHistory = stockHistoryAt(timeList.get(1));

        latestHistory.getList().forEach(latestStock -> {
            for (Stock lastStock : lastHistory.getList()) {
                if (lastStock.getSymbol().equals(latestStock.getSymbol())) {
                    latestStock.setFollowersChange(latestStock.getFollowers() - lastStock.getFollowers());
                    latestStock.setStatusesChange(latestStock.getStatuses() - lastStock.getStatuses());
                    break;
                }
            }
        });

        latestHistory.getList().sort(Comparator.comparingInt(Stock::getFollowersChange));
        return latestHistory;
    }

    public static void main(String[] args) {
        StockTask task = new StockTask();
//        System.out.println(JSON.toJSONString(task.stockHistoryTimeList()));
        System.out.println(JSON.toJSONString(task.oneHourChange()));
    }

}
