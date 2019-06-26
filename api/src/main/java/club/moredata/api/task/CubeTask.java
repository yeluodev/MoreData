package club.moredata.api.task;

import club.moredata.api.model.LeekResult;
import club.moredata.common.db.helper.CubeDbHelper;
import club.moredata.common.db.sql.SQLBuilder;
import club.moredata.common.entity.*;
import club.moredata.common.exception.ApiException;
import club.moredata.common.net.AsyncApi;
import club.moredata.common.net.BaseApiCallback;
import club.moredata.common.net.SyncApi;
import club.moredata.common.util.DBPoolConnection;
import club.moredata.common.util.DateUtil;
import club.moredata.common.util.RedisUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 组合相关任务处理类
 *
 * @author yeluodev1226
 */
public class CubeTask {

    /**
     * 查询雪球风云榜排行组合数据入库
     */
    public void fetchCubeRankList() {
        fetchCubeRankList(null);
    }

    /**
     * 查询雪球风云榜排行组合数据入库
     */
    private void fetchCubeRankList(List<Integer> levelList) {
        List<Integer> levels = new ArrayList<>();
        List<Integer> errorLevels = new ArrayList<>();
        if (levelList == null || levelList.size() == 0) {
            levels.addAll(Arrays.asList(1, 2, 3));
        } else {
            levels.addAll(levelList);
        }

        List<Observable<RankCubeResult>> observableList = new ArrayList<>();
        levels.forEach(level -> observableList.add(
                Observable.create(emitter -> AsyncApi.getInstance().cubeRankList(level, new BaseApiCallback() {
                    @Override
                    public void onSuccess(String response) {
                        Gson gson = new Gson();
                        RankCubeResult rankCubeResult = gson.fromJson(response, RankCubeResult.class);
                        emitter.onNext(rankCubeResult);
                        emitter.onComplete();
                    }

                    @Override
                    public void onError(String response) {
                        emitter.onError(new ApiException(String.valueOf(level), "风云榜组合数据获取失败"));
                        emitter.onComplete();
                    }
                }))));

        Observable.merge(observableList)
                .subscribe(new Observer<RankCubeResult>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(RankCubeResult rankCubeResult) {
                        CubeDbHelper.getInstance().insertRankCube(rankCubeResult);
                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.println(e.getMessage());
                        if (e instanceof ApiException) {
                            errorLevels.add(Integer.valueOf(((ApiException) e).getSymbol()));
                        }
                    }

                    @Override
                    public void onComplete() {
                        if (errorLevels.size() == 0) {
                            System.out.println("雪球风云榜数据已更新完毕");
                        } else {
                            fetchCubeRankList(errorLevels);
                        }

                    }
                });
    }

    /**
     * 风云榜组合待更新
     */
    public void fetchCubeDetail() {
        List<Cube> cubeList = CubeDbHelper.getInstance().rankCubeListNeedUpdate();
        cubeList.forEach(dbCube -> AsyncApi.getInstance().fetchCubeDetail(dbCube.getSymbol(), new BaseApiCallback() {
            @Override
            public void onSuccess(String response) {
                Gson gson = new Gson();
                Cube cube = gson.fromJson(response, Cube.class);
                cube.setTableRowId(dbCube.getTableRowId());
                syncCubeData(cube, true);
            }
        }));
    }

    /**
     * 组合数据同步更新到数据库
     *
     * @param cube 组合数据
     * @param inDb 是否已入库
     */
    private boolean syncCubeData(Cube cube, boolean inDb) {
        Connection connection = null;
        boolean syncSuccess = false;
        try {
            connection = DBPoolConnection.getInstance().getConnection();
            //关闭外键约束
            connection.prepareStatement(SQLBuilder.buildForeignKeyCheck(false)).execute();
            connection.setAutoCommit(false);

            if (inDb) {
                //更新rank_cubes中待更新的组合数据
                PreparedStatement updateRankCubesPs =
                        connection.prepareStatement(SQLBuilder.buildRankCubeUpdate());
                updateRankCubesPs.setInt(1, cube.getViewRebalancing().getId());
                updateRankCubesPs.setInt(2, cube.getTableRowId());
                updateRankCubesPs.execute();
                updateRankCubesPs.close();
            }

            //查询调仓历史是异步操作，与这里的事务提交插入rebalancing可能造成死锁，改成同步
            fetchRebalancingHistory(connection, cube.getId(), 1);
            insertRebalancing(connection, cube.getViewRebalancing());
            insertHoldings(connection, cube.getViewRebalancing());
            insertAccount(connection, cube.getOwner());
            insertCube(connection, cube);
            //关闭外键约束
            connection.prepareStatement(SQLBuilder.buildForeignKeyCheck(true)).execute();
            connection.commit();
            syncSuccess = true;
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (null != connection) {
                    connection.rollback();
                }
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
            }
            syncSuccess = false;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != connection) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return syncSuccess;

    }

    /**
     * 查询当前需更新的组合列表插入Redis待更新组合队列
     */
    public void updatePendingCubeList() {
        List<Cube> cubes = CubeDbHelper.getInstance().fetchCubeList();
        RedisTask.insertCubeToPending(cubes);
    }

    /**
     * 重置更新组合队列
     */
    public void resetPendingCubeList() {
        RedisTask.resetPendingCube();
        updatePendingCubeList();
    }

    /**
     * 更新待更新组合
     */
    public void fetchPendingCubeDetail() {
        List<String> cubeSymbolList = RedisTask.oldestCubeToUpdate();
        //异步改同步，并发会导致数据库死锁
        List<Observable<Boolean>> observableList = new ArrayList<>();
        cubeSymbolList.forEach(symbol -> observableList.add(
                Observable.create(emitter -> AsyncApi.getInstance().fetchCubeDetail(symbol, new BaseApiCallback() {
                    @Override
                    public void onSuccess(String response) {
                        Gson gson = new Gson();
                        Cube cube = gson.fromJson(response, Cube.class);
                        if (syncCubeData(cube, false)) {
                            RedisTask.moveCubeFromPendingToSuccess(symbol);
                            emitter.onNext(true);
                        } else {
                            RedisTask.moveCubeFromPendingToFail(symbol);
                            emitter.onError(new ApiException("数据库操作异常"));
                        }
                        emitter.onComplete();
                    }

                    @Override
                    public void onError(String response) {
                        RedisTask.moveCubeFromPendingToFail(symbol);
                        Gson gson = new Gson();
                        ErrorResponse errorResponse = gson.fromJson(response, ErrorResponse.class);
                        if (errorResponse.getErrorDescription().equals("该组合不存在")) {
                            CubeDbHelper.getInstance().updateCubeRemoved(symbol);
                        }
                        emitter.onError(new ApiException("数据库操作异常"));
                        emitter.onComplete();
                    }
                }))));

        Observable.concat(observableList)
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(Boolean updateSuccess) {
                        System.out.println(updateSuccess);
                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.println(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("本次更新完毕");
                    }
                });

    }

    /**
     * 更新组合详情
     *
     * @param symbol 组合标识
     * @param inDB   是否入库
     */
    private void updateCubeDetail(String symbol, boolean inDB) {
        AsyncApi.getInstance().fetchCubeDetail(symbol, new BaseApiCallback() {
            @Override
            public void onSuccess(String response) {
                Gson gson = new Gson();
                Cube cube = gson.fromJson(response, Cube.class);
                boolean syncSuccess = syncCubeData(cube, false);
                if (inDB) {
                    if (syncSuccess) {
                        RedisTask.moveCubeFromPendingToSuccess(symbol);
                    } else {
                        RedisTask.moveCubeFromPendingToFail(symbol);
                    }
                }
            }

            @Override
            public void onError(String response) {
                if (inDB) {
                    RedisTask.moveCubeFromPendingToFail(symbol);
                }
            }
        });
    }

    /**
     * 更新组合详情
     *
     * @param symbol 组合代码
     * @return 执行结果
     */
    public boolean syncUpdateCubeDetail(String symbol) {
        return syncUpdateCubeDetail(symbol, false);
    }

    /**
     * 更新组合详情
     *
     * @param symbol 组合代码
     * @param inDB   是否已入库
     * @return 执行结果
     */
    public boolean syncUpdateCubeDetail(String symbol, boolean inDB) {
        String response = SyncApi.getInstance().fetchCubeDetail(symbol);
        if (response == null) {
            if (inDB) {
                RedisTask.moveCubeFromPendingToFail(symbol);
            }
            return false;
        }
        Gson gson = new Gson();
        Cube cube = gson.fromJson(response, Cube.class);
        boolean syncSuccess = syncCubeData(cube, false);
        if (inDB) {
            if (syncSuccess) {
                RedisTask.moveCubeFromPendingToSuccess(symbol);
            } else {
                RedisTask.moveCubeFromPendingToFail(symbol);
            }
        }
        return syncSuccess;
    }

    /**
     * 更新指定组合入库--用户输入未入库组合时更新
     *
     * @param symbol 组合代码
     */
    public void updateCubeDetail(String symbol) {
        updateCubeDetail(symbol, false);
    }

    /**
     * 搜索雪球组合
     *
     * @param cubeIds 组合id，形如19383,232323,43412
     * @return 查询结果
     */
    public LeekResult<Cube> fetchCubeList(String cubeIds) {
        Connection connection = null;
        LeekResult<Cube> leekResult = null;
        try {
            connection = DBPoolConnection.getInstance().getConnection();

            PreparedStatement queryPs = connection.prepareStatement(SQLBuilder.buildSpecifiedCubeQuery(cubeIds));
            ResultSet resultSet = queryPs.executeQuery();
            List<Cube> cubeList = new ArrayList<>();
            while (resultSet.next()) {
                Cube cube = new Cube();
                cube.setId(resultSet.getInt(1));
                cube.setName(resultSet.getString(2));
                cube.setSymbol(resultSet.getString(3));
                cube.setDescription(resultSet.getString(4));
                Account owner = new Account();
                owner.setId(resultSet.getLong(5));
                owner.setScreenName(resultSet.getString(10));
                owner.setPhotoDomain(resultSet.getString(11));
                owner.setProfileImageUrl(resultSet.getString(12));
                cube.setOwner(owner);
                cube.setFollowerCount(resultSet.getInt(6));
                cube.setNetValue(resultSet.getFloat(7));
                cube.setCreatedAt(resultSet.getLong(8));
                cube.setUpdatedAt(resultSet.getLong(9));
                cubeList.add(cube);
            }

            leekResult = new LeekResult<>();
            leekResult.setCount(cubeList.size());
            leekResult.setUpdatedAt(DateUtil.getInstance().getTimeNow());
            leekResult.setList(cubeList);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != connection) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return leekResult;
    }

    /**
     * 查询组合调仓历史并插入数据库
     *
     * @param cubeId 组合id
     */
    private void fetchRebalancingHistory(Connection connection, int cubeId, int page) throws IOException, SQLException, InterruptedException {
        String response = SyncApi.getInstance().fetchRebalancingHistory(cubeId, page);
        if (response != null) {
            Gson gson = new Gson();
            SnowBallListResult<Rebalancing> result = gson.fromJson(response,
                    new TypeToken<SnowBallListResult<Rebalancing>>() {
                    }.getType());

            Jedis redis = RedisUtil.getJedis();
            redis.select(5);
            redis.zadd("rebHistoryCount", result.getTotalCount(), String.valueOf(cubeId));
            redis.select(0);
            redis.close();

            if (result.getList() == null || result.getList().size() == 0) {
                return;
            }

            int temp = 0;
            boolean findLastInserted = false;
            //找调仓表中的最大和最小id
            int max = 0;
            int min = 0;
            int count = 0;
            PreparedStatement maxPs = connection.prepareStatement("SELECT MAX(`id`) AS `max` FROM " +
                    "`view_rebalancing` WHERE `cube_id` = ?;");
            maxPs.setInt(1, cubeId);
            ResultSet maxRs = maxPs.executeQuery();
            if (maxRs.next()) {
                max = maxRs.getInt(1);
            }
            maxPs.close();
            PreparedStatement minPs = connection.prepareStatement("SELECT MIN(`id`) AS `min` FROM " +
                    "`view_rebalancing` WHERE `cube_id` = ?;");
            minPs.setInt(1, cubeId);
            ResultSet minRs = minPs.executeQuery();
            if (minRs.next()) {
                min = minRs.getInt(1);
            }
            minPs.close();
            PreparedStatement countPs = connection.prepareStatement("SELECT COUNT(*) AS `count` FROM " +
                    "`view_rebalancing` WHERE `cube_id` = ?;");
            countPs.setInt(1, cubeId);
            ResultSet countRs = countPs.executeQuery();
            if (countRs.next()) {
                count = countRs.getInt(1);
            }
            countPs.close();
            for (Rebalancing rebalancing : result.getList()) {
                //如果调仓记录的上一条记录ID为0，代表该条记录已经是最后一条记录
                if (rebalancing.getPreRebalancingId() == 0) {
                    findLastInserted = true;
                    insertRebalancing(connection, rebalancing);
                    insertRebalancingHistory(connection, rebalancing);
                    break;
                }

                //调仓记录ID大于表中已存在的记录最大值或小于最小值，代表需要插入数据库
                if (rebalancing.getId() >= max || rebalancing.getId() < min) {
                    temp++;
                    insertRebalancing(connection, rebalancing);
                    insertRebalancingHistory(connection, rebalancing);
                    //插入记录数加上表中已有调仓记录数，大于组合调仓总数，代表调仓历史已全部插入完毕
                    if (count + temp > result.getTotalCount()) {
                        findLastInserted = true;
                        break;
                    }
                }
            }

            //未插入完毕的情况下，继续爬取下一页调仓数据
            if (!findLastInserted) {
                Thread.sleep(1000);
                fetchRebalancingHistory(connection, cubeId, page + 1);
            }
        }
    }

    /**
     * 插入调仓详情表
     *
     * @param connection db连接
     * @param account    用户
     */
    private void insertAccount(Connection connection, Account account) throws SQLException {
        if (account == null) {
            return;
        }

        PreparedStatement ps = connection.prepareStatement(SQLBuilder.buildAccountInsert());
        ps.setLong(1, account.getId());
        ps.setString(2, account.getScreenName());
        ps.setString(3, account.getDescription());
        ps.setInt(4, account.getFollowersCount());
        ps.setInt(5, account.getFriendsCount());
        ps.setString(6, account.getPhotoDomain());
        ps.setString(7, account.getProfileImageUrl());
        ps.execute();
        ps.close();
    }

    /**
     * 插入持仓记录表
     *
     * @param connection  db连接
     * @param rebalancing 调仓
     */
    private void insertRebalancing(Connection connection, Rebalancing rebalancing) throws SQLException {
        //插入持仓记录表
        PreparedStatement rebalancingInsertPs =
                connection.prepareStatement(SQLBuilder.buildRebalancingInsert());
        rebalancingInsertPs.setInt(1, rebalancing.getId());
        rebalancingInsertPs.setString(2, rebalancing.getStatus());
        rebalancingInsertPs.setInt(3, rebalancing.getCubeId());
        rebalancingInsertPs.setInt(4, rebalancing.getPreRebalancingId());
        rebalancingInsertPs.setString(5, rebalancing.getCategory());
        rebalancingInsertPs.setString(6, rebalancing.getExeStrategy());
        rebalancingInsertPs.setLong(7, rebalancing.getCreatedAt());
        rebalancingInsertPs.setLong(8, rebalancing.getUpdatedAt());
        rebalancingInsertPs.setDouble(9, rebalancing.getCashValue());
        rebalancingInsertPs.setDouble(10, rebalancing.getCash());
        rebalancingInsertPs.setString(11, rebalancing.getErrorCode());
        rebalancingInsertPs.setString(12, rebalancing.getErrorMessage());
        rebalancingInsertPs.setString(13, rebalancing.getErrorStatus());
        rebalancingInsertPs.setString(14, rebalancing.getComment());
        rebalancingInsertPs.setInt(15, rebalancing.getDiff());
        rebalancingInsertPs.setInt(16, rebalancing.getNewBuyCount());
        rebalancingInsertPs.execute();
        rebalancingInsertPs.close();
    }

    /**
     * 插入调仓详情表
     *
     * @param connection  db连接
     * @param rebalancing 当前持仓情况
     */
    private void insertHoldings(Connection connection, Rebalancing rebalancing) throws SQLException {
        if (rebalancing.getHoldings() == null || rebalancing.getHoldings().size() == 0) {
            return;
        }
        PreparedStatement holdingsInsertPs =
                connection.prepareStatement(SQLBuilder.buildHoldingsInsert());
        for (Holdings holdings : rebalancing.getHoldings()) {
            holdingsInsertPs.setInt(1, rebalancing.getId());
            holdingsInsertPs.setInt(2, holdings.getStockId());
            holdingsInsertPs.setDouble(3, holdings.getWeight());
            holdingsInsertPs.setString(4, holdings.getSegmentName());
            holdingsInsertPs.setInt(5, holdings.getSegmentId());
            //holding存在stock_name、stock_symbol的异常情况，修改相关字段可空
            holdingsInsertPs.setString(6, holdings.getStockName());
            holdingsInsertPs.setString(7, holdings.getStockSymbol());
            holdingsInsertPs.setString(8, holdings.getSegmentColor());
            holdingsInsertPs.setBoolean(9, holdings.isProactive());
            holdingsInsertPs.setDouble(10, holdings.getVolume());
            holdingsInsertPs.addBatch();
        }
        holdingsInsertPs.executeBatch();
        holdingsInsertPs.close();
    }

    /**
     * 插入调仓记录表
     *
     * @param connection  db连接
     * @param rebalancing 调仓历史
     */
    private void insertRebalancingHistory(Connection connection, Rebalancing rebalancing) throws SQLException {
        if (rebalancing.getRebalancingHistories() == null || rebalancing.getRebalancingHistories().size() == 0) {
            return;
        }
        PreparedStatement historyInsertPs =
                connection.prepareStatement(SQLBuilder.buildRebalancingHistoryInsert());
        for (RebalancingHistory history : rebalancing.getRebalancingHistories()) {
            historyInsertPs.setInt(1, rebalancing.getCubeId());
            historyInsertPs.setInt(2, history.getId());
            historyInsertPs.setInt(3, history.getRebalancingId());
            historyInsertPs.setInt(4, history.getStockId());
            historyInsertPs.setString(5, history.getStockName());
            historyInsertPs.setString(6, history.getStockSymbol());
            historyInsertPs.setDouble(7, history.getVolume());
            historyInsertPs.setDouble(8, history.getPrice());
            historyInsertPs.setDouble(9, history.getNetValue());
            historyInsertPs.setDouble(10, history.getWeight());
            historyInsertPs.setDouble(11, history.getTargetWeight());
            historyInsertPs.setDouble(12, history.getPrevWeight());
            historyInsertPs.setDouble(13, history.getPrevTargetWeight());
            historyInsertPs.setDouble(14, history.getPrevWeightAdjusted());
            historyInsertPs.setDouble(15, history.getPrevVolume());
            historyInsertPs.setDouble(16, history.getPrevPrice());
            historyInsertPs.setDouble(17, history.getPrevNetValue());
            historyInsertPs.setBoolean(18, history.isProactive());
            historyInsertPs.setLong(19, history.getCreatedAt());
            historyInsertPs.setLong(20, history.getUpdatedAt());
            historyInsertPs.setDouble(21, history.getTargetVolume());
            historyInsertPs.setDouble(22, history.getPrevTargetVolume());
            historyInsertPs.setDouble(23, history.getTargetWeight() - history.getPrevWeightAdjusted());
            historyInsertPs.addBatch();
        }
        historyInsertPs.executeBatch();
        historyInsertPs.close();
    }

    /**
     * 更新组合详情
     *
     * @param connection db连接
     * @param cube       组合
     */
    private void insertCube(Connection connection, Cube cube) throws SQLException {
        PreparedStatement insertCubePs = connection.prepareStatement(SQLBuilder.buildCubeDetailInsert());
        insertCubePs.setInt(1, cube.getId());
        insertCubePs.setString(2, cube.getName());
        insertCubePs.setString(3, cube.getSymbol());
        insertCubePs.setString(4, cube.getDescription());
        insertCubePs.setLong(5, cube.getOwnerId());
        insertCubePs.setInt(6, cube.getFollowerCount());
        insertCubePs.setBoolean(7, cube.isActiveFlag());
        insertCubePs.setLong(8, cube.getCreatedAt());
        insertCubePs.setLong(9, cube.getUpdatedAt());
        insertCubePs.setDouble(10, cube.getDailyGain());
        insertCubePs.setDouble(11, cube.getMonthlyGain());
        insertCubePs.setDouble(12, cube.getTotalGain());
        insertCubePs.setDouble(13, cube.getNetValue());
        insertCubePs.setDouble(14, cube.getRankPercent());
        insertCubePs.setString(15, cube.getTag() == null ? "" : cube.getTag().toString());
        insertCubePs.setInt(16, cube.getViewRebalancing().getId());
        insertCubePs.setLong(17, cube.getClosedAt());

        insertCubePs.execute();
        insertCubePs.close();
    }

}
