package club.moredata.common.test;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class OcrTest2 {

    // webapi 接口地址
    private static final String URL = "http://tupapi.xfyun.cn/v1/currency";
    // 应用ID
    private static final String APPID = "5bbac6ff";
    // 接口密钥
    private static final String API_KEY = "433ceda55ec0107b5e6a2a3c7f1aa50e";
    // 图片名称
    private static final String IMAGE_NAME = "img.jpg";
    // 图片url
    private static final String IMAGE_URL = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1561645294712&di=b7a30cfcb04549df3a25af87a0dc451c&imgtype=0&src=http%3A%2F%2Fb-ssl.duitang.com%2Fuploads%2Fitem%2F201403%2F21%2F20140321122433_3zHcJ.jpeg";

    // 图片地址
    private static final String PATH = "C:\\Users\\yeluodev1226\\Desktop\\IMG_0418.JPG";

    /**
     * WebAPI 调用示例程序
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        Map<String, String> header = buildHttpHeader();
        byte[] imageByteArray = FileUtil.read(PATH);
        String result = HttpUtil.doPost1(URL, header, imageByteArray);
        System.out.println("接口调用结果：" + result);
    }

    /**
     * 组装http请求头
     */
    private static Map<String, String> buildHttpHeader() throws UnsupportedEncodingException {
        String curTime = System.currentTimeMillis() / 1000L + "";
        String param = "{\"image_name\":\"" + IMAGE_NAME + "\"}";
        String paramBase64 = new String(Base64.encodeBase64(param.getBytes("UTF-8")));
        String checkSum = DigestUtils.md5Hex(API_KEY + curTime + paramBase64);
        Map<String, String> header = new HashMap<String, String>();
        header.put("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
        header.put("X-Param", paramBase64);
        header.put("X-CurTime", curTime);
        header.put("X-CheckSum", checkSum);
        header.put("X-Appid", APPID);
        return header;
    }
}
