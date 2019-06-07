package club.moredata.task;

import club.moredata.util.RedisUtil;
import redis.clients.jedis.Jedis;

/**
 * @author yeluodev1226
 */
public class RedisTask {

    public static String getValue(String key){
        Jedis jedis = RedisUtil.getJedis();
        return jedis.get(key);
    }

    public static void setValue(String key,String value){
        Jedis jedis = RedisUtil.getJedis();
        jedis.set(key,value);
    }
}
