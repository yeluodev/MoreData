package club.moredata.api.task;

import club.moredata.common.entity.Cube;
import club.moredata.common.util.RedisUtil;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Redis数据库操作类
 * @author yeluodev1226
 */
public class RedisTask {

    public static final String KEY_CUBE_PENDING = "PendingCube";
    public static final String KEY_CUBE_FETCHING = "FetchingCube";
    public static final String KEY_CUBE_FAIL = "FailCube";
    public static final String KEY_CUBE_SUCCESS = "SuccessCube";

    /**
     * 插入待更新组合队列
     * @param cubeList 列表
     */
    public static void insertCubeToPending(List<Cube> cubeList){
        Jedis jedis = RedisUtil.getJedis();
        jedis.select(4);
        cubeList.forEach(cube -> jedis.zadd(KEY_CUBE_PENDING, cube.getFollowerCount(), cube.getSymbol()));
        jedis.select(0);
        jedis.close();
    }

    /**
     * 重置待更新组合队列
     */
    public static void resetPendingCube(){
        Jedis jedis = RedisUtil.getJedis();
        jedis.select(4);
        jedis.del(KEY_CUBE_PENDING);
        jedis.select(0);
        jedis.close();
    }

    /**
     * 查找更新时间距现在最长的10个组合
     * @return 组合代码列表
     */
    public static List<String> oldestCubeToUpdate(){
        Jedis redis = RedisUtil.getJedis();
        redis.select(4);
        Set<String> set = redis.zrevrangeByScore(KEY_CUBE_PENDING, "+inf", "-inf", 0, 10);
        List<String> symbolList = new ArrayList<>(set);
        redis.select(0);
        redis.close();
        return symbolList;
    }

    /**
     * 组合更新成功
     * @param symbol 组合代码
     */
    public static void moveCubeFromFetchingToSuccess(String symbol){
        moveRedisMember(KEY_CUBE_FETCHING,KEY_CUBE_SUCCESS,symbol);
    }

    /**
     * 组合更新失败
     * @param symbol 组合代码
     */
    public static void moveCubeFromFetchingToFail(String symbol){
        moveRedisMember(KEY_CUBE_FETCHING,KEY_CUBE_FAIL,symbol);
    }

    /**
     * 转移集合成员
     *
     * @param source
     * @param des
     * @param member
     */
    private static void moveRedisMember(String source, String des, String member) {
        Jedis redis = RedisUtil.getJedis();
        redis.select(4);
        redis.smove(source, des, member);
        redis.select(0);
        redis.close();
    }


}
