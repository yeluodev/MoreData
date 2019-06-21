package club.moredata.api.task;

import club.moredata.api.model.*;
import club.moredata.common.db.sql.OrderType;
import club.moredata.common.db.sql.RebalancingType;
import club.moredata.common.db.sql.SQLBuilder;
import club.moredata.common.entity.StockQuotep;
import club.moredata.common.net.ApiManager;
import club.moredata.common.util.Arith;
import club.moredata.common.util.DBPoolConnection;
import club.moredata.common.util.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import okhttp3.Response;

import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 数据分析任务
 *
 * @author yeluodev1226
 */
public class AnalysisTask {

    public static void main(String[] args) {
        AnalysisTask task = new AnalysisTask();
//        task.getCompleteBeforeId(1, 100);
//        task.getCompleteBeforeId(2, 100);
//        task.getCompleteBeforeId(1, 20);

//        task.getSuspensionIds(1, 20, 1000, task.getCompleteBeforeId(1, 20));
//        LeekResult<AlsStock> leekResult = task.stockRankList(1, 100, 3000, false, OrderType.WEIGHT_DESC);
//        LeekResult leekResult = task.rebalancingRankList(1, 100, 3000,
//                OrderType.CHANGE_WEIGHT_DESC, RebalancingType.ALL);
//
//        System.out.println(JSON.toJSONString(leekResult));
//        LeekResult<AlsSegment> leekResult = task.segmentRankList(1, 20, OrderType.WEIGHT_DESC);
//        System.out.println(JSON.toJSONString(leekResult));

//        LeekResult<AlsRebalancing> leekResult = task.rebalancingRankList(1, 30, 3000, OrderType.CHANGE_WEIGHT_DESC,
//                RebalancingType.ALL);
//        PropertyFilter propertyFilter = (obj, name, value) -> {
//            if(name.equalsIgnoreCase("cash")){
//                return false;
//            }
//            return true;
//        };
//        System.out.println(JSON.toJSONString(leekResult,propertyFilter));

//        LeekResult leekResult = task.cubeRankList(1, Integer.MAX_VALUE);
//        PropertyFilter propertyFilter = (obj, name, value) -> {
//            if(name.equalsIgnoreCase("cash")){
//                return false;
//            }
//            return true;
//        };
//        System.out.println(JSON.toJSONString(leekResult,propertyFilter));

//        task.getLatestUpdateTime(1, 30);
//        task.snowballCubeList(1);
        LeekResult result = task.stockRankList("1773085, 1359749, 1392200, 52627, 1423409, 1387791", 6,
                34, false, OrderType.WEIGHT_DESC);
        System.out.println(JSON.toJSONString(result));
    }

