package club.moredata.db;

import club.moredata.util.DBPoolConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 数据库操作封装
 *
 * @author yeluodev1226
 */
public class DbHelper {

    public static void getCubeRank(int level, int page) {
        Connection connection = null;
        try {
            connection = DBPoolConnection.getInstance().getConnection();

            PreparedStatement preparedStatement = connection.prepareStatement(SQLBuilder.buildCubeRank());
            preparedStatement.setInt(1, level);
            preparedStatement.setInt(2, 20 * (page - 1));
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                System.out.println(resultSet.getString(2) + "-" + resultSet.getString(3));
            }

            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("数据库连接失败！");
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
