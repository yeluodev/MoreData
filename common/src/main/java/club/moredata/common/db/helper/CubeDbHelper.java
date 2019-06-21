package club.moredata.common.db.helper;

import club.moredata.common.db.sql.CubeSQLBuilder;
import club.moredata.common.db.sql.SQLBuilder;
import club.moredata.common.entity.Account;
import club.moredata.common.entity.Cube;
import club.moredata.common.entity.RankCubeResult;
import club.moredata.common.util.DBPoolConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 组合数据库操作类
 *
 * @author yeluodev1226
 */
public class CubeDbHelper {

    private static CubeDbHelper instance;

    private CubeDbHelper() {
    }

    public static CubeDbHelper getInstance() {
        if (null == instance) {
            synchronized (CubeDbHelper.class) {
                if (null == instance) {
                    instance = new CubeDbHelper();
                }
            }
        }
        return instance;
    }

    /**
     * 将组合数据插入数据库表
     *
     * @param rankCubeResult 接口数据
     */
    public void insertRankCube(RankCubeResult rankCubeResult) {
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
     * @return 待更新组合列表
     */
    public List<Cube> rankCubeListNeedUpdate() {
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
     * 得到需要更新数据的组合列表
     *
     * @return 组合列表
     */
    public List<Cube> fetchCubeList() {
        Connection connection = null;
        List<Cube> cubeList = new ArrayList<>();
        try {
            connection = DBPoolConnection.getInstance().getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(CubeSQLBuilder.buildCubeListNeedUpdateQuery());
            ResultSet resultSet = preparedStatement.executeQuery();
            addResultToList(cubeList, resultSet);
            preparedStatement.close();

            PreparedStatement ps = connection.prepareStatement(CubeSQLBuilder.buildCubeRebalancingUnfinishedQuery());
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

    /**
     * 组合数据
     *
     * @param list 列表
     * @param rs   查询结果
     * @return 组合列表
     * @throws SQLException
     */
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
     * 更新组合为已删除，组合可能已被雪球删除或屏蔽搜索，接口返回不存在
     *
     * @param symbol 组合代码
     */
    public void updateCubeRemoved(String symbol) {
        Connection connection = null;
        try {
            connection = DBPoolConnection.getInstance().getConnection();
            PreparedStatement ps = connection.prepareStatement(CubeSQLBuilder.buildCubeRemovedUpdate());
            ps.setString(1, symbol);
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
