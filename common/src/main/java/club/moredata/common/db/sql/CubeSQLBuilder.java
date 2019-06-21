package club.moredata.common.db.sql;

public class CubeSQLBuilder {

    /**
     * 组合更新时间距当前时间超出6小时以上，添加入待更新队列
     * 因为是redis有序列表，这里查询不需要排序，排除已关停组合(=0代表未关停)
     *
     * @return sql
     */
    public static String buildCubeListNeedUpdateQuery() {
        return "SELECT `id`,`symbol`,TIMESTAMPDIFF(SECOND ,`latest_updated_at`,NOW()) AS `timediff` FROM " +
                "`cube` WHERE TIMESTAMPDIFF(HOUR,`latest_updated_at`,NOW()) > 6 AND `closed_at` = 0;";
    }

    /**
     * 调仓历史未更新完毕的组合，因为是redis有序列表，这里查询不需要排序
     *
     * @return sql
     */
    public static String buildCubeRebalancingUnfinishedQuery() {
        return "SELECT `id`,`symbol`,TIMESTAMPDIFF(SECOND ,`latest_updated_at`,NOW()) AS `timediff` FROM `cube` " +
                "WHERE `id` IN (SELECT `cube_id` FROM `view_rebalancing` WHERE `cube_id` NOT IN (SELECT `cube_id` FROM `view_rebalancing` WHERE `prev_bebalancing_id` = 0) GROUP BY `cube_id`)";

    }

    /**
     * 接口返回不存在的组合，将其removed状态置为true
     *
     * @return sql
     */
    public static String buildCubeRemovedUpdate() {
        return "UPDATE `cube` SET `removed` = 1 WHERE `symbol` = ?;";
    }
}
