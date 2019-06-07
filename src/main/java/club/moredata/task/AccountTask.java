package club.moredata.task;

import club.moredata.db.SQLBuilder;
import club.moredata.model.LeekResult;
import club.moredata.model.AccountSection;
import club.moredata.util.DBPoolConnection;
import com.alibaba.fastjson.JSON;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AccountTask {

    public static void main(String[] args) {
        AccountTask task = new AccountTask();
//        System.out.println(task.countTotalAccount());
        System.out.println(JSON.toJSONString(task.accountGenderListDealed()));
    }

    /**
     * 查询雪球网账户总数
     *
     * @return
     */
    public int countTotalAccount() {
        Connection connection = null;
        int count = 0;
        try {
            connection = DBPoolConnection.getInstance().getConnection();
            PreparedStatement queryPs = connection.prepareStatement(SQLBuilder.buildAccountQuery());
            ResultSet resultSet = queryPs.executeQuery();
            if (resultSet.next()) {
                count = resultSet.getInt(1);
            }
            queryPs.close();
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
        return count;
    }

    /**
     * 性别分布
     *
     * @return
     */
    public LeekResult<AccountSection> accountGenderList() {
        Connection connection = null;
        LeekResult<AccountSection> leekResult = null;
        try {
            connection = DBPoolConnection.getInstance().getConnection();
            PreparedStatement queryPs = connection.prepareStatement(SQLBuilder.buildAccountGenderQuery());
            ResultSet resultSet = queryPs.executeQuery();
            List<AccountSection> genderList = new ArrayList<>();
            while (resultSet.next()) {
                AccountSection gender = new AccountSection();
                switch (resultSet.getString(1)) {
                    case "m":
                        gender.setName("男性");
                        break;
                    case "f":
                        gender.setName("女性");
                        break;
                    case "n":
                    default:
                        gender.setName("保密");
                        break;
                }
                gender.setCount(resultSet.getInt(2));
                gender.setPercent(resultSet.getDouble(3));
                genderList.add(gender);
            }

            leekResult = new LeekResult<>();
            leekResult.setCount(7259061);
            leekResult.setUpdatedAt("2019-6-6 17:26:50");
            leekResult.setList(genderList);
            queryPs.close();
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
     * 性别分布
     *
     * @return
     */
    public LeekResult<AccountSection> accountGenderListDealed() {
        List<AccountSection> genderList = new ArrayList<>();
        AccountSection gender1 = new AccountSection();
        gender1.setName("男性");
        gender1.setCount(415452);
        gender1.setPercent(5.7232);
        AccountSection gender2 = new AccountSection();
        gender2.setName("女性");
        gender2.setCount(87066);
        gender2.setPercent(1.1994);
        AccountSection gender3 = new AccountSection();
        gender3.setName("保密");
        gender3.setCount(6756543);
        gender3.setPercent(93.0774);
        genderList.add(gender1);
        genderList.add(gender2);
        genderList.add(gender3);

        LeekResult<AccountSection> leekResult = new LeekResult<>();
        leekResult.setCount(7259061);
        leekResult.setUpdatedAt("2019-6-6 17:26:50");
        leekResult.setList(genderList);
        return leekResult;
    }

    /**
     * 粉丝值区间分布
     *
     * @return
     */
    public LeekResult<AccountSection> accountFansListDealed() {
        List<AccountSection> genderList = new ArrayList<>();
        AccountSection section1 = new AccountSection();
        section1.setName("无人关注者");
        section1.setCount(5911534);
        section1.setPercent(81.4366);
        AccountSection section2 = new AccountSection();
        section2.setName("1~10");
        section2.setCount(1164854);
        section2.setPercent(16.0469);
        AccountSection section3 = new AccountSection();
        section3.setName("10~100");
        section3.setCount(128151);
        section3.setPercent(1.7654);
        AccountSection section4 = new AccountSection();
        section4.setName("100~1000");
        section4.setCount(38269);
        section4.setPercent(0.5272);
        AccountSection section5 = new AccountSection();
        section5.setName("1000~10000");
        section5.setCount(11122);
        section5.setPercent(0.1532);
        AccountSection section6 = new AccountSection();
        section6.setName("10000~10万");
        section6.setCount(5009);
        section6.setPercent(0.0690);
        AccountSection section7 = new AccountSection();
        section7.setName(">10万");
        section7.setCount(122);
        section7.setPercent(0.0017);
        genderList.add(section1);
        genderList.add(section2);
        genderList.add(section3);
        genderList.add(section4);
        genderList.add(section5);
        genderList.add(section6);
        genderList.add(section7);

        LeekResult<AccountSection> leekResult = new LeekResult<>();
        leekResult.setCount(7259061);
        leekResult.setUpdatedAt("2019-6-6 17:26:50");
        leekResult.setList(genderList);
        return leekResult;
    }

    /**
     * 粉丝值区间分布
     *
     * @return
     */
    public LeekResult<AccountSection> accountActivationListDealed() {
        List<AccountSection> genderList = new ArrayList<>();
        AccountSection section1 = new AccountSection();
        section1.setName("无发言");
        section1.setCount(4958086);
        AccountSection section2 = new AccountSection();
        section2.setName("1~10");
        section2.setCount(1630273);
        AccountSection section3 = new AccountSection();
        section3.setName("10~100");
        section3.setCount(547036);
        AccountSection section4 = new AccountSection();
        section4.setName("100~1000");
        section4.setCount(114830);
        AccountSection section5 = new AccountSection();
        section5.setName("1000~10000");
        section5.setCount(8836);
        genderList.add(section1);
        genderList.add(section2);
        genderList.add(section3);
        genderList.add(section4);
        genderList.add(section5);

        LeekResult<AccountSection> leekResult = new LeekResult<>();
        leekResult.setCount(7259061);
        leekResult.setUpdatedAt("2019-6-6 17:26:50");
        leekResult.setList(genderList);
        return leekResult;
    }

}
