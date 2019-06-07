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

/**
 * @author yeluodev1226
 */
@WebServlet(name = "AccountServlet", urlPatterns = "/account/*")
public class AccountServlet extends HttpServlet {
    private static final long serialVersionUID = 3846114669112219956L;

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
        switch (matchPath) {
            case "gender":
                leekResult = task.accountGenderListDealed();
                leekResponse = LeekResponse.generateResponse(leekResult);
                break;
            case "area":
                leekResult = task.accountGenderListDealed();
                leekResponse = LeekResponse.generateResponse(leekResult);
                break;
            case "activation":
                leekResult = task.accountActivationListDealed();
                leekResponse = LeekResponse.generateResponse(leekResult);
                break;
            case "fans":
                leekResult = task.accountFansListDealed();
                leekResponse = LeekResponse.generateResponse(leekResult);
                break;
            default:
                leekResponse = LeekResponse.errorURLResponse();
                break;
        }

        PropertyFilter propertyFilter = (object, name, value) -> !"cash".equals(name);
        out.print(JSON.toJSONString(leekResponse, propertyFilter));
    }
}
