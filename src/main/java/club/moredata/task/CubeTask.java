package club.moredata.task;

import club.moredata.api.ApiCallback;
import club.moredata.api.ApiManager;
import club.moredata.db.SQLBuilder;
import club.moredata.entity.*;
import club.moredata.model.LeekResult;
import club.moredata.util.DBPoolConnection;
import club.moredata.util.DateUtil;
import club.moredata.util.RedisUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.Response;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 雪球组合相关任务
 *
 * @author yeluodev1226
 */
public class CubeTask {

    private int reqCount = 0;

    /**
     * 将所有组合插入Redis待更新队列
     */
    public void updatePendingList() {
        List<Cube> cubes = fetchCubeList();
        Jedis redis = RedisUtil.getJedis();
        redis.select(4);
        cubes.forEach(cube -> redis.zadd("pendingCube", cube.getFollowerCount(), cube.getSymbol()));
        redis.select(0);
        redis.close();
    }

    /**
     * 更新组合详情
     *
     * @param symbol 组合标识
     * @param inDB   是否入库
     */
    private void updateCubeDetail(String symbol, boolean inDB) {
        ApiManager.getInstance().fetchCubeDetail(symbol, new ApiCallback() {
            @Override
            public void onSuccess(String response) {
                Gson gson = new Gson();
                Cube cube = gson.fromJson(response, Cube.class);
                Connection connection = null;
                try {
                    connection = DBPoolConnection.getInstance().getConnection();
                    //关闭外键约束
                    connection.prepareStatement(SQLBuilder.buildForeignKeyCheck(false)).execute();
                    connection.setAutoCommit(false);
                    //事务提交后，再去抓取调仓历史，尽量避免异步操作多事务间造成死锁
                    fetchRebalancingHistory(connection, cube.getId(), 1);
                    insertRebalancing(connection, cube.getViewRebalancing());
                    insertHoldings(connection, cube.getViewRebalancing());
                    insertAccount(connection, cube.getOwner());
                    insertCube(connection, cube);
                    //关闭外键约束
                    connection.prepareStatement(SQLBuilder.buildForeignKeyCheck(true)).execute();
                    connection.commit();

                } catch (SQLException e) {
                    e.printStackTrace();
                    try {
                        if (null != connection) {
                            connection.rollback();
                        }
                    } catch (SQLException sqlException) {
                        sqlException.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
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
                if (inDB) {
                    moveRedisMember("fetchingCube", "successCube", symbol);
                    updateCubeDetail();
                }
            }

            @Override
            public void onError(String response) {
                if (inDB) {
                    moveRedisMember("fetchingCube", "failCube", symbol);
                    updateCubeDetail();
                }
            }
        });
    }

    /**
     * 转移集合成员
     *
     * @param source
     * @param des
     * @param member
     */
    private void moveRedisMember(String source, String des, String member) {
        Jedis redis = RedisUtil.getJedis();
        redis.select(4);
        redis.smove(source, des, member);
        redis.select(0);
        redis.close();
    }

    /**
     * 更新指定组合入库--用户输入未入库组合时更新
     *
     * @param symbol
     */
    public void updateCubeDetail(String symbol) {
        updateCubeDetail(symbol, false);
    }

    public static void main(String[] args) {
        CubeTask task = new CubeTask();
        task.updateCubeDetail("ZH715320");
//        task.fetchCubeList(Util.dealCubeIds("ZH1326651,ZH713034,ZH715320"));
    }

    /**
     * 查找redis待更新组合队列中最晚更新的10个组合
     */
    public void updateOldestUpdateCubes() {
        Jedis redis = RedisUtil.getJedis();
        redis.select(4);
        Set<String> set = redis.zrevrangeByScore("pendingCube", "+inf", "-inf", 0, 10);
        symbolList.addAll(set);
        redis.select(0);
        redis.close();
        updateCubeDetail();
    }

    private List<String> symbolList = new ArrayList<>();

    public void updateCubeDetail() {
        if (reqCount >= symbolList.size()) {
            return;
        }
        String symbol = symbolList.get(reqCount);
        Jedis redis = RedisUtil.getJedis();
        redis.select(4);
        redis.zrem("pendingCube", symbol);
        redis.select(0);
        redis.close();
        reqCount++;
        updateCubeDetail(symbol, true);
    }

    /**
     * 获取所有组合
     *
     * @return
     */
    private List<Cube> fetchCubeList() {
        Connection connection = null;
        List<Cube> cubeList = new ArrayList<>();
        try {
            connection = DBPoolConnection.getInstance().getConnection();
            //组合更新时间距当前时间超出6小时以上，添加入待更新队列，因为是redis有序列表，这里查询不需要排序，排除已关停组合(=0代表未关停)
            String sql = "SELECT `id`,`symbol`,TIMESTAMPDIFF(SECOND ,`latest_updated_at`,NOW()) AS `timediff` FROM " +
                    "`cube` WHERE TIMESTAMPDIFF(HOUR,`latest_updated_at`,NOW()) > 6 AND `closed_at` = 0;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            addResultToList(cubeList, resultSet);
            preparedStatement.close();

            //调仓历史未更新完毕的组合，因为是redis有序列表，这里查询不需要排序
            sql = "SELECT `id`,`symbol`,TIMESTAMPDIFF(SECOND ,`latest_updated_at`,NOW()) AS `timediff` FROM `cube` " +
                    "WHERE `id` IN (SELECT `cube_id` FROM `view_rebalancing` WHERE `cube_id` NOT IN (SELECT `cube_id` FROM `view_rebalancing` WHERE `prev_bebalancing_id` = 0) GROUP BY `cube_id`)";
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            addResultToList(cubeList, rs);
            ps.close();
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
        return cubeList;
    }

    private List<Cube> addResultToList(List<Cube> list, ResultSet rs) throws SQLException {
        while (rs.next()) {
            Cube cube = new Cube();
            cube.setId(rs.getInt(1));
            cube.setSymbol(rs.getString(2));
            //用follower_count字段暂时存储时间差（second）
            cube.setFollowerCount(rs.getInt(3));
            list.add(cube);
        }
        return list;
    }

    /**
     * 查询雪球风云榜排行组合数据入库
     */
    public void fetchCubeRankListAndInsert2DB() {
        for (int i = 1; i < 4; i++) {
            //这边使用同步请求，是为了避免异步导致数据库死锁
            Response response = ApiManager.getInstance().cubeRankList(i);
            if (response.code() >= 200 && response.code() < 300) {
                try {
                    String res = response.body().string();
                    Gson gson = new Gson();
                    RankCubeResult rankCubeResult = gson.fromJson(res, RankCubeResult.class);
                    insertRankCube(rankCubeResult);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 将组合数据插入数据库表
     *
     * @param rankCubeResult 接口数据
     */
    private void insertRankCube(RankCubeResult rankCubeResult) {
        if (rankCubeResult == null || rankCubeResult.getList() == null || rankCubeResult.getList().size() == 0) {
            return;
        }
        List<Cube> cubeList = rankCubeResult.getList();
        Connection connection = null;
        try {
            connection = DBPoolConnection.getInstance().getConnection();
            //关闭外键约束
            connection.prepareStatement(SQLBuilder.buildForeignKeyCheck(false)).execute();

            connection.setAutoCommit(false);
            PreparedStatement ownerPs = connection.prepareStatement(SQLBuilder.buildAccountInsert());
            PreparedStatement cubePs = connection.prepareStatement(SQLBuilder.buildCubeInsert());
            PreparedStatement rankPs = connection.prepareStatement(SQLBuilder.buildRankInsert());
            for (Cube cube : cubeList) {
                Account owner = cube.getOwner();

                ownerPs.setLong(1, owner.getId());
                ownerPs.setString(2, owner.getScreenName());
                ownerPs.setString(3, owner.getDescription());
                ownerPs.setInt(4, owner.getFollowersCount());
                ownerPs.setInt(5, owner.getFriendsCount());
                ownerPs.setString(6, owner.getPhotoDomain());
                ownerPs.setString(7, owner.getProfileImageUrl());
                ownerPs.addBatch();

                cubePs.setLong(1, cube.getId());
                cubePs.setString(2, cube.getName());
                cubePs.setString(3, cube.getSymbol());
                cubePs.setLong(4, cube.getOwnerId());
                cubePs.setInt(5, cube.getFollowerCount());
                cubePs.addBatch();

                rankPs.setInt(1, cube.getId());
                rankPs.setInt(2, cube.getCubeLevel());
                rankPs.setInt(3, cube.getRank());
                rankPs.setDouble(4, cube.getGainOnLevel());
                rankPs.addBatch();
            }

            ownerPs.executeBatch();
            cubePs.executeBatch();
            rankPs.executeBatch();

            connection.prepareStatement(SQLBuilder.buildForeignKeyCheck(true)).execute();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (null != connection) {
                    connection.rollback();
                }
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
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

    /**
     * 获取10条需要更新的组合
     *
     * @return
     */
    private List<Cube> cubesNeedFetchDetail() {
        Connection connection = null;
        List<Cube> cubeList = new ArrayList<>();
        try {
            connection = DBPoolConnection.getInstance().getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(SQLBuilder.buildUpdateCubeQuery());
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Cube cube = new Cube();
                cube.setTableRowId(resultSet.getInt(1));
                cube.setSymbol(resultSet.getString(2));
                cube.setId(resultSet.getInt(3));
                cubeList.add(cube);
            }
            preparedStatement.close();
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
        return cubeList;
    }

    /**
     * 查询组合调仓历史并插入数据库
     *
     * @param cubeId
     */
    private void fetchRebalancingHistory(Connection connection, int cubeId, int page) throws IOException, SQLException, InterruptedException {
        Response response = ApiManager.getInstance().fetchRebalancingHistory(cubeId, page);
        String res = response.body().string();
        System.out.println(res);
        if (response.code() >= 200 && response.code() < 300) {
            Gson gson = new Gson();
            SnowBallListResult<Rebalancing> result = gson.fromJson(res, new TypeToken<SnowBallListResult<Rebalancing>>() {
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
                if (rebalancing.getPreRebalancingId() == 0) {
                    findLastInserted = true;
                    insertRebalancing(connection, rebalancing);
                    insertRebalancingHistory(connection, rebalancing);
                    break;
                }

                if (rebalancing.getId() >= max || rebalancing.getId() < min) {
                    temp++;
                    insertRebalancing(connection, rebalancing);
                    insertRebalancingHistory(connection, rebalancing);
                    if (count + temp > result.getTotalCount()) {
                        findLastInserted = true;
                        break;
                    }
                }
            }

            if (!findLastInserted) {
                Thread.sleep(1000);
                fetchRebalancingHistory(connection, cubeId, page + 1);
            }
        }
    }

    /**
     * 插入调仓详情表
     *
     * @param connection
     * @param account
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
     * @param connection
     * @param rebalancing
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
     * @param connection
     * @param rebalancing
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
     * @param connection
     * @param rebalancing
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
     * @param connection
     * @param cube
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

    private void syncFetchCube(List<Cube> list, int index) {
        if (index > list.size() - 1) {
            return;
        }
        Cube dbCube = list.get(index);
        if (dbCube == null) {
            return;
        }
        Response response = ApiManager.getInstance().fetchCubeDetail(dbCube.getSymbol());
        if (response.code() >= 200 && response.code() < 300) {
            Connection connection = null;
            try {
                String res = response.body().string();
                Gson gson = new Gson();
                Cube cube = gson.fromJson(res, Cube.class);

                connection = DBPoolConnection.getInstance().getConnection();
                //关闭外键约束
                connection.prepareStatement(SQLBuilder.buildForeignKeyCheck(false)).execute();
                connection.setAutoCommit(false);

                //更新rank_cubes中待更新的组合数据
                PreparedStatement updateRankCubesPs =
                        connection.prepareStatement(SQLBuilder.buildRankCubeUpdate());
                updateRankCubesPs.setInt(1, cube.getViewRebalancing().getId());
                updateRankCubesPs.setInt(2, dbCube.getTableRowId());
                updateRankCubesPs.execute();
                updateRankCubesPs.close();
                //查询调仓历史是异步操作，与这里的事务提交插入rebalancing可能造成死锁，改成同步
                fetchRebalancingHistory(connection, cube.getId(), 1);
                insertRebalancing(connection, cube.getViewRebalancing());
                insertHoldings(connection, cube.getViewRebalancing());
                insertAccount(connection, cube.getOwner());
                insertCube(connection, cube);
                //关闭外键约束
                connection.prepareStatement(SQLBuilder.buildForeignKeyCheck(true)).execute();
                connection.commit();

                syncFetchCube(list, index + 1);

            } catch (SQLException e) {
                e.printStackTrace();
                try {
                    if (null != connection) {
                        connection.rollback();
                    }
                } catch (SQLException sqlException) {
                    sqlException.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
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

        }
    }


    /**
     * 更新排行组合详情
     */
    public void fetchCubeDetailAndInsert2DB() {
        List<Cube> cubeList = cubesNeedFetchDetail();
        syncFetchCube(cubeList, 0);
    }

    /**
     * 搜索雪球组合
     *
     * @param cubeIds 组合id，形如19383,232323,43412
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

}
