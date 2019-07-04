package club.moredata.common.net;

import club.moredata.common.util.RedisUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.Objects;

/**
 * 接口回调
 *
 * @author yeluodev1226
 */
public abstract class BaseApiCallback implements Callback {

    private static final int HTTP_CODE_200 = 200;
    private static final int HTTP_CODE_300 = 300;

    @Override
    public void onFailure(Call call, IOException e) {
        e.printStackTrace();
        onError(e.getMessage());
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        String str = Objects.requireNonNull(response.body()).string();
        System.out.println(str);
        if (response.code() >= HTTP_CODE_200 && response.code() < HTTP_CODE_300) {
            onSuccess(str);
        } else {
            System.out.println(String.format("接口请求发生%d错误", response.code()));
            removeCookieToErrorList(response.request().header("Cookie"));
            onError(str);
        }
    }

    /**
     * 将出错cookie从Valid-Cookie转移到Error-Cookie
     *
     * @param cookie
     */
    private void removeCookieToErrorList(String cookie) {
        Jedis jedis = RedisUtil.getJedis();
        jedis.select(1);
        jedis.lrem("Valid-Cookie", 1, cookie);
        jedis.lpush("Error-Cookie", cookie);
        jedis.select(0);
        jedis.close();
    }

    /**
     * 服务器返回数据处理
     *
     * @param response 返回数据
     */
    public abstract void onSuccess(String response);

    /**
     * 默认错误处理函数
     *
     * @param response 返回数据
     */
    public void onError(String response) {
    }
}
