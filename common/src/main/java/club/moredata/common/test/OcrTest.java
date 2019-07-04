package club.moredata.common.test;

import club.moredata.common.net.CommonHeadersInterceptor;
import okhttp3.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class OcrTest {
    // webapi 接口地址
    private static final String URL = "http://tupapi.xfyun.cn/v1/currency";
    // 应用ID
    private static final String APPID = "5bbac6ff";
    // 接口密钥
    private static final String API_KEY = "433ceda55ec0107b5e6a2a3c7f1aa50e";
    // 图片名称
    private static final String IMAGE_NAME = "IMG_0418.JPG";
    // 图片url
    private static final String IMAGE_URL = "https://cp1.douguo.com/upload/caiku/4/6/f/yuan_46a51548d59bfd9f3d8896538f215ecf.jpg";

    // 图片地址
    private static final String PATH = "C:\\Users\\yeluodev1226\\Desktop\\keyboard.JPG";

    /**
     * WebAPI 调用示例程序
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(new CommonHeadersInterceptor())
                .build();

        String curTime = System.currentTimeMillis() / 1000L + "";
//        String param = "{\"image_url\":\"" + IMAGE_URL + "\",\"image_name\":\"" + IMAGE_NAME + "\"}";
        String param = "{\"image_name\":\"" + IMAGE_NAME + "\"}";
        String paramBase64 = new String(Base64.encodeBase64(param.getBytes("UTF-8")));
        String checkSum = DigestUtils.md5Hex(API_KEY + curTime + paramBase64);
        Headers.Builder headerBuilder = new Headers.Builder();
        headerBuilder.add("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
//        headerBuilder.add("Content-Type", "binary/octet-stream");
        headerBuilder.add("X-Param", paramBase64);
        headerBuilder.add("X-CurTime", curTime);
        headerBuilder.add("X-CheckSum", checkSum);
        headerBuilder.add("X-Appid", APPID);

        MediaType MEDIA_TYPE_PNG = MediaType.parse("binary/octet-stream");
        File file = new File(PATH);
        RequestBody filebody = MultipartBody.create(MEDIA_TYPE_PNG, file);

        Request request = new Request.Builder()
                .url(URL)
                .headers(headerBuilder.build())
                .post(filebody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println("接口调用结果：" + response.body().string());
            }
        });

    }
}
