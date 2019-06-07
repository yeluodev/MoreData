package club.moredata.api;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class CommonHeadersInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request.Builder builder = request.newBuilder();

        Headers.Builder headersBuilder = request.headers().newBuilder();
        if (request.url().host().contains("api.xueqiu.com")) {
            headersBuilder.add("Host", "api.xueqiu.com");
        }
        headersBuilder.add("Accept", "application/json");
        headersBuilder.add("Cookie", "xq_a_token=e302b606c69d4a45281d6a399db8f7d1cbac675c;u=9084578148");
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
