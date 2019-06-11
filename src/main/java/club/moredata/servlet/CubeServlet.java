package club.moredata.servlet;

import club.moredata.api.ApiManager;
import club.moredata.db.OrderType;
import club.moredata.entity.Cube;
import club.moredata.entity.Rebalancing;
import club.moredata.entity.SearchResult;
import club.moredata.model.LeekResponse;
import club.moredata.model.LeekResult;
import club.moredata.model.RebStock;
import club.moredata.task.AutoTask;
import club.moredata.task.CubeTask;
import club.moredata.util.DateUtil;
import club.moredata.util.Util;
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
 * @author yeluodev1226
 */
@WebServlet(name = "CubeServlet", urlPatterns = "/cube/*")
public class CubeServlet extends BaseServlet {

    private static final long serialVersionUID = 6369933881428115052L;
    private Pattern levelPattern = Pattern.compile("[1-3]");
    private Pattern cubePattern = Pattern.compile("^([1-9]|[1-9]\\d|100)$");
    private Pattern stockPattern = Pattern.compile("^([1-9]|[12]\\d|30)$");
    private Pattern suspensionPattern = Pattern.compile("[01]");
    private Pattern cashPattern = Pattern.compile("[01]");
    private Pattern orderPattern = Pattern.compile("[1-4]");
    private Pattern cubeIdsPattern = Pattern.compile("^(((ZH)?[1-9]\\d*[,])*(ZH)?[1-9]\\d*)$");

    private Pattern paramKeyPattern = Pattern.compile("code|message|data|id|status|cube_id|error_status" +
            "|error_code|error_message|error_message|comment");

    @Override
    public void dealRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();

        LeekResponse leekResponse;
        if (!DateUtil.getInstance().isTradingDay(System.currentTimeMillis())) {
            leekResponse = LeekResponse.errorResponse(LeekResponse.ERROR_OTHER, "A股今日休市，组合不进行跟踪调仓");
            out.print(JSON.toJSONString(leekResponse));
            return;
        }

        String matchPath = request.getHttpServletMapping().getMatchValue();
        if(matchPath==null){
            leekResponse = LeekResponse.errorURLResponse();
            out.print(JSON.toJSONString(leekResponse));
            return;
        }

        String level = request.getParameter("level");
        String cubeLimit = request.getParameter("cubeLimit");
        String stockLimit = request.getParameter("stockLimit");
        String suspension = request.getParameter("suspension");
        String cash = request.getParameter("cash");
        String orderType = request.getParameter("order");
        String key = request.getParameter("key");
        String cubeIds = request.getParameter("cubeIds");

        if (level == null) {
            level = "1";
        }
        if (cubeLimit == null) {
            cubeLimit = "100";
        }
        if (stockLimit == null) {
            stockLimit = "15";
        }
        if (suspension == null) {
            suspension = "1";
        }
        if (cash == null) {
            cash = "0";
        }
        if (orderType == null) {
            orderType = "1";
        }
        if (key == null) {
            key = "";
        }

        if (cubeIds != null && !cubeIdsPattern.matcher(cubeIds.replaceAll(" ","")).matches()) {
            leekResponse = LeekResponse.errorParameterResponse();
            out.print(JSON.toJSONString(leekResponse));
            return;
        }

        if (!levelPattern.matcher(level).matches()
                || !cubePattern.matcher(cubeLimit).matches()
                || !stockPattern.matcher(stockLimit).matches()
                || !suspensionPattern.matcher(suspension).matches()
                || !cashPattern.matcher(cash).matches()
                || !orderPattern.matcher(orderType).matches()) {
            leekResponse = LeekResponse.errorParameterResponse();
            out.print(JSON.toJSONString(leekResponse));
            return;
        }

        int levelInt = Integer.valueOf(level);
        int cubeLimitInt = Integer.valueOf(cubeLimit);
        int stockLimitInt = Integer.valueOf(stockLimit);
        int suspensionInt = Integer.valueOf(suspension);
        int cashInt = Integer.valueOf(cash);

        PropertyFilter propertyFilter = (object, name, value) -> true;
        AutoTask autoTask = new AutoTask();
        switch (matchPath) {
            case "rebalancing":
                Rebalancing rebalancing = autoTask.rebalancingCube(levelInt, cubeLimitInt, stockLimitInt, suspensionInt == 0,
                        cashInt == 0, OrderType.getType(orderType));
                if (rebalancing == null) {
                    leekResponse = LeekResponse.errorDatabaseResponse();
                } else {
                    leekResponse = LeekResponse.successResponse(rebalancing);
                }
                propertyFilter = (object, name, value) -> paramKeyPattern.matcher(name).matches();
                break;
            case "track":
                LeekResult<RebStock> leekResult = autoTask.trackCube(levelInt, cubeLimitInt, stockLimitInt, suspensionInt == 0,
                        cashInt == 0, OrderType.getType(orderType));
                if(leekResult==null){
                    leekResponse = LeekResponse.errorDatabaseResponse();
                }else {
                    leekResponse = LeekResponse.successResponse(leekResult);
                }
                break;
            case "search":
                SearchResult<Cube> searchResult = ApiManager.getInstance().searchCube(key);
                if(searchResult==null){
                    leekResponse = LeekResponse.errorOtherResponse();
                }else {
                    leekResponse = LeekResponse.successResponse(searchResult);
                }
                break;
            case "list":
                if(cubeIds==null){
                    leekResponse = LeekResponse.errorParameterResponse();
                }else {
                    CubeTask task = new CubeTask();
                    LeekResult<Cube> cubeLeekResult = task.fetchCubeList(Util.dealCubeIds(cubeIds));
                    leekResponse = LeekResponse.successResponse(cubeLeekResult);
                    propertyFilter = (object, name, value) -> !"cash".equals(name);
                }
                break;
            default:
                leekResponse = LeekResponse.errorURLResponse();
                break;
        }

        out.print(JSON.toJSONString(leekResponse, propertyFilter));
    }


}
