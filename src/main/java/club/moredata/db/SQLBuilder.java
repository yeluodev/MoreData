package club.moredata.db;

/**
 * sql语句
 *
 * @author yeluodev1226
 */
public class SQLBuilder {

    /**
     * 开关外键约束
     *
     * @param check
     * @return
     */
    public static String buildForeignKeyCheck(boolean check) {
        return "SET FOREIGN_KEY_CHECKS = " + (check ? 1 : 0);
    }

    /**
     * 插入user用户表
     *
     * @return
     */
    public static String buildAccountInsert() {
        return "INSERT IGNORE INTO `user`(`id`,`screen_name`,`description`,`photo_domain`,`profile_image_url`) VALUES" +
                "(?,?,?," +
                "?,?) ON DUPLICATE KEY UPDATE `screen_name` = ?,`description` = ?,`photo_domain` = ?,`profile_image_url` = ?";
    }

    /**
     * 插入cube组合表
     *
     * @return
     */
    public static String buildCubeInsert() {
        return "INSERT INTO `cube`(`id`,`name`,`symbol`,`owner_id`,`follower_count`) VALUES(?,?,?,?,?) ON DUPLICATE " +
                "KEY UPDATE `name` = ?,`symbol` = ?,`owner_id` = ?,`follower_count` = ?";
    }

