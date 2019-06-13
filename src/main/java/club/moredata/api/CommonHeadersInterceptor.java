package club.moredata.api;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class CommonHeadersInterceptor implements Interceptor {

    private static String[] cookieArr = new String[]{
            "xq_a_token=0aebf2e32b55e41f30f0042f3e556b5e26206173;u=411560226561917",//chrome游客
            "xq_a_token=6a8cac4e9ea074035410b8ecea901a9053e2eaa6;u=9084578148",//QQ登录
            "xq_a_token=df8524b38974fce13d20b7ee39af90871c03be91;u=5414038194",//iphone游客
            "xq_a_token=7681e1ad0dceb363850fd64403e0ae1c79bbd64c;u=4880351754",//手机号登录
            "xq_a_token=8e00da4adb7e8067c2bfd19344954ceb1ec306ee;u=2341915540"};//微信登录

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request.Builder builder = request.newBuilder();

        Headers.Builder headersBuilder = request.headers().newBuilder();
        if (request.url().host().contains("api.xueqiu.com")) {
            headersBuilder.add("Host", "api.xueqiu.com");
        }
        headersBuilder.add("Accept", "application/json");
        int index = Math.min((int) Math.round(Math.random() * 5), 4);
        String cookie = cookieArr[index];
        System.out.println(cookie);
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
        return chain.proceed(request);
    }
}