    /**
     * 获取还未完整更新组合详情的最小id，用于查询已完整更新的条件语句
     *
     * @param level
     * @param cubeLimit
     * @return
     */
    private int getCompleteBeforeId(int level, int cubeLimit) {
        Connection connection = null;
        int beforeId = 0;
        try {
            connection = DBPoolConnection.getInstance().getConnection();
            int lastId = Integer.MAX_VALUE;
            while (true) {
                PreparedStatement countPs = connection.prepareStatement(SQLBuilder.buildRankCubeUpdateCount());
                countPs.setInt(1, level);
                countPs.setInt(2, lastId);
                countPs.setInt(3, cubeLimit);
                ResultSet countResultSet = countPs.executeQuery();
                if (countResultSet.next()) {
                    int size = countResultSet.getInt(1);
                    if (size > 0) {
                        PreparedStatement queryPs = connection.prepareStatement(SQLBuilder.buildStartIdUpdateSeriesQuery());
                        queryPs.setInt(1, level);
                        queryPs.setInt(2, lastId);
                        ResultSet queryResultSet = queryPs.executeQuery();
                        if (queryResultSet.next()) {
                            beforeId = queryResultSet.getInt(1);
                            lastId = beforeId;
                        }
                        queryPs.close();
                    } else {
                        countPs.close();
                        break;
                    }
                }
                countPs.close();
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

        return beforeId;
    }

    /**
     * 获取个股停牌id
     *
     * @param level
     * @param cubeLimit
     * @param stockLimit
     * @param beforeId
     * @return
     */
    private String getSuspensionIds(int level, int cubeLimit, int stockLimit, int beforeId) {
        Connection connection = null;
        String suspensionIds = "";
        try {
            connection = DBPoolConnection.getInstance().getConnection();
            PreparedStatement stockIdQueryPs = connection.prepareStatement(SQLBuilder.buildRankStockIdsQuery());
            stockIdQueryPs.setInt(1, level);
            stockIdQueryPs.setInt(2, beforeId == 0 ? Integer.MAX_VALUE : beforeId);
            stockIdQueryPs.setInt(3, cubeLimit);
            ResultSet stockIdsResultSet = stockIdQueryPs.executeQuery();
            List<Integer> stockIdList = new ArrayList<>();
            while (stockIdsResultSet.next()) {
                stockIdList.add(stockIdsResultSet.getInt(1));
            }
            stockIdQueryPs.close();
            suspensionIds = fetchQuotep(stockIdList, stockLimit);
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

        return suspensionIds;
    }

    public String getSuspensionIds(String ids, int stockLimit) {
        Connection connection = null;
        String suspensionIds = "";
        try {
            connection = DBPoolConnection.getInstance().getConnection();
            PreparedStatement stockIdQueryPs =
                    connection.prepareStatement(SQLBuilder.buildSpecifiedCubeRankStockIdsQuery(ids));
            ResultSet stockIdsResultSet = stockIdQueryPs.executeQuery();
            List<Integer> stockIdList = new ArrayList<>();
            while (stockIdsResultSet.next()) {
                stockIdList.add(stockIdsResultSet.getInt(1));
            }
            stockIdQueryPs.close();

            suspensionIds = fetchQuotep(stockIdList, stockLimit);
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
        return suspensionIds;
    }

    private String fetchQuotep(List<Integer> stockIdList, int stockLimit) {
        String stockIds = "";
        String suspensionIds = "";
        int suspensionSize = 0;
        int limit = stockLimit > stockIdList.size() ? (stockIdList.size() - 1) : (stockLimit - 1);
        int tempCount = 0;
        for (Integer stockId : stockIdList) {
            if (tempCount < limit) {
                stockIds += stockId + "%2C";
                tempCount++;
                continue;
            } else if (tempCount == limit) {
                stockIds += stockId + "%2C";
            } else {
                stockIds = String.valueOf(stockId);
            }
            tempCount++;

            Response response = ApiManager.getInstance().fetchStockQuotep(stockIds);
            try {
                String res = response.body().string();
                System.out.println(res);
                JSONObject jsonObject = JSON.parseObject(res);
                for (String key : jsonObject.keySet()) {
                    Gson gson = new Gson();
                    StockQuotep stockQuotep = gson.fromJson(jsonObject.getString(key), StockQuotep.class);
                    if (stockQuotep.getFlag() == 2) {
                        if (suspensionSize == 0) {
                            suspensionIds = "'" + key + "'";
                        } else {
                            suspensionIds += ",'" + key + "'";
                        }
                        suspensionSize++;
                    }
                }
                if (tempCount - suspensionSize >= stockLimit) {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return suspensionIds;
    }

    /**
     * 组合个股比重排行
     *
     * @param level
     * @param cubeLimit
     * @param stockLimit
     * @param removeSuspensionStock
     * @param orderType
     * @return
     */
    public LeekResult<AlsStock> stockRankList(int level, int cubeLimit, int stockLimit, boolean removeSuspensionStock,
                                              OrderType orderType) {
        LeekResult<AlsStock> leekResult = null;
        int beforeId = getCompleteBeforeId(level, cubeLimit);
        String suspensionIds = removeSuspensionStock ? getSuspensionIds(level, cubeLimit, stockLimit, beforeId) : "";
        Connection connection = null;
        try {
            connection = DBPoolConnection.getInstance().getConnection();

            PreparedStatement totalPs = connection.prepareStatement(SQLBuilder.buildStockTotalQuery(suspensionIds,
                    beforeId, orderType));
            totalPs.setInt(1, level);
            totalPs.setInt(2, cubeLimit);
            ResultSet totalResultSet = totalPs.executeQuery();
            double cash = 0;
            int count = 0;
            if (totalResultSet.next()) {
                cash = Arith.mul(Arith.sub(1, Arith.div(totalResultSet.getDouble(1), cubeLimit * 100.0, 8)), 100);
                count = totalResultSet.getInt(2);
            }
            totalPs.close();

            PreparedStatement leekStockPs = connection.prepareStatement(SQLBuilder.buildStockRankQuery(suspensionIds,
                    beforeId, orderType));
            leekStockPs.setInt(1, level);
            leekStockPs.setInt(2, cubeLimit);
            leekStockPs.setInt(3, stockLimit);
            leekStockPs.setInt(4, level);
            leekStockPs.setInt(5, cubeLimit);
            leekStockPs.setInt(6, stockLimit);
            ResultSet resultSet = leekStockPs.executeQuery();
            int rank = 0;
            List<AlsStock> alsStockList = new ArrayList<>();
            while (resultSet.next()) {
                rank++;
                AlsStock alsStock = new AlsStock();
                alsStock.setRank(rank);
                alsStock.setStockName(resultSet.getString(1));
                alsStock.setStockId(resultSet.getInt(2));
                alsStock.setStockSymbol(resultSet.getString(3));
                alsStock.setSegmentName(resultSet.getString(4));
                alsStock.setSegmentColor(resultSet.getString(5));
                alsStock.setWeight(resultSet.getDouble(6));
                alsStock.setCount(resultSet.getInt(7));
                alsStock.setPercent(resultSet.getDouble(8));
                double percentWithCash = Arith.div(Arith.mul(Arith.sub(100, cash), resultSet.getDouble(8)), 100, 4);
                alsStock.setPercentWithCash(percentWithCash);
                alsStockList.add(alsStock);
            }
            leekStockPs.close();

            leekResult = new LeekResult<>();
            leekResult.setCash(cash);
            leekResult.setCount(count);
            leekResult.setUpdatedAt(getLatestUpdateTime(level, cubeLimit));
            leekResult.setList(alsStockList);

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
     * 组合板块比重排行
     *
     * @param level
     * @param cubeLimit
     * @param orderType
     * @return
     */
    public LeekResult<AlsSegment> segmentRankList(int level, int cubeLimit, OrderType orderType) {
        int beforeId = getCompleteBeforeId(level, cubeLimit);
        Connection connection = null;
        LeekResult<AlsSegment> leekResult = null;
        try {
            connection = DBPoolConnection.getInstance().getConnection();

            PreparedStatement leekSegmentPs = connection.prepareStatement(SQLBuilder.buildSegmentRankQuery(beforeId,
                    orderType));
            leekSegmentPs.setInt(1, level);
            leekSegmentPs.setInt(2, cubeLimit);
            leekSegmentPs.setInt(3, level);
            leekSegmentPs.setInt(4, cubeLimit);
            ResultSet resultSet = leekSegmentPs.executeQuery();
            int rank = 0;
            List<AlsSegment> alsSegmentList = new ArrayList<>();
            while (resultSet.next()) {
                rank++;
                AlsSegment alsSegment = new AlsSegment();
                alsSegment.setRank(rank);
                alsSegment.setSegmentName(resultSet.getString(1));
                alsSegment.setSegmentColor(resultSet.getString(2));
                alsSegment.setWeight(resultSet.getDouble(3));
                alsSegment.setCount(resultSet.getInt(4));
                alsSegment.setPercent(resultSet.getDouble(5));
                alsSegment.setPercentWithCash(Arith.mul(Arith.div(resultSet.getDouble(3), cubeLimit * 100.0, 8), 100));
                alsSegmentList.add(alsSegment);
            }
            leekSegmentPs.close();

            PreparedStatement totalPs = connection.prepareStatement(SQLBuilder.buildSegmentTotalQuery(beforeId, orderType));
            totalPs.setInt(1, level);
            totalPs.setInt(2, cubeLimit);
            ResultSet totalResultSet = totalPs.executeQuery();
            double cash = 0;
            int count = 0;
            if (totalResultSet.next()) {
                cash = Arith.mul(Arith.sub(1, Arith.div(totalResultSet.getDouble(1), cubeLimit * 100.0, 8)), 100);
                count = totalResultSet.getInt(2);
            }
            totalPs.close();

            leekResult = new LeekResult<>();
            leekResult.setCash(cash);
            leekResult.setCount(count);
            leekResult.setUpdatedAt(getLatestUpdateTime(level, cubeLimit));
            leekResult.setList(alsSegmentList);

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
     * 组合调仓排行
     *
     * @param level
     * @param cubeLimit
     * @param stockLimit
     * @param orderType
     * @return
     */
    public LeekResult<AlsRebalancing> rebalancingRankList(int level, int cubeLimit, int stockLimit,
                                                          OrderType orderType, RebalancingType rebalancingType) {
        int beforeId = getCompleteBeforeId(level, cubeLimit);
        String diaplayDate = DateUtil.getInstance().transactionDataDate(System.currentTimeMillis());
        Connection connection = null;
        LeekResult<AlsRebalancing> leekResult = null;
        try {
            connection = DBPoolConnection.getInstance().getConnection();

            Date date = new SimpleDateFormat("yyyyMMdd").parse(diaplayDate);
            long startTimestamp = DateUtil.getInstance().getDayStartTimestamp(date);
            long endTimestamp = DateUtil.getInstance().getDayEndTimestamp(date);
            PreparedStatement rebalancingPs = connection.prepareStatement(SQLBuilder.buildRebalancingRankQuery(beforeId,
                    orderType, rebalancingType));
            rebalancingPs.setInt(1, level);
            rebalancingPs.setInt(2, cubeLimit);
            rebalancingPs.setLong(3, startTimestamp);
            rebalancingPs.setLong(4, endTimestamp);
            rebalancingPs.setInt(5, stockLimit);
            ResultSet resultSet = rebalancingPs.executeQuery();
            int rank = 0;
            List<AlsRebalancing> alsRebalancingList = new ArrayList<>();
            while (resultSet.next()) {
                rank++;
                AlsRebalancing alsRebalancing = new AlsRebalancing();
                alsRebalancing.setRank(rank);
                alsRebalancing.setStockName(resultSet.getString(1));
                alsRebalancing.setStockSymbol(resultSet.getString(2));
                alsRebalancing.setChangWeight(resultSet.getDouble(3));
                alsRebalancing.setPercent(Arith.div(resultSet.getDouble(3), cubeLimit, 8));
                alsRebalancingList.add(alsRebalancing);
            }
            rebalancingPs.close();

            leekResult = new LeekResult<>();
            leekResult.setCount(alsRebalancingList.size());
            leekResult.setUpdatedAt(getLatestUpdateTime(level, cubeLimit));
            leekResult.setList(alsRebalancingList);
        } catch (SQLException | ParseException e) {
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
     * 组合在榜天数排行
     *
     * @param level
     * @param cubeLimit
     * @return
     */
    public LeekResult<AlsCube> cubeRankList(int level, int cubeLimit) {
        Connection connection = null;
        LeekResult<AlsCube> leekResult = null;
        try {
            connection = DBPoolConnection.getInstance().getConnection();

            PreparedStatement cubePs = connection.prepareStatement(SQLBuilder.buildCubeShowDaysRankQuery());
            cubePs.setInt(1, level);
            cubePs.setInt(2, cubeLimit);
            ResultSet resultSet = cubePs.executeQuery();
            int rank = 0;
            List<AlsCube> alsCubeList = new ArrayList<>();
            while (resultSet.next()) {
                rank++;
                AlsCube alsCube = new AlsCube();
                alsCube.setRank(rank);
                alsCube.setName(resultSet.getString(1));
                alsCube.setSymbol(resultSet.getString(2));
                alsCube.setNetValue(resultSet.getDouble(3));
                alsCube.setOwnerId(resultSet.getLong(4));
                alsCube.setScreenName(resultSet.getString(5));
                alsCube.setPhotoDomain(resultSet.getString(6));
                alsCube.setProfileImageUrl(resultSet.getString(7));
                alsCube.setShowDaysCount(resultSet.getInt(8));
                alsCube.setGainOnLevel(resultSet.getDouble(9));
                alsCubeList.add(alsCube);
            }
            cubePs.close();

            leekResult = new LeekResult<>();
            leekResult.setCount(alsCubeList.size());
            leekResult.setUpdatedAt(getLatestUpdateTime(level, cubeLimit));
            leekResult.setList(alsCubeList);
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
     * 最新一期雪球风云榜
     *
     * @param level
     * @return
     */
    public LeekResult<AlsCube> snowballCubeList(int level) {
        int beforeId = getCompleteBeforeId(level, 100);
        Connection connection = null;
        LeekResult<AlsCube> leekResult = null;
        try {
            connection = DBPoolConnection.getInstance().getConnection();
            PreparedStatement snowballPs =
                    connection.prepareStatement(SQLBuilder.buildSnowballCubeQuery(beforeId));
            snowballPs.setInt(1, level);
            ResultSet resultSet = snowballPs.executeQuery();
            List<AlsCube> alsCubeList = new ArrayList<>();
            while (resultSet.next()) {
                AlsCube alsCube = new AlsCube();
                alsCube.setRank(resultSet.getInt(1));
                alsCube.setName(resultSet.getString(2));
                alsCube.setSymbol(resultSet.getString(3));
                alsCube.setNetValue(resultSet.getDouble(4));
                alsCube.setOwnerId(resultSet.getLong(5));
                alsCube.setScreenName(resultSet.getString(6));
                alsCube.setPhotoDomain(resultSet.getString(7));
                alsCube.setProfileImageUrl(resultSet.getString(8));
                alsCube.setGainOnLevel(resultSet.getDouble(9));
                alsCubeList.add(alsCube);
            }
            snowballPs.close();

            leekResult = new LeekResult<>();
            leekResult.setCount(alsCubeList.size());
            leekResult.setUpdatedAt(getLatestUpdateTime(level, 100));
            leekResult.setList(alsCubeList);
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
     * 更新时间
     *
     * @param level
     * @param cubeLimit
     * @return
     */
    public String getLatestUpdateTime(int level, int cubeLimit) {
        int beforeId = getCompleteBeforeId(level, cubeLimit);
        Connection connection = null;
        String updateTime = "";
        try {
            connection = DBPoolConnection.getInstance().getConnection();
            PreparedStatement updateTimePs =
                    connection.prepareStatement(SQLBuilder.buildLatestUpdateTimeQuery(beforeId));
            updateTimePs.setInt(1, level);
            ResultSet resultSet = updateTimePs.executeQuery();
            if (resultSet.next()) {
                Timestamp timestamp = resultSet.getTimestamp(1);
                updateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(timestamp.getTime()));
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
        return updateTime;
    }

    /**
     * 指定组合个股排行
     *
     * @param cubeIds
     * @param cubeSize
     * @param stockLimit
     * @param removeSuspensionStock
     * @param orderType
     * @return
     */
    public LeekResult<AlsStock> stockRankList(String cubeIds, int cubeSize, int stockLimit,
                                              boolean removeSuspensionStock,
                                              OrderType orderType) {
        LeekResult<AlsStock> leekResult = null;
        Connection connection = null;

        String suspensionIds;
        try {
            connection = DBPoolConnection.getInstance().getConnection();

            PreparedStatement stockIdQueryPs =
                    connection.prepareStatement(SQLBuilder.buildSpecifiedCubeRankStockIdsQuery(cubeIds));
            ResultSet stockIdsResultSet = stockIdQueryPs.executeQuery();
            List<Integer> stockIdList = new ArrayList<>();
            double totalWeight = 0;
            double cash = 0;
            while (stockIdsResultSet.next()) {
                stockIdList.add(stockIdsResultSet.getInt(1));
                totalWeight = Arith.add(totalWeight, stockIdsResultSet.getDouble(2));
            }
            cash = Arith.mul(Arith.sub(1, Arith.div(totalWeight, cubeSize * 100.0, 8)), 100);
            stockIdQueryPs.close();
            //是否移除停牌个股
            suspensionIds = removeSuspensionStock ? fetchQuotep(stockIdList, stockLimit) : "";

            PreparedStatement leekStockPs =
                    connection.prepareStatement(SQLBuilder.buildSpecifiedCubeStockRankQuery(cubeIds, suspensionIds, orderType));
            leekStockPs.setInt(1, stockLimit);
            leekStockPs.setInt(2, stockLimit);
            ResultSet resultSet = leekStockPs.executeQuery();
            int rank = 0;
            List<AlsStock> alsStockList = new ArrayList<>();
            while (resultSet.next()) {
                rank++;
                AlsStock alsStock = new AlsStock();
                alsStock.setRank(rank);
                alsStock.setStockName(resultSet.getString(1));
                alsStock.setStockId(resultSet.getInt(2));
                alsStock.setStockSymbol(resultSet.getString(3));
                alsStock.setSegmentName(resultSet.getString(4));
                alsStock.setSegmentColor(resultSet.getString(5));
                alsStock.setWeight(resultSet.getDouble(6));
                alsStock.setCount(resultSet.getInt(7));
                alsStock.setPercent(resultSet.getDouble(8) > 100 ? 100 : resultSet.getDouble(8));
                double percentWithCash = Arith.div(Arith.mul(Arith.sub(100, cash), resultSet.getDouble(8)), 100, 6);
                alsStock.setPercentWithCash(percentWithCash);
                alsStockList.add(alsStock);
            }
            leekStockPs.close();

            leekResult = new LeekResult<>();
            leekResult.setCash(cash);
            leekResult.setCount(alsStockList.size());
            leekResult.setUpdatedAt(DateUtil.getInstance().getTimeNow());
            leekResult.setList(alsStockList);

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
     * 组合板块比重排行
     *
     * @param cubeIds
     * @param cubeSize
     * @param orderType
     * @return
     */
    public LeekResult<AlsSegment> segmentRankList(String cubeIds, int cubeSize, OrderType orderType) {
        LeekResult<AlsSegment> leekResult = null;
        Connection connection = null;

        try {
            connection = DBPoolConnection.getInstance().getConnection();

            PreparedStatement leekSegmentPs =
                    connection.prepareStatement(SQLBuilder.buildSpecifiedCubeSegmentRankQuery(cubeIds));
            ResultSet resultSet = leekSegmentPs.executeQuery();
            int rank = 0;
            List<AlsSegment> alsSegmentList = new ArrayList<>();
            while (resultSet.next()) {
                rank++;
                AlsSegment alsSegment = new AlsSegment();
                alsSegment.setRank(rank);
                alsSegment.setSegmentName(resultSet.getString(1));
                alsSegment.setSegmentColor(resultSet.getString(2));
                alsSegment.setWeight(resultSet.getDouble(3));
                alsSegment.setCount(resultSet.getInt(4));
                alsSegment.setPercent(resultSet.getDouble(5));
                alsSegment.setPercentWithCash(Arith.mul(Arith.div(resultSet.getDouble(3), cubeSize * 100.0, 8),
                        100));
                alsSegmentList.add(alsSegment);
            }
            leekSegmentPs.close();

            PreparedStatement stockIdQueryPs =
                    connection.prepareStatement(SQLBuilder.buildSpecifiedCubeRankStockIdsQuery(cubeIds));
            ResultSet stockIdsResultSet = stockIdQueryPs.executeQuery();
            double totalWeight = 0;
            double cash = 0;
            while (stockIdsResultSet.next()) {
                totalWeight = Arith.add(totalWeight, stockIdsResultSet.getDouble(2));
            }
            cash = Arith.mul(Arith.sub(1, Arith.div(totalWeight, cubeSize * 100.0, 8)), 100);
            stockIdQueryPs.close();

            leekResult = new LeekResult<>();
            leekResult.setCash(cash);
            leekResult.setCount(alsSegmentList.size());
            leekResult.setUpdatedAt(DateUtil.getInstance().getTimeNow());
            leekResult.setList(alsSegmentList);

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
     * 组合调仓排行
     *
     * @param cubeIds
     * @param cubeSize
     * @param orderType
     * @param rebalancingType
     * @return
     */
    public LeekResult<AlsRebalancing> rebalancingRankList(String cubeIds, int cubeSize, OrderType orderType,
                                                          RebalancingType rebalancingType) {
        String diaplayDate = DateUtil.getInstance().transactionDataDate(System.currentTimeMillis());
        Connection connection = null;
        LeekResult<AlsRebalancing> leekResult = null;
        try {
            connection = DBPoolConnection.getInstance().getConnection();

            Date date = new SimpleDateFormat("yyyyMMdd").parse(diaplayDate);
            long startTimestamp = DateUtil.getInstance().getDayStartTimestamp(date);
            long endTimestamp = DateUtil.getInstance().getDayEndTimestamp(date);
            PreparedStatement rebalancingPs = connection.prepareStatement(SQLBuilder.buildSpecifiedCubeRebalancingRankQuery(cubeIds, orderType, rebalancingType));
            rebalancingPs.setLong(1, startTimestamp);
            rebalancingPs.setLong(2, endTimestamp);
            ResultSet resultSet = rebalancingPs.executeQuery();
            int rank = 0;
            List<AlsRebalancing> alsRebalancingList = new ArrayList<>();
            while (resultSet.next()) {
                rank++;
                AlsRebalancing alsRebalancing = new AlsRebalancing();
                alsRebalancing.setRank(rank);
                alsRebalancing.setStockName(resultSet.getString(1));
                alsRebalancing.setStockSymbol(resultSet.getString(2));
                alsRebalancing.setChangWeight(resultSet.getDouble(3));
                alsRebalancing.setPercent(Arith.div(resultSet.getDouble(3), cubeSize, 8));
                alsRebalancingList.add(alsRebalancing);
            }
            rebalancingPs.close();

            leekResult = new LeekResult<>();
            leekResult.setCount(alsRebalancingList.size());
            leekResult.setUpdatedAt(DateUtil.getInstance().getTimeNow());
            leekResult.setList(alsRebalancingList);
        } catch (SQLException | ParseException e) {
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
