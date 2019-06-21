package club.moredata.common.db.sql;

/**
 * public_holidays表sql
 *
 * @author yeluodev1226
 */
public class DateSQLBuilder {

    /**
     * 查询日期是否是法定节假日（除周末外）
     *
     * @return sql
     */
    public static String buildHolidayQuery() {
        return "SELECT * FROM  `public_holidays` WHERE date = ?;";
    }
}
