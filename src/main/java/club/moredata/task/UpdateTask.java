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

        CubeTask task = new CubeTask();
        task.updatePendingList();
    }

}
