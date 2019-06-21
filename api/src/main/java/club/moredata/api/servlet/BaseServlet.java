package club.moredata.api.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * servlet基类
 * @author yeluodev1226
 */
public abstract class BaseServlet extends HttpServlet {

    private static final long serialVersionUID = 7886846708443883462L;
    Pattern typePattern = Pattern.compile("[1-5]");
    Pattern levelPattern = Pattern.compile("[1-3]");
    Pattern cubePattern = Pattern.compile("^([1-9]|[1-9]\\d|100)$");
    Pattern stockPattern = Pattern.compile("^([1-9]|[1-9]\\d|[1-9]\\d{2}|[12]\\d{3}|3000)$");
    Pattern suspensionPattern = Pattern.compile("[01]");
    Pattern orderPattern = Pattern.compile("[1-6]");
    Pattern rebalancingPattern = Pattern.compile("[1-3]");
//    Pattern cubeIdsPattern = Pattern.compile("^(((ZH)?\\d*[,])*(ZH)?\\d*)$");
    //输入框不再限制组合id和symbol，任意字符串搜索
    Pattern cubeIdsPattern = Pattern.compile("\\S*");
    Pattern symbolPattern = Pattern.compile("^(ZH\\d{5,7})$");
    Pattern cashPattern = Pattern.compile("[01]");
    Pattern pagePattern = Pattern.compile("[1-9]\\d*");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("utf-8");
        response.setContentType("text/json;charset=utf-8");
        dealRequest(request, response);
    }

    public abstract void dealRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
}
