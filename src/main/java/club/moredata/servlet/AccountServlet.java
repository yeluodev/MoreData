package club.moredata.servlet;

import club.moredata.model.LeekResponse;
import club.moredata.model.LeekResult;
import club.moredata.task.AccountTask;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.PropertyFilter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Pattern;

/**
 * @author yeluodev1226
 */
@WebServlet(name = "AccountServlet", urlPatterns = "/account/*")
public class AccountServlet extends HttpServlet {
    private static final long serialVersionUID = 3846114669112219956L;
    private Pattern propertyPattern = Pattern.compile("code|message|data|list|count|updatedAt|rank|id|followersCount" +
            "|screenName|stocksCount" +
            "|photoDomain|profileImageUrl|statusCount|description|province|gender|realFans");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("utf-8");
        response.setContentType("text/json;charset=utf-8");
        PrintWriter out = response.getWriter();

        String matchPath = request.getHttpServletMapping().getMatchValue();
        AccountTask task = new AccountTask();
        LeekResult leekResult = null;
        LeekResponse leekResponse;
        PropertyFilter propertyFilter = (object, name, value) -> !"cash".equals(name);
        switch (matchPath) {
            case "gender":
                leekResult = task.accountGenderListDealed();
                break;
            case "area":
                leekResult = task.accountAreaListDealed();
                break;
            case "activation":
                leekResult = task.accountActivationListDealed();
                break;
            case "fans":
                leekResult = task.accountFansListDealed();
                break;
            case "rank/followers":
                leekResult = task.accountRankList(1);
                propertyFilter = (object, name, value) -> propertyPattern.matcher(name).matches();
                break;
            case "rank/realFans":
                leekResult = task.accountRankList(2);
                propertyFilter = (object, name, value) -> propertyPattern.matcher(name).matches();
                break;
            case "rank/status":
                leekResult = task.accountRankList(3);
                propertyFilter = (object, name, value) -> propertyPattern.matcher(name).matches();
                break;
            default:
                break;
        }
        if(leekResult!=null){
            leekResponse = LeekResponse.generateResponse(leekResult);
        }else {
            leekResponse = LeekResponse.errorURLResponse();
        }

        out.print(JSON.toJSONString(leekResponse, propertyFilter));
    }
}
