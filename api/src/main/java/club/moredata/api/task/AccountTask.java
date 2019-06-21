package club.moredata.api.task;

import club.moredata.api.model.AccountSection;
import club.moredata.api.model.LeekResult;
import club.moredata.common.db.sql.SQLBuilder;
import club.moredata.common.entity.Account;
import club.moredata.common.util.DBPoolConnection;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.PropertyFilter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author yeluodev1226
 */
public class AccountTask {

    private static String[] provinces = new String[]{"北京", "天津", "上海", "重庆", "河北", "河南", "云南", "辽宁",
            "黑龙江", "湖南", "安徽", "山东", "新疆", "江苏", "浙江", "江西", "湖北", "广西", "甘肃", "山西", "内蒙古", "陕西", "吉林", "福建",
            "贵州", "广东", "青海", "西藏", "四川", "宁夏", "海南", "台湾", "香港", "澳门"};
    private static int[] numbers = new int[]{159469, 10784, 117347, 15623, 14277, 21499, 6535, 15226, 6717, 20358,
            18443, 34276, 4755, 58426, 62955, 10982, 29550, 9658, 4724, 7848, 3621, 14615, 4684, 30764, 3453, 163794,
            784, 818, 31792, 1334, 3977, 1931, 13213, 1168};

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
     * TODO 用户数据变动很小，直接返回处理后的数据，不进行数据库操作
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
     * TODO
     *
     * @return
     */
    public LeekResult<AccountSection> accountFansListDealed() {
        List<AccountSection> genderList = new ArrayList<>();
        AccountSection section1 = new AccountSection();
        section1.setName("无人关注");
        section1.setCount(5911534);
        section1.setPercent(81.4366);
        AccountSection section2 = new AccountSection();
        section2.setName("1~10");
        section2.setCount(1164854);
        section2.setPercent(16.0469);
        AccountSection section3 = new AccountSection();
        section3.setName("11~100");
        section3.setCount(128151);
        section3.setPercent(1.7654);
        AccountSection section4 = new AccountSection();
        section4.setName("101~1000");
        section4.setCount(38269);
        section4.setPercent(0.5272);
        AccountSection section5 = new AccountSection();
        section5.setName("1001~10000");
        section5.setCount(11122);
        section5.setPercent(0.1532);
        AccountSection section6 = new AccountSection();
        section6.setName("10001~10万");
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
     * 活跃度区间分布
     * TODO
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
        section3.setName("11~100");
        section3.setCount(547036);
        AccountSection section4 = new AccountSection();
        section4.setName("101~1000");
        section4.setCount(114830);
        AccountSection section5 = new AccountSection();
        section5.setName(">1000");
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

    /**
     * 用户地域分布
     * TODO
     *
     * @return
     */
    public LeekResult<AccountSection> accountAreaListDealed() {
        List<AccountSection> areaList = new ArrayList<>();
        for (int i = 0; i < 34; i++) {
            AccountSection section = new AccountSection();
            section.setName(provinces[i]);
            section.setCount(numbers[i]);
            areaList.add(section);
        }

        Collections.sort(areaList);
        Collections.reverse(areaList);

        LeekResult<AccountSection> leekResult = new LeekResult<>();
        leekResult.setCount(7259061);
        leekResult.setUpdatedAt("2019-6-6 17:26:50");
        leekResult.setList(areaList);
        return leekResult;
    }

    /**
     * 用户排行
     * TODO type=2/3时，sql语句查询时间较长，未加索引
     * @param type 1/2/3
     * @return
     */
    public LeekResult<Account> accountRankList(int type) {
        Connection connection = null;
        List<Account> accountList = new ArrayList<>();
        LeekResult<Account> leekResult = null;
        try {
            connection = DBPoolConnection.getInstance().getConnection();
            String sql;
            if(type==1){
                sql = SQLBuilder.buildAccountRankQuery();
            }else if(type==2){
                sql = SQLBuilder.buildAccountRealFansRankQuery();
            }else {
                sql = SQLBuilder.buildAccountStatusRankQuery();
            }

            PreparedStatement queryPs = connection.prepareStatement(sql);
            ResultSet resultSet = queryPs.executeQuery();
            int rank = 0;
            while (resultSet.next()) {
                rank++;
                Account account = new Account();
                account.setRank(rank);
                account.setId(resultSet.getLong(1));
                account.setFollowersCount(resultSet.getInt(2));
                account.setScreenName(resultSet.getString(3));
                account.setFriendsCount(resultSet.getInt(4));
                account.setStocksCount(resultSet.getInt(7));
                account.setPhotoDomain(resultSet.getString(8));
                account.setProfileImageUrl(resultSet.getString(9));
                account.setStatusCount(resultSet.getInt(12));
                account.setDescription(resultSet.getString(14));
                account.setProvince(resultSet.getString(15));
                account.setGender(resultSet.getString(17));
                account.setRealFans(resultSet.getInt(22));
                accountList.add(account);
            }

            leekResult = new LeekResult<>();
            leekResult.setCount(500);
            leekResult.setUpdatedAt("2019-6-6 17:26:50");
            leekResult.setList(accountList);
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

    private static Pattern propertyPattern = Pattern.compile("list|count|updatedAt|id" +
            "|followersCount" +
            "|screenName|stocksCount|photoDomain" +
            "|profileImageUrl|statusCount|description|province|gender|realFans");

    public static void main(String[] args) {
        AccountTask task = new AccountTask();
//        System.out.println(task.countTotalAccount());
//        System.out.println(JSON.toJSONString(task.accountGenderListDealed()));
//        System.out.println(JSON.toJSONString(task.accountAreaListDealed()));
        PropertyFilter propertyFilter = (object, name, value) -> propertyPattern.matcher(name).matches();
        System.out.println(JSON.toJSONString(task.accountRankList(3), propertyFilter));
    }

}
