package club.moredata.api.spider;

import club.moredata.common.db.helper.StockDbHelper;
import club.moredata.common.entity.StatusesSearch;
import club.moredata.common.entity.StockEntity;
import club.moredata.common.entity.StockFollower;
import club.moredata.common.entity.StockList;
import club.moredata.common.exception.ApiException;
import club.moredata.common.model.Stock;
import club.moredata.common.net.AsyncApi;
import club.moredata.common.net.BaseApiCallback;
import club.moredata.common.util.DBPoolConnection;
import club.moredata.common.util.RedisUtil;
import com.google.gson.Gson;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import redis.clients.jedis.Jedis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 * 股票相关数据爬取
 *
 * @author yeluodev1226
 */
public class StockSpider {

    /**
     * 当前股票相关信息存入Redis，保留历史
     */
    private void copyStockInfosToRedis() {
        Jedis jedis = RedisUtil.getJedis();
        jedis.hmset(String.valueOf(System.currentTimeMillis()), StockDbHelper.stockMap());
        jedis.close();
        System.out.println("已上传至Redis数据库");
    }

    /**
     * 访问雪球API，更新股票关注数和相关讨论数
     */
    public void updateStockInfos() {
        updateStockInfos(null);
    }

    /**
     * 访问雪球API，更新股票关注数和相关讨论数
     *
     * @param symbolList 待更新股票代码
     */
    private void updateStockInfos(List<String> symbolList) {
        List<String> symbols = new ArrayList<>();
        List<String> errorSymbols = new ArrayList<>();
        if (symbolList == null || symbolList.size() == 0) {
            symbols.addAll(StockDbHelper.symbolList());
        } else {
            symbols.addAll(symbolList);
        }

        List<Observable<Stock>> observableList = new ArrayList<>();
        symbols.forEach(symbol -> observableList.add(Observable.<Stock>create(emitter -> AsyncApi.getInstance().searchSymbolStatuses(symbol,
                new BaseApiCallback() {
                    @Override
                    public void onSuccess(String response) {
                        Gson gson = new Gson();
                        StatusesSearch result = gson.fromJson(response, StatusesSearch.class);
                        Stock stock = new Stock();
                        stock.setSymbol(symbol);
                        stock.setStatuses(result.getCount());
                        emitter.onNext(stock);
                        emitter.onComplete();
                    }

                    @Override
                    public void onError(String response) {
                        emitter.onError(new ApiException(symbol, "股票信息更新失败"));
                        emitter.onComplete();
                    }
                })).flatMap((Function<Stock, ObservableSource<Stock>>) stock -> Observable.create(emitter -> AsyncApi.getInstance().fetchStockFollowers(symbol,
                new BaseApiCallback() {
                    @Override
                    public void onSuccess(String response) {
                        Gson gson = new Gson();
                        StockFollower result = gson.fromJson(response, StockFollower.class);
                        stock.setFollowers(result.getData().getCount());
                        StockDbHelper.updateStockStatuses(stock);
                        emitter.onNext(stock);
                        emitter.onComplete();
                    }

                    @Override
                    public void onError(String response) {
                        emitter.onError(new ApiException(symbol, "股票信息更新失败"));
                        emitter.onComplete();
                    }
                })
        ))));

        Observable.merge(observableList)
                .subscribe(new Observer<Stock>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(Stock stock) {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof ApiException) {
                            errorSymbols.add(((ApiException) e).getSymbol());
                        }
                    }

                    @Override
                    public void onComplete() {
                        if (errorSymbols.size() > 0) {
                            updateStockInfos(errorSymbols);
                        } else {
                            System.out.println("股票信息更新完毕");
                            //更新完毕后，同步最新数据到Redis数据库
                            copyStockInfosToRedis();
                        }
                    }
                });

    }

    private List<StockEntity> stockEntityList = new ArrayList<>();

    private void syncStockSymbol(List<Integer> pageList) {
        List<Integer> pages = new ArrayList<>();
        List<Integer> errorPages = new ArrayList<>();
        if (pageList == null || pageList.size() == 0) {
            for (int i = 1; i < 64; i++) {
                pages.add(i);
            }
        } else {
            pages.addAll(pageList);
        }

        List<Observable<StockEntity>> observableList = new ArrayList<>();
        pages.forEach(page -> observableList.add(Observable.<StockEntity>create(emitter -> {
            AsyncApi.getInstance().fetchStockList(page, new BaseApiCallback() {
                @Override
                public void onSuccess(String response) {
                    Gson gson = new Gson();
                    StockList result = gson.fromJson(response, StockList.class);
                    if (result == null) {
                        System.out.println("接口出错");
                        emitter.onError(new ApiException(String.valueOf(page), "股票列表请求失败"));
                        emitter.onComplete();
                        return;
                    }
                    result.getData().getList().forEach(emitter::onNext);
                    emitter.onComplete();
                }

                @Override
                public void onError(String response) {
                    System.out.println("接口出错");
                    emitter.onError(new ApiException(String.valueOf(page), "股票列表请求失败"));
                    emitter.onComplete();
                }
            });
        })));

        Observable.merge(observableList)
                .subscribe(new Observer<StockEntity>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(StockEntity stock) {
                        stockEntityList.add(stock);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof ApiException) {
                            errorPages.add(Integer.valueOf(((ApiException) e).getSymbol()));
                        }
                    }

                    @Override
                    public void onComplete() {
                        if (errorPages.size() > 0) {
                            syncStockSymbol(errorPages);
                        } else {
                            //更新完毕后，同步最新数据到Redis数据库
                            insert2DB();
                            System.out.println("股票同步完毕");
                        }
                    }
                });

    }

    /**
     * 同步当前沪深股市股票代码
     */
    public void syncStockSymbol() {
        syncStockSymbol(null);
    }

    private void insert2DB() {
        Connection connection = null;
        try {
            connection = DBPoolConnection.getInstance().getConnection();
            connection.setAutoCommit(false);
            PreparedStatement ps = connection.prepareStatement("INSERT INTO `stock`(`name`,`symbol`,`updated_at`) " +
                    "VALUES (?,?,CURRENT_TIMESTAMP()) ON DUPLICATE KEY UPDATE `name` = VALUES(`name`),`symbol` = " +
                    "VALUES(`symbol`),`updated_at` = VALUES(`updated_at`);");

            for (StockEntity stock : stockEntityList) {
                ps.setString(1, stock.getName());
                ps.setString(2, stock.getSymbol());
                ps.addBatch();
            }
            ps.executeBatch();
            connection.commit();
            ps.close();

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (null != connection) {
                    connection.rollback();
                }
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } finally {
            try {
                if (null != connection) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
//        new StockSpider().syncStockSymbol();
//        new StockSpider().updateStockInfos();
        new StockSpider().copyStockInfosToRedis();
    }

}
