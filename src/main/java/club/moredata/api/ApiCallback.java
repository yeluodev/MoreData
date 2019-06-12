package club.moredata.api;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import java.io.IOException;
import java.util.Objects;

/**
 * 接口回调
 *
 * @author yeluodev1226
 */
public abstract class ApiCallback implements Callback {
    @Override
    public void onFailure(Call call, IOException e) {
        e.printStackTrace();
        onError(e.getMessage());
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        String str = Objects.requireNonNull(response.body()).string();
        System.out.println(str);
        if (response.code() >= 200 && response.code() < 300) {
            onSuccess(str);
        } else {
            System.out.println("接口请求发生错误" + response.code());
            onError(str);
        }
    }

    /**
     * 服务器返回数据处理
     *
     * @param response 返回数据
     */
    public abstract void onSuccess(String response);

    public void onError(String response) {
    }
}
