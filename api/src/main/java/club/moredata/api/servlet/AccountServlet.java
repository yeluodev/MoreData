package club.moredata.api.servlet;

import club.moredata.api.model.LeekResponse;
import club.moredata.api.model.LeekResult;
import club.moredata.api.task.AccountTask;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.PropertyFilter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Pattern;

/**
 * 雪球用户相关数据接口
 * @author yeluodev1226
 */
@WebServlet(name = "AccountServlet", urlPatterns = "/account/*")
public class AccountServlet extends BaseServlet {
    private static final long serialVersionUID = 3846114669112219956L;
    private Pattern propertyPattern = Pattern.compile("code|message|data|list|count|updatedAt|rank|id|followersCount" +
            "|screenName|stocksCount" +
            "|photoDomain|profileImageUrl|statusCount|description|province|gender|realFans");

    @Override
    public void dealRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();

        String matchPath = request.getHttpServletMapping().getMatchValue();
        AccountTask task = new AccountTask();
        LeekResult leekResult = null;
        LeekResponse leekResponse;
        PropertyFilter propertyFilter = (object, name, value) -> !"cash".equals(name);
        switch (matchPath) {
            //性别分布
            case "gender":
                leekResult = task.accountGenderListDealed();
                break;

            //地区分布
            case "area":
                leekResult = task.accountAreaListDealed();
                break;

            //活跃度分布
            case "activation":
                leekResult = task.accountActivationListDealed();
                break;

            //粉丝值区间分布
            case "fans":
                leekResult = task.accountFansListDealed();
                break;

            //用户排行-粉丝数由大到小
            case "rank/followers":
                leekResult = task.accountRankList(1);
                propertyFilter = (object, name, value) -> propertyPattern.matcher(name).matches();
                break;

            //用户排行-排除匿名粉丝后，由大到小
            case "rank/realFans":
                leekResult = task.accountRankList(2);
                propertyFilter = (object, name, value) -> propertyPattern.matcher(name).matches();
                break;

            //用户排行-发言数
            case "rank/status":
                leekResult = task.accountRankList(3);
                propertyFilter = (object, name, value) -> propertyPattern.matcher(name).matches();
                break;

            default:
                break;
        }
        if (leekResult != null) {
            leekResponse = LeekResponse.generateResponse(leekResult);
        } else {
            leekResponse = LeekResponse.errorURLResponse();
        }

        out.print(JSON.toJSONString(leekResponse, propertyFilter));
    }
}
