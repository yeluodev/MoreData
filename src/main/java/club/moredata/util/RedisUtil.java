package club.moredata.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * Redis 工具类
 *
 * @author yeluodev1226
 */
public class RedisUtil {
    public static JedisPool pool;

    static {
        JedisPoolConfig config = new JedisPoolConfig();
        //Redis最大连接数300，数据操作完成后应及时放回Redis池内
        config.setMaxTotal(300);
        config.setMaxIdle(300);
        config.setMaxWaitMillis(100000L);
        config.setTestOnBorrow(true);
        config.setTestOnReturn(true);

        pool = new JedisPool(config, "118.25.182.189", 6379, 100000, "My17602142031");
    }

    public static Jedis getJedis() {
        Jedis jedis = null;
        //获取jedis pool 对象
        try {
            jedis = pool.getResource();
        } catch (JedisConnectionException e) {
            e.printStackTrace();
        }
        return jedis;
    }
}