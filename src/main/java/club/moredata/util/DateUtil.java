package club.moredata.util;

import club.moredata.db.SQLBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期工具类
 *
 * @author yeluodev1226
 */
public class DateUtil {

    private static long TIMESTAMP_MILLS_ONE_DAY = 60 * 60 * 24 * 1000L;

    private static DateUtil instance;

    private DateUtil() {
    }

    public static DateUtil getInstance() {
        if (instance == null) {
            synchronized (DateUtil.class) {
                instance = new DateUtil();
            }
        }
        return instance;
    }

    /**
     * 根据时间戳获取日历对象
     *
     * @param timestamp
     * @return
     */
    private Calendar getCalendar(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        return calendar;
    }

    /**
     * 获取所给时间戳是周几
     *
     * @param timestamp
     * @return
     */
    public int getWeekDayIndex(long timestamp) {
        Calendar calendar = getCalendar(timestamp);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    /**
     * 获取所给时间戳日期，形如20190531
     *
     * @param timestamp
     * @return
     */
    public String getDate(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMdd");
        return sdf.format(date);
    }

    /**
     * 获取所给时间戳当天的开始时间戳
     * @param timestamp
     * @return
     */
    public long getDayStartTimestamp(long timestamp){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);

        calendar.set(Calendar.HOUR,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        return calendar.getTimeInMillis();
    }

    /**
     * 获取所给日期当天的开始时间戳
     * @param date
     * @return
     */
    public long getDayStartTimestamp(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        calendar.set(Calendar.HOUR,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        return calendar.getTimeInMillis();
    }

    /**
     * 获取所给时间戳当天的结束时间戳
     * @param timestamp
     * @return
     */
    public long getDayEndTimestamp(long timestamp){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);

        calendar.set(Calendar.HOUR,23);
        calendar.set(Calendar.MINUTE,59);
        calendar.set(Calendar.SECOND,59);
        calendar.set(Calendar.MILLISECOND,999);
        return calendar.getTimeInMillis();
    }

    /**
     * 获取所给日期当天的结束时间戳
     * @param date
     * @return
     */
    public long getDayEndTimestamp(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        calendar.set(Calendar.HOUR,23);
        calendar.set(Calendar.MINUTE,59);
        calendar.set(Calendar.SECOND,59);
        calendar.set(Calendar.MILLISECOND,999);
        return calendar.getTimeInMillis();
    }

    /**
     * 判断当前日期是否是交易日
     *
     * @param timestamp
     * @return
     */
    public boolean isTradingDay(long timestamp) {
        int weekIndex = getWeekDayIndex(timestamp);
        //周日-->周六:1-->7
        if (weekIndex > 1 && weekIndex < 7) {
            //周一至周五
            try {
                Connection connection = DBPoolConnection.getInstance().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(SQLBuilder.buildHolidayQuery());
                preparedStatement.setString(1, getDate(timestamp));
                ResultSet resultSet = preparedStatement.executeQuery();
                return !resultSet.next();
            } catch (SQLException e) {
                System.out.println("数据库查询出错");
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    /**
     * 当前时间点应该显示的调仓数据交易日日期
     *
     * @param timestamp 时间戳
     * @return 日期形如20190531
     */
    public String transactionDataDate(long timestamp) {
        return transactionDataDate(timestamp, true);
    }

    /**
     * 当前时间点应该显示的调仓数据交易日日期
     *
     * @param timestamp 时间戳
     * @param isToday   是否为当天
     * @return 日期形如20190531
     */
    private String transactionDataDate(long timestamp, boolean isToday) {
        if (isTradingDay(timestamp)) {
            Calendar calendar = getCalendar(timestamp);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            if (isToday && hour < 13) {
                return transactionDataDate(timestamp - TIMESTAMP_MILLS_ONE_DAY, false);
            }
            return getDate(timestamp);
        } else {
            return transactionDataDate(timestamp - TIMESTAMP_MILLS_ONE_DAY, false);
        }
    }

}
