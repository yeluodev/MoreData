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

    private static String[] cookieArr = new String[]{
            //chrome游客
//            "xq_a_token=fb5669a9402562082fea0085812a685be3947b19;u=251560908693357",
            //QQ登录
            "xq_a_token=e9a16fd39f8a7a53c4bad40a23c6d7bc0d4f4c53;u=9084578148",
            //iPhone游客登录
            "xq_a_token=df8524b38974fce13d20b7ee39af90871c03be91;u=5414038194",
            //手机号登录 失效
            "xq_a_token=0a7ac01c8257d47ff5404264fad05adf14feb1d0;u=4880351754",
            //微信登录
            "xq_a_token=8e00da4adb7e8067c2bfd19344954ceb1ec306ee;u=2341915540"};

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

        int index = Math.min((int) Math.round(Math.random() * 4), 3);
        String cookie = cookieArr[index];

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
