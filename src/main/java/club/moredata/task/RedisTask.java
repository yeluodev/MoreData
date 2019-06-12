package club.moredata.task;

import club.moredata.entity.Cube;
import club.moredata.util.RedisUtil;
import redis.clients.jedis.Jedis;

import java.util.List;

/**
 * @author yeluodev1226
 */
public class RedisTask {

    public void insertCubeToPendingList(List<Cube> cubeList){
        Jedis jedis = RedisUtil.getJedis();
        cubeList.forEach(cube -> jedis.lpush("pending",cube.getSymbol()));
        jedis.close();
    }

    public static String getValue(String key){
        Jedis jedis = RedisUtil.getJedis();
        return jedis.get(key);
    }

    public static void setValue(String key,String value){
        Jedis jedis = RedisUtil.getJedis();
        jedis.set(key,value);
    }
}
