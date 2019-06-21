package club.moredata.common.db.sql;

/**
 * stock表相关sql语句
 *
 * @author yeluodev1226
 */
public class StockSQLBuilder {

    /**
     * 查询stock表，返回全部字段信息
     *
     * @return sql
     */
    public static String buildStockQuery() {
        return "SELECT * FROM `stock` ORDER BY `followers_count` DESC;";
    }

    /**
     * 查询stock表，仅返回股票代码
     *
     * @return sql
     */
    public static String buildSymbolQuery() {
        return "SELECT `symbol` FROM `stock`;";
    }

    /**
     * 更新stock表，关注人数和相关讨论数
     *
     * @return sql
     */
    public static String buildStockUpdate() {
        return "UPDATE `stock` SET `followers_count` = ? ,`status_count` = ? , `updated_at` = CURRENT_TIMESTAMP() WHERE `symbol` = ?;";
    }

}
