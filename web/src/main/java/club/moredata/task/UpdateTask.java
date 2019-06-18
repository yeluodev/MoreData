package club.moredata.task;

import club.moredata.util.RedisUtil;
import redis.clients.jedis.Jedis;

/**
 * @author yeluodev1226
 */
public class UpdateTask {

    private static UpdateTask instance = null;

    public static void main(String[] args) {
        UpdateTask task = new UpdateTask();
        task.resetUpdateTask();
    }

    private UpdateTask() {

    }

    public static UpdateTask getInstance() {
        if (null == instance) {
            synchronized (UpdateTask.class) {
                if (null == instance) {
                    instance = new UpdateTask();
                }
            }
        }
        return instance;
    }

    public void runUpdateTask() {
        CubeTask task = new CubeTask();
        task.updateOldestUpdateCubes();
    }

    /**
     * 重置任务队列
     */
    public void resetUpdateTask() {
        Jedis redis = RedisUtil.getJedis();
        //组合更新数据库
        redis.select(4);
        redis.flushDB();
        redis.select(0);
        redis.close();

        CubeTask task = new CubeTask();
        task.updatePendingList();
    }

    /**
     * 对需要更新组合的任务队列进行刷新
     */
    public void refreshPendingList() {
        CubeTask task = new CubeTask();
        task.updatePendingList();
    }

}
