package club.moredata.task;

import club.moredata.api.ApiCallback;
import club.moredata.api.ApiManager;
import club.moredata.db.SQLBuilder;
import club.moredata.entity.*;
import club.moredata.util.DBPoolConnection;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.Response;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 雪球组合相关任务
 *
 * @author yeluodev1226
 */
public class CubeTask {

    public static void main(String[] args) {
        CubeTask task = new CubeTask();
//        task.fetchCubeRankListAndInsert2DB();
        task.fetchCubeDetailAndInsert2DB();
//        task.fetchRebalancingHistory(1356626);
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
                ownerPs.setString(4, owner.getPhotoDomain());
                ownerPs.setString(5, owner.getProfileImageUrl());
                ownerPs.setString(6, owner.getScreenName());
                ownerPs.setString(7, owner.getDescription());
                ownerPs.setString(8, owner.getPhotoDomain());
                ownerPs.setString(9, owner.getProfileImageUrl());
                ownerPs.addBatch();

                cubePs.setLong(1, cube.getId());
                cubePs.setString(2, cube.getName());
                cubePs.setString(3, cube.getSymbol());
                cubePs.setLong(4, cube.getOwnerId());
                cubePs.setInt(5, cube.getFollowerCount());
                cubePs.setString(6, cube.getName());
                cubePs.setString(7, cube.getSymbol());
                cubePs.setLong(8, cube.getOwnerId());
                cubePs.setInt(9, cube.getFollowerCount());
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
     * @param cubeId
     */
    private void fetchRebalancingHistory(int cubeId) {
        ApiManager.getInstance().fetchRebalancingHistory(cubeId, new ApiCallback() {
            @Override
            public void onResponse(String response) {
                System.out.println(response);
                Gson gson = new Gson();
                SnowBallListResult<Rebalancing> result = gson.fromJson(response,
                        new TypeToken<SnowBallListResult<Rebalancing>>() {
                        }.getType());
                if (result == null || result.getList() == null || result.getList().size() == 0) {
                    return;
                }
                Connection connection = null;
                try {
                    connection = DBPoolConnection.getInstance().getConnection();
                    for (Rebalancing rebalancing : result.getList()) {
                        insertRebalancing(connection, rebalancing);
                        insertRebalancingHistory(connection, rebalancing);
                    }
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

            }
        });
    }

    /**
     * 插入持仓记录表
     *
     * @param connection
     * @param rebalancing
     */
    private void insertRebalancing(Connection connection, Rebalancing rebalancing) {
        try {
            //插入持仓记录表
            PreparedStatement rebalancingInsertPs =
                    connection.prepareStatement(SQLBuilder.buildRebalancingInsert());
            rebalancingInsertPs.setInt(1, rebalancing.getId());
            rebalancingInsertPs.setString(2, rebalancing.getStatus());
            rebalancingInsertPs.setInt(3, rebalancing.getCubeId());
            rebalancingInsertPs.setInt(4, rebalancing.getPreRebalancingId());
            rebalancingInsertPs.setLong(5, rebalancing.getCreatedAt());
            rebalancingInsertPs.setLong(6, rebalancing.getUpdatedAt());
            rebalancingInsertPs.setDouble(7, rebalancing.getCashValue());
            rebalancingInsertPs.setDouble(8, rebalancing.getCash());
            rebalancingInsertPs.setString(9, rebalancing.getErrorCode());
            rebalancingInsertPs.setString(10, rebalancing.getErrorMessage());
            rebalancingInsertPs.setString(11, rebalancing.getErrorStatus());
            rebalancingInsertPs.execute();
            rebalancingInsertPs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 插入调仓详情表
     *
     * @param connection
     * @param rebalancing
     */
    private void insertHoldings(Connection connection, Rebalancing rebalancing) {
        try {
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
                holdingsInsertPs.setString(6, holdings.getStockName());
                holdingsInsertPs.setString(7, holdings.getStockSymbol());
                holdingsInsertPs.setString(8, holdings.getSegmentColor());
                holdingsInsertPs.setBoolean(9, holdings.isProactive());
                holdingsInsertPs.setDouble(10, holdings.getVolume());
                holdingsInsertPs.addBatch();
            }
            holdingsInsertPs.executeBatch();
            holdingsInsertPs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 插入调仓记录表
     *
     * @param connection
     * @param rebalancing
     */
    private void insertRebalancingHistory(Connection connection, Rebalancing rebalancing) {
        try {
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
                historyInsertPs.setDouble(7, history.getPrevWeightAdjusted());
                historyInsertPs.setDouble(8, history.getTargetWeight());
                historyInsertPs.setDouble(9, history.getTargetWeight() - history.getPrevWeightAdjusted());
                historyInsertPs.setLong(10, history.getUpdatedAt());
                historyInsertPs.addBatch();
            }
            historyInsertPs.executeBatch();
            historyInsertPs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新组合详情
     *
     * @param connection
     * @param cube
     */
    private void insertCube(Connection connection, Cube cube) {
        try {
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

            insertCubePs.execute();
            insertCubePs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新排行组合详情
     */
    public void fetchCubeDetailAndInsert2DB() {
        List<Cube> cubeList = cubesNeedFetchDetail();
        for (Cube dbCube : cubeList) {
            ApiManager.getInstance().fetchCubeDetail(dbCube.getSymbol(), new ApiCallback() {
                @Override
                public void onResponse(String response) {
                    System.out.println(response);
                    Gson gson = new Gson();
                    Cube cube = gson.fromJson(response, Cube.class);
                    Connection connection = null;
                    try {
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


                        //查询组合的持仓是否已插入表中
                        PreparedStatement queryRebalancingPs =
                                connection.prepareStatement(SQLBuilder.buildRebalancingQuery());
                        queryRebalancingPs.setInt(1, cube.getViewRebalancing().getId());
                        ResultSet rebalancingResultSet = queryRebalancingPs.executeQuery();
                        if (rebalancingResultSet.next()) {
                            System.out.println("已插入");
                            PreparedStatement queryHistoryPs =
                                    connection.prepareStatement(SQLBuilder.buildRebalancingHistoryQuery());
                            queryHistoryPs.setInt(1, cube.getViewRebalancing().getId());
                            ResultSet historyResultSet = queryHistoryPs.executeQuery();
                            if (!historyResultSet.next()) {
                                fetchRebalancingHistory(cube.getId());
                            }
                        } else {
                            System.out.println("未插入");
                            insertRebalancing(connection, cube.getViewRebalancing());
                            insertHoldings(connection, cube.getViewRebalancing());

                            fetchRebalancingHistory(cube.getId());
                        }
                        queryRebalancingPs.close();

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
            });
        }
    }

}
