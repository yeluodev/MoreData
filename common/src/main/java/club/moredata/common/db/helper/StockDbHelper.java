package club.moredata.common.db.helper;

import club.moredata.common.db.sql.StockSQLBuilder;
import club.moredata.common.model.Stock;
import club.moredata.common.util.DBPoolConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 股票相关数据库操作类
 *
 * @author yeluodev1226
 */
public class StockDbHelper {

    private StockDbHelper() {
    }

    /**
     * 查询stock表中所有股票的代码
     *
     * @return 股票代码列表
     */
    public static List<String> symbolList() {
        List<String> list = new ArrayList<>();
        Connection connection = null;
        try {
            connection = DBPoolConnection.getInstance().getConnection();
            PreparedStatement ps = connection.prepareStatement(StockSQLBuilder.buildSymbolQuery());
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                list.add(resultSet.getString(1));
            }
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    /**
     * 查询数据库stock表，得到股票信息map
     *
     * @return key:symbol value:followers-statuses
     */
    public static Map<String, String> stockMap() {
        Map<String, String> map = new LinkedHashMap<>();
        Connection connection = null;

        try {
            connection = DBPoolConnection.getInstance().getConnection();
            PreparedStatement ps = connection.prepareStatement(StockSQLBuilder.buildStockQuery());
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                map.put(resultSet.getString(2),
                        resultSet.getString(1) + "-" + resultSet.getInt(3) + "-" + resultSet.getInt(4));
            }
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

        return map;
    }

    /**
     * 更新股票信息-关注人数/相关讨论数
     *
     * @param stock 股票信息
     */
    public static void updateStockStatuses(Stock stock) {
        Connection connection = null;
        try {
            connection = DBPoolConnection.getInstance().getConnection();
            PreparedStatement ps = connection.prepareStatement(StockSQLBuilder.buildStockUpdate());
            ps.setInt(1, stock.getFollowers());
            ps.setInt(2, stock.getStatuses());
            ps.setString(3, stock.getSymbol());
            ps.executeUpdate();
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
    }

}
