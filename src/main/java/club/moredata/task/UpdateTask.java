package club.moredata.task;

import club.moredata.util.RedisUtil;
import redis.clients.jedis.Jedis;

/**
 * @author yeluodev1226
 */
public class UpdateTask {

    private static UpdateTask instance = null;

    public static void main(String[] args) {
        getInstance().resetUpdateTask();
//        getInstance().runUpdateTask();
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
        task.updateCubeDetail();
    }

    /**
     * 重置任务队列
     */
    public void resetUpdateTask() {
        Jedis redis = RedisUtil.getJedis();
        redis.flushDB();
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
