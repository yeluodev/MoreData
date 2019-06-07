package club.moredata.api;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.Objects;

/**
 * 接口回调
 * @author yeluodev1226
 */
public abstract class ApiCallback implements Callback {
    @Override
    public void onFailure(Call call, IOException e) {
        e.printStackTrace();
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        if(response.code()>=200 && response.code() < 300){
            String str = Objects.requireNonNull(response.body()).string();
            onResponse(str);
        }else {
            System.out.println("接口请求发生错误"+response.code());
        }
    }

    /**
     * 服务器返回数据处理
     * @param response 返回数据
     */
    public abstract void onResponse(String response);
}