    /**
     * 填充cube详情
     *
     * @return
     */
    public static String buildCubeDetailInsert() {
        return "REPLACE INTO `cube` (`id`, `name`, `symbol`, `description`, `owner_id`, `follower_count`, " +
                "`active_flag`, `created_at`, `updated_at`, `daily_gain`, `monthly_gain`, `total_gain`, `net_value`, `rank_percent`, `tag`, `view_rebalancing`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    }

    /**
     * 插入rank排行表
     *
     * @return
     */
    public static String buildRankInsert() {
        return "INSERT INTO `rank_cubes`(`id`,`cube_level`,`rank`,`gain_on_level`) VALUES(?,?,?,?)";
    }

    /**
     * 待填充组合详情的组合
     *
     * @return
     */
    public static String buildUpdateCubeQuery() {
        return "SELECT `rank_cubes`.`_id`,`cube`.`symbol`,`cube`.`id` FROM `rank_cubes`,`cube` WHERE `rank_cubes`" +
                ".`view_rebalancing_id` = 0 AND `rank_cubes`.`id`=`cube`.`id` ORDER BY `rank_cubes`.`update_time` ASC" +
                " LIMIT 10;";
    }

    /**
     * 更新rank_cubes组合持仓情况
     *
     * @return
     */
    public static String buildRankCubeUpdate() {
        return "UPDATE `rank_cubes` SET `view_rebalancing_id` = ? WHERE `_id` = ?;";
    }

    /**
     * 查询持仓表中是否已存在该条数据
     *
     * @return
     */
    public static String buildRebalancingQuery() {
        return "SELECT * FROM `view_rebalancing` WHERE `id` = ?;";
    }

    /**
     * 查询组合调仓表中是否已存在该条数据
     *
     * @return
     */
    public static String buildRebalancingHistoryQuery() {
        return "SELECT * FROM `rebalancing_history` WHERE rebalancing_id = ?;";
    }

    /**
     * 插入持仓记录表
     *
     * @return
     */
    public static String buildRebalancingInsert() {
        return "INSERT IGNORE INTO `view_rebalancing`(`id`,`status`,`cube_id`,`prev_bebalancing_id`,`created_at`," +
                "`updated_at`," +
                "`cash_value`,`cash`,`error_code`,`error_message`,`error_status`) VALUES(?,?,?,?,?,?,?,?,?,?,?);";
    }

    /**
     * 插入调仓记录表
     *
     * @return
     */
    public static String buildRebalancingHistoryInsert() {
        return "INSERT IGNORE INTO `rebalancing_history`(`cube_id`,`id`,`rebalancing_id`,`stock_id`,`stock_name`," +
                "`stock_symbol`,`weight`,`target_weight`,`change_weight`,`updated_at`) VALUES" +
                "(?,?,?,?," +
                "?,?,?,?,?,?);";
    }

    /**
     * 插入持仓详情表
     *
     * @return
     */
    public static String buildHoldingsInsert() {
        return "INSERT INTO `holdings`(`view_rebalancing_id`, `stock_id`, `weight`, `segment_name`, `segment_id`, " +
                "`stock_name`, `stock_symbol`, `segment_color`, `proactive`, `volume`) VALUES(?,?,?,?,?,?,?,?,?,?);";
    }

    /**
     * 数据分析处理的SQL
     */

    /**
     * 统计还没有更新详情的组合个数
     *
     * @return
     */
    public static String buildRankCubeUpdateCount() {
        return "SELECT COUNT(*) FROM (SELECT * FROM (SELECT * FROM `rank_cubes` WHERE `cube_level` = ? AND `_id` < ? ORDER BY " +
                "`update_time` DESC LIMIT 100) AS `t` ORDER BY `t`.`rank` ASC LIMIT ?) as `m` WHERE `m`" +
                ".`view_rebalancing_id` = 0;";
    }

    /**
     * 获取未完成全部统计流程的当次起始ID
     *
     * @return
     */
    public static String buildStartIdUpdateSeriesQuery() {
        return "SELECT `_id`,`update_time` FROM (SELECT * FROM `rank_cubes` WHERE `cube_level` = ? AND `_id` < ? ORDER BY `_id` DESC " +
                "LIMIT 100) AS t ORDER BY t.`_id` ASC LIMIT 1;";
    }

    /**
     * 获得组合个股比重排行
     *
     * @return
     */
    public static String buildRankStockIdsQuery() {
        return "SELECT `stock_id` FROM `holdings` WHERE `_id` in (SELECT `_id` FROM `holdings` where `view_rebalancing_id` " +
                "in (SELECT `view_rebalancing_id` FROM `rank_cubes` WHERE `_id` in (select `t`.`_id` FROM (select `t`" +
                ".`_id` FROM  (SELECT * FROM `rank_cubes` WHERE `cube_level` = ? AND `_id` < ? ORDER BY `_id` DESC " +
                "LIMIT 100) AS `t` " +
                "ORDER BY `rank` ASC LIMIT ?) AS `t`))) GROUP BY `stock_id` ORDER BY SUM(`weight`) DESC;";
    }

    /**
     * 个股比重排行
     *
     * @param suspensionIds
     * @param beforeId
     * @param orderType
     * @return
     */
    public static String buildStockRankQuery(String suspensionIds, int beforeId, OrderType orderType) {
        String sql = "SELECT substring_index(group_concat(stock_name order by _id DESC),',',1) as stock_name,stock_id,stock_symbol,segment_name," +
                "segment_color,ROUND(SUM(weight),2) AS weight,COUNT(*) AS count,ROUND(SUM(weight)*100/(SELECT SUM" +
                "(weight) FROM (SELECT stock_id,ROUND(SUM(weight),2) AS weight,COUNT(*) AS count FROM holdings " +
                "WHERE _id in (SELECT _id FROM `holdings` where view_rebalancing_id in (SELECT view_rebalancing_id " +
                "FROM `rank_cubes` WHERE _id in (select t._id FROM (SELECT t._id FROM(SELECT * FROM `rank_cubes` " +
                "WHERE cube_level = ? %s ORDER BY `_id` DESC LIMIT 100) AS t ORDER BY t.rank ASC LIMIT ?)as t )%s)) " +
                "GROUP BY stock_id %s LIMIT ?) AS temp),4) AS percent FROM holdings WHERE _id in (SELECT _id FROM " +
                "`holdings` where view_rebalancing_id in (SELECT view_rebalancing_id FROM `rank_cubes` WHERE _id in " +
                "(select t._id FROM (SELECT t._id FROM(SELECT * FROM `rank_cubes` WHERE cube_level = ? %s ORDER BY " +
                "`_id` DESC LIMIT 100) AS t ORDER BY t.rank ASC LIMIT ?)as t )%s)) GROUP BY stock_id %s LIMIT ?;";
        String stockIdCondition = getStockIdCondition(suspensionIds);
        String idCondition = getIdCondition(beforeId);
        String orderCondition = getOrderCondition(orderType);

        return String.format(sql, idCondition, stockIdCondition, orderCondition, idCondition, stockIdCondition, orderCondition);
    }

    /**
     * 获取个股合计比重和个数
     *
     * @param suspensionIds
     * @param beforeId
     * @param orderType
     * @return
     */
    public static String buildStockTotalQuery(String suspensionIds, int beforeId, OrderType orderType) {
        String sql = "SELECT ROUND(SUM(temp.weight),2) AS weight, count(*) as count FROM (SELECT ROUND(SUM(weight),2) AS weight," +
                "COUNT(*) AS count FROM holdings WHERE _id in (SELECT _id FROM `holdings` where view_rebalancing_id in (SELECT " +
                "view_rebalancing_id FROM `rank_cubes` WHERE _id in (select t._id FROM (SELECT t._id FROM(SELECT * " +
                "FROM `rank_cubes` WHERE cube_level = ? %s ORDER BY `_id` DESC LIMIT 100) AS t ORDER BY t.rank ASC " +
                "LIMIT ?)as t )%s)) GROUP BY stock_id %s LIMIT 3000) AS temp";
        String stockIdCondition = getStockIdCondition(suspensionIds);
        String idCondition = getIdCondition(beforeId);
        String orderCondition = getOrderCondition(orderType);

        return String.format(sql, idCondition, stockIdCondition, orderCondition);
    }

    /**
     * 板块比重排行
     *
     * @param beforeId
     * @param orderType
     * @return
     */
    public static String buildSegmentRankQuery(int beforeId, OrderType orderType) {
        String sql = "SELECT segment_name,segment_color,ROUND(SUM(weight),2) AS weight,COUNT(*) AS count,ROUND(SUM" +
                "(weight)*100/(SELECT SUM(weight) FROM (SELECT ROUND(SUM(weight),2) AS weight,COUNT(*) AS count FROM " +
                "holdings WHERE _id in (SELECT _id FROM `holdings` where view_rebalancing_id in (SELECT " +
                "view_rebalancing_id FROM `rank_cubes` WHERE _id in (select t._id FROM (SELECT t._id FROM(SELECT * " +
                "FROM `rank_cubes` WHERE cube_level = ? %s ORDER BY `_id` DESC LIMIT 100) AS t ORDER BY t.rank ASC " +
                "LIMIT ?)as t ))) GROUP BY segment_name %s ) AS temp),4) AS percent FROM holdings WHERE _id in " +
                "(SELECT _id FROM `holdings` where view_rebalancing_id in (SELECT view_rebalancing_id FROM " +
                "`rank_cubes` WHERE _id in (select t._id FROM (SELECT t._id FROM(SELECT * FROM `rank_cubes` WHERE " +
                "cube_level = ? %s ORDER BY `_id` DESC LIMIT 100) AS t ORDER BY t.rank ASC LIMIT ?)as t ))) GROUP BY segment_name %s;";
        String idCondition = getIdCondition(beforeId);
        String orderCondition = getOrderCondition(orderType);

        return String.format(sql, idCondition, orderCondition, idCondition, orderCondition);
    }

    /**
     * 获取板块合计比重和个数
     *
     * @param beforeId
     * @param orderType
     * @return
     */
    public static String buildSegmentTotalQuery(int beforeId, OrderType orderType) {
        String sql = "SELECT ROUND(SUM(temp.weight),2) AS weight, count(*) as count FROM (SELECT ROUND(SUM(weight),2) AS weight,COUNT(*) AS " +
                "count FROM holdings WHERE _id in (SELECT _id FROM `holdings` where view_rebalancing_id in (SELECT " +
                "view_rebalancing_id FROM `rank_cubes` WHERE _id in (select t._id FROM (SELECT t._id FROM(SELECT * " +
                "FROM `rank_cubes` WHERE cube_level = ? %s ORDER BY `_id` DESC LIMIT 100) AS t ORDER BY t.rank " +
                "ASC LIMIT ?)as t ))) GROUP BY segment_name %s ) AS temp;";
        String idCondition = getIdCondition(beforeId);
        String orderCondition = getOrderCondition(orderType);

        return String.format(sql, idCondition, orderCondition, idCondition, orderCondition);
    }

    /**
     * 获取调仓情况
     *
     * @param beforeId
     * @param orderType
     * @param rebalancingType
     * @return
     */
    public static String buildRebalancingRankQuery(int beforeId, OrderType orderType, RebalancingType rebalancingType) {
        String sql = "SELECT stock_name,stock_symbol,ROUND(SUM(change_weight),2) AS change_weight FROM rebalancing_history WHERE _id in (SELECT _id " +
                "FROM `rebalancing_history` where cube_id in (SELECT id FROM `rank_cubes` WHERE _id in (select t._id FROM (SELECT t" +
                "._id FROM(SELECT * FROM `rank_cubes` WHERE cube_level = ? %s ORDER BY `_id` DESC LIMIT 100) AS t " +
                "ORDER BY " +
                "t.rank ASC LIMIT ?)as t ))) AND (updated_at between ? AND ?) AND " +
                "(change_weight %s 0) GROUP BY stock_id %s LIMIT ?;";

        String idCondition = getIdCondition(beforeId);
        String orderCondition = getOrderCondition(orderType);
        String sign = getRebalancingSign(rebalancingType);

        return String.format(sql, idCondition, sign, orderCondition);
    }

    /**
     * 组合在榜天数排行
     *
     * @return
     */
    public static String buildCubeShowDaysRankQuery() {
        return "SELECT `cube`.`name`,`cube`.`symbol`,`cube`.`net_value`,`user`.`id`,`user`.`screen_name`,`user`.`photo_domain`,`user`" +
                ".`profile_image_url`,count(*) AS `showCount`,`rank_cubes`.`gain_on_level` FROM `rank_cubes`,`cube`," +
                "`user` WHERE `rank_cubes`.`_id` IN(SELECT `temp`.`_id` FROM (SELECT substring_index(group_concat" +
                "(`_id` order by `update_time` desc),',',1) as `_id` FROM `rank_cubes` group by DATE_FORMAT" +
                "(`update_time`,'%Y-%m-%d'),`rank`,`cube_level`) AS `temp`) AND `rank_cubes`.`cube_level` = ? AND " +
                "`cube`.`id` = `rank_cubes`.`id` AND `cube`.`owner_id` = `user`.`id` GROUP BY `rank_cubes`.`id` " +
                "ORDER BY `showCount` DESC,`cube`.`net_value` DESC LIMIT ?;";
    }

    /**
     * 更新时间
     *
     * @param beforeId
     * @return
     */
    public static String buildLatestUpdateTimeQuery(int beforeId) {
        String sql = "SELECT `update_time` FROM `rank_cubes` WHERE `cube_level` = ? %s ORDER BY `_id` DESC LIMIT 1;";
        return String.format(sql, getIdCondition(beforeId));
    }

    /**
     * 获取最新雪球风云榜排行
     *
     * @param beforeId
     * @return
     */
    public static String buildSnowballCubeQuery(int beforeId) {
        String sql = "SELECT `temp`.* FROM (SELECT `rank_cubes`.`rank`, `cube`.`name`,`cube`.`symbol`,`cube`" +
                ".`net_value`,`user`.`id`,`user`.`screen_name`,`user`.`photo_domain`,`user`.`profile_image_url`,`rank_cubes`" +
                ".`gain_on_level` FROM `rank_cubes`,`cube`,`user` WHERE `rank_cubes`.`id` = `cube`.`id` AND `cube`" +
                ".`owner_id` = `user`.`id` AND `rank_cubes`.`cube_level` = ? %s ORDER BY `rank_cubes`.`_id` DESC " +
                "LIMIT 100) AS `temp` ORDER BY `temp`.`rank` ASC;";
        return String.format(sql, beforeId > 0 ? ("AND `rank_cubes`.`_id` < " + beforeId) : "");
    }

    /**
     * 比重前三个股
     *
     * @return
     */
    public static String buildStockTop3Query() {
        return "SELECT substring_index(group_concat(stock_name order by _id DESC),',',1) as stock_name,stock_symbol," +
                "ROUND(SUM(weight),2) AS tWeight FROM holdings WHERE _id in (SELECT _id FROM `holdings` where " +
                "view_rebalancing_id in (SELECT view_rebalancing_id FROM `rank_cubes` WHERE _id in (SELECT t._id FROM(SELECT * FROM `rank_cubes` ORDER BY `_id` DESC LIMIT 300) AS t))) GROUP BY stock_id ORDER BY tWeight DESC LIMIT 3;";
    }

    private static String getStockIdCondition(String suspensionIds) {
        return (suspensionIds == null || suspensionIds.equals("")) ? "" : ("AND holdings.stock_id NOT IN(" + suspensionIds + ")");
    }

    private static String getIdCondition(int beforeId) {
        return beforeId > 0 ? ("AND _id < " + beforeId) : "";
    }

    private static String getOrderCondition(OrderType orderType) {
        String orderCondition;
        switch (orderType) {
            case COUNT_DESC:
                orderCondition = "ORDER BY count DESC,weight DESC";
                break;
            case COUNT_ASC:
                orderCondition = "ORDER BY count ASC,weight ASC";
                break;
            case WEIGHT_DESC:
                orderCondition = "ORDER BY weight DESC,count DESC";
                break;
            case WEIGHT_ASC:
                orderCondition = "ORDER BY weight ASC,count ASC";
                break;
            case CHANGE_WEIGHT_DESC:
                orderCondition = "ORDER BY change_weight DESC";
                break;
            case CHANGE_WEIGHT_ASC:
                orderCondition = "ORDER BY change_weight ASC";
                break;
            default:
                orderCondition = "";
                break;
        }
        return orderCondition;
    }

    private static String getRebalancingSign(RebalancingType rebalancingType) {
        String sign;
        switch (rebalancingType) {
            case IN:
                sign = ">";
                break;
            case OUT:
                sign = "<";
                break;
            case ALL:
            default:
                sign = "<>";
                break;
        }
        return sign;
    }

    /**
     * @return
     */
    public static String buildCubeRank() {
        return "SELECT temp.* FROM (SELECT `rank_cubes`.`rank`, `cube`.`name`,`user`.`screen_name`,`user`" +
                ".`photo_domain`,`user`.`profile_image_url`,`rank_cubes`.`gain_on_level` FROM rank_cubes,cube,user " +
                "WHERE rank_cubes.id = cube.id AND cube.owner_id = user.id AND rank_cubes.cube_level = ? ORDER BY " +
                "rank_cubes.`_id` DESC LIMIT 100) AS temp ORDER BY temp.rank ASC LIMIT ?,20";
    }

    /**
     * 查询是否是节假日
     *
     * @return
     */
    public static String buildHolidayQuery() {
        return "SELECT * FROM  `public_holidays` WHERE date = ?;";
    }

    /**
     * 统计账号总数
     *
     * @return
     */
    public static String buildAccountQuery() {
        return "SELECT count(*) FROM `user`;";
    }

    /**
     * 统计账号性别
     *
     * @return
     */
    public static String buildAccountGenderQuery() {
        return "SELECT `gender`,COUNT(*) AS `count`,ROUND(COUNT(*)*100/(SELECT COUNT(*) FROM `user`),4) AS `percent`" +
                " FROM " +
                "`user` " +
                "GROUP BY `gender`;";
    }

    /**
     * 账号粉丝值区间分布
     *
     * @return
     */
    public static String buildAccountFansQuery() {
        return "SELECT INTERVAL (`followers`,1,10,100,1000,10000,100000) AS `section` ,COUNT(*) FROM `user` " +
                "GROUP BY `section`;";
    }

    /**
     * 账号活跃度区间分布
     *
     * @return
     */
    public static String buildAccountStatusCountQuery() {
        return "SELECT INTERVAL (`status_count`,1,10,100,1000) AS `section` ,COUNT(*) FROM `user` " +
                "GROUP BY `section`;";
    }

    /**
     * 用户排行--粉丝数（含匿名用户即非注册用户）
     *
     * @return
     */
    public static String buildAccountRankQuery() {
        return "SELECT *,(`followers` - `anonymous_count`) AS `realFans` FROM `user` ORDER BY `followers` DESC LIMIT" +
                " 500;";
    }

    /**
     * 用户排行--非匿名粉丝数量
     *
     * @return
     */
    public static String buildAccountRealFansRankQuery() {
        return "SELECT *,(`followers` - `anonymous_count`) AS `realFans` FROM `user` ORDER BY `realFans` DESC LIMIT " +
                "500;";
    }

    /**
     * 用户排行--发言数
     *
     * @return
     */
    public static String buildAccountStatusRankQuery() {
        return "SELECT *,(`followers` - `anonymous_count`) AS `realFans` FROM `user` ORDER BY `status_count` DESC " +
                "LIMIT 500;";
    }

    /**
     * 指定组合的个股比重排行
     *
     * @return
     */
    public static String buildSpecifiedCubeStockRankQuery(String ids, String suspensionIds) {
        String sql = "SELECT substring_index(group_concat(stock_name order by _id DESC),',',1) as stock_name," +
                "stock_id,stock_symbol,segment_name,segment_color,ROUND(SUM(weight),2) AS weight,COUNT(*) AS count,ROUND(SUM(weight)*100/(SELECT ROUND(SUM(weight),2) AS weight FROM (SELECT `holdings`.* from `cube`,`holdings` WHERE " +
                "`cube`.`id` IN (%s) AND `cube`.`view_rebalancing` = `holdings`.`view_rebalancing_id` %s) AS temp )," +
                "6) " +
                "AS percent FROM (SELECT `holdings`.* from `cube`,`holdings` WHERE `cube`.`id` IN (%s) AND `cube`" +
                ".`view_rebalancing` = `holdings`.`view_rebalancing_id` %s) AS temp GROUP BY stock_id ORDER BY weight" +
                " DESC,count DESC;";
        String stockIdCondition = getStockIdCondition(suspensionIds);
        return String.format(sql, ids, stockIdCondition, ids, stockIdCondition);
    }

    /**
     * 获得组合个股比重排行
     *
     * @return
     */
    public static String buildSpecifiedCubeRankStockIdsQuery(String ids) {
        String sql = "SELECT stock_id,ROUND(SUM(weight),2) AS weight,COUNT(*) AS count FROM (SELECT `holdings`.* from" +
                " " +
                "`cube`,`holdings` WHERE `cube`.`id` IN (%s) AND `cube`.`view_rebalancing` = `holdings`" +
                ".`view_rebalancing_id`) AS temp GROUP BY stock_id ORDER BY weight DESC,count DESC;";
        return String.format(sql, ids);
    }

    public static String buildSpecifiedCubeStockTotalQuery() {
        return "";
    }

    /**
     * 指定组合的板块比重排行
     *
     * @return
     */
    public static String buildSpecifiedCubeSegmentRankQuery(String ids) {
        String sql = "SELECT segment_name,segment_color,ROUND(SUM(weight),2) AS weight,COUNT(*) AS count,ROUND(SUM" +
                "(weight)" +
                "*100/(SELECT ROUND(SUM(weight),2) AS weight FROM (SELECT `holdings`.* from `cube`,`holdings` WHERE " +
                "`cube`.`id` IN (%s) AND `cube`.`view_rebalancing` = `holdings`.`view_rebalancing_id`) AS temp ),6) " +
                "AS" +
                " percent FROM (SELECT `holdings`.* from `cube`,`holdings` WHERE `cube`.`id` IN (%s) AND `cube`" +
                ".`view_rebalancing` = `holdings`.`view_rebalancing_id`) AS temp GROUP BY segment_id ORDER BY weight DESC,count DESC;";
        return String.format(sql, ids, ids);
    }

    /**
     * 查询指定id的组合
     *
     * @param ids
     * @return
     */
    public static String buildSpecifiedCubeQuery(String ids) {
        String sql = "SELECT `cube`.`id`,`cube`.`name`,`cube`.`symbol`,`cube`.`description`,`cube`.`owner_id`,`cube`.`follower_count`,`cube`.`net_value`,`cube`.`created_at`,`cube`.`updated_at`,`user`.`screen_name` FROM `cube`,`user` WHERE `cube`.`id` IN (%s) AND `cube`.`owner_id` = `user`.`id`;";
        return String.format(sql, ids);
    }
}
