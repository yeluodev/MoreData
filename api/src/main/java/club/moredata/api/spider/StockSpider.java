package club.moredata.api.spider;

import club.moredata.common.db.helper.StockDbHelper;
import club.moredata.common.entity.StatusesSearch;
import club.moredata.common.entity.StockFollower;
import club.moredata.common.exception.ApiException;
import club.moredata.common.model.Stock;
import club.moredata.common.net.AsyncApi;
import club.moredata.common.net.BaseApiCallback;
import club.moredata.common.util.RedisUtil;
import com.google.gson.Gson;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import redis.clients.jedis.Jedis;

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

}
