package club.moredata.db;

/**
 * 用户相关sql语句
 *
 * @author yeluodev1226
 */
public class UserSQL {

    /**
     * 插入用户表，信息完整
     *
     * @param resetFinishStatus
     * @return
     */
    public static String completeInsert(boolean resetFinishStatus) {
        String sql = "INSERT INTO `user`(`id`,`followers_count`,`screen_name`,`friends_count`,`type`,`verified`,`stocks_count`," +
                "`photo_domain`,`profile_image_url`,`cube_count`,`verified_realname`,`status_count`,`last_status_id`,`description`," +
                "`province`,`city`,`gender`,`page`,`finished`) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE " +
                "`followers_count` = VALUES(`followers_count`),`screen_name` = VALUES(`screen_name`)," +
                "`friends_count` = VALUES(`friends_count`),`type` = VALUES(`type`),`verified` = VALUES(`verified`)," +
                "`stocks_count` = VALUES(`stocks_count`),`photo_domain` = VALUES(`photo_domain`),`profile_image_url` = VALUES(`profile_image_url`)," +
                "`cube_count` = VALUES(`cube_count`),`verified_realname` = VALUES(`verified_realname`),`status_count` = VALUES(`status_count`)," +
                "`last_status_id` = VALUES(`last_status_id`),`description` = VALUES(`description`)," +
                "`province` = VALUES(`province`),`city` = VALUES(`city`)%s;";
        return String.format(sql, resetFinishStatus ? ",`page`=VALUES(`page`),`finished`=VALUES(`finished`)" : "");
    }

    /**
     * 检测指定id用户粉丝列表是否已抓取完毕
     *
     * @return
     */
    public static String accountQuery() {
        String sql = "SELECT * FROM `user` WHERE `id` = ?;";
        return sql;
    }

    /**
     * 更新用户粉丝相关数据
     *
     * @return
     */
    public static String accountUpdate() {
        String sql = "UPDATE `user` SET `followers_count` = ?, `page` = ?,`maxPage`=?,`anonymous_count`=?,`finished`=? WHERE `id` = ?;";
        return sql;
    }
}
