package club.moredata.common.net;

import club.moredata.common.util.RedisUtil;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import redis.clients.jedis.Jedis;

import java.io.IOException;

/**
 * 请求头拦截器，添加公共头部
 *
 * @author yeluodev1226
 */
public class CommonHeadersInterceptor implements Interceptor {

    private static String HOST_XUEQIU_API = "api.xueqiu.com";
    private static String HOST_XUEQIU_STOCK = "stock.xueqiu.com";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request.Builder builder = request.newBuilder();

        Headers.Builder headersBuilder = request.headers().newBuilder();
        String requestHost = request.url().host();
        if (requestHost.contains(HOST_XUEQIU_API)) {
            headersBuilder.add("Host", HOST_XUEQIU_API);
        } else if (requestHost.contains(HOST_XUEQIU_STOCK)) {
            headersBuilder.add("Host", HOST_XUEQIU_STOCK);
        }
        headersBuilder.add("Accept", "application/json");

        Jedis jedis = RedisUtil.getJedis();
        jedis.select(1);
        int length = Math.toIntExact(jedis.llen("Valid-Cookie"));
        int index = Math.min((int) Math.round(Math.random() * length), (length - 1));
        String cookie = jedis.lindex("Valid-Cookie", index);
        jedis.select(0);
        jedis.close();

        headersBuilder.add("Cookie", cookie);
        headersBuilder.add("User-Agent", "Xueqiu iPhone 11.19.1");
        headersBuilder.add("Accept-Language", "zh-Hans-CN;q=1");
        headersBuilder.add("Connection", "keep-alive");
        String url = request.url().toString();
        if (url.contains("/cubes/rebalancing/create.json")
                || url.contains("/statuses/update.json")) {
            headersBuilder.add("Content-Type", "application/x-www-form-urlencoded");
        } else if (request.url().toString().contains("/photo/upload.json")) {
            headersBuilder.add("Content-Type", "multipart/form-data");
        }

        builder.headers(headersBuilder.build());
        request = builder.build();

        //TODO 考虑拦截response,若是请求频繁导致被封，将当前cookie置入不活跃状态，避免随机cookie再次请求失败
        return chain.proceed(request);
    }
}
