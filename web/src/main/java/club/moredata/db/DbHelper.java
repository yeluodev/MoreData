package club.moredata.db;

import club.moredata.entity.Account;
import club.moredata.util.DBPoolConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 数据库操作封装
 *
 * @author yeluodev1226
 */
public class DbHelper {

    /**
     * 插入用户到数据库
     *
     * @param account
     */
    public static void addAccount(Account account) {
        Connection connection = null;
        try {
            connection = DBPoolConnection.getInstance().getConnection();
            PreparedStatement addPs = connection.prepareStatement(UserSQL.completeInsert(true));
            setPsParams(addPs, account);
            addPs.execute();
            addPs.close();
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

    public static boolean addAccountList(List<Account> accountList) {
        Connection connection = null;
        boolean success = true;
        try {
            connection = DBPoolConnection.getInstance().getConnection();
            connection.setAutoCommit(false);
            PreparedStatement addPs = connection.prepareStatement(UserSQL.completeInsert(false));

            for (Account account : accountList) {
                setPsParams(addPs, account);
                addPs.addBatch();
            }
            int[] resultRows = addPs.executeBatch();
            connection.commit();
            addPs.close();

            for (int rows : resultRows) {
                if (rows == 0) {
                    success = false;
                    break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            success = false;
        } finally {
            try {
                if (null != connection) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return success;
    }

    private static void setPsParams(PreparedStatement ps, Account account) throws SQLException {
        ps.setLong(1, account.getId());
        ps.setInt(2, account.getFollowersCount());
        ps.setString(3, account.getScreenName());
        ps.setInt(4, account.getFriendsCount());
        ps.setString(5, account.getType());
        ps.setBoolean(6, account.isVerified());
        ps.setInt(7, account.getStocksCount());
        ps.setString(8, account.getPhotoDomain());
        ps.setString(9, account.getProfileImageUrl());
        ps.setInt(10, account.getCubeCount());
        ps.setBoolean(11, account.isVerifiedRealname());
        ps.setInt(12, account.getStatusCount());
        ps.setLong(13, account.getLastStatusId());
        ps.setString(14, account.getDescription());
        ps.setString(15, account.getProvince());
        ps.setString(16, account.getCity());
        ps.setString(17, account.getGender());
        ps.setInt(18, account.getFollowersCount() == 0 ? 1 : 0);
        ps.setBoolean(19, account.getFollowersCount() == 0);
    }

    /**
     * 查找用户
     *
     * @param uid
     * @return
     */
    public static Account findAccount(String uid) {
        Connection connection = null;
        Account account = new Account();
        try {
            connection = DBPoolConnection.getInstance().getConnection();
            PreparedStatement findPs = connection.prepareStatement(UserSQL.accountQuery());
            findPs.setString(1, uid);
            ResultSet resultSet = findPs.executeQuery();
            if (resultSet.next()) {
                account.setId(resultSet.getInt(1));
                account.setFollowersCount(resultSet.getInt(2));
                account.setScreenName(resultSet.getString(3));
                account.setFriendsCount(resultSet.getInt(4));
                account.setFinished(resultSet.getBoolean(21));
            }
            findPs.close();
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
        return account;
    }

    /**
     * 更新用户的部分信息
     *
     * @param uid
     * @param followersCount
     * @param page
     * @param maxPage
     * @param anonymousCount
     */
    public static void updateAccount(String uid, int followersCount, int page, int maxPage, int anonymousCount) {
        Connection connection = null;
        try {
            connection = DBPoolConnection.getInstance().getConnection();
            PreparedStatement updatePs = connection.prepareStatement(UserSQL.accountUpdate());
            updatePs.setInt(1, followersCount);
            updatePs.setInt(2, page);
            updatePs.setInt(3, maxPage);
            updatePs.setInt(4, anonymousCount);
            updatePs.setBoolean(5, page >= maxPage);
            updatePs.setString(6, uid);
            updatePs.executeUpdate();

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
