package club.moredata.servlet;

import club.moredata.db.OrderType;
import club.moredata.db.RebalancingType;
import club.moredata.model.*;
import club.moredata.task.AnalysisTask;
import club.moredata.util.Util;
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
@WebServlet(name = "AnalysisServlet", urlPatterns = "/analysis")
public class AnalysisServlet extends HttpServlet {

    private static final long serialVersionUID = 6369933881428115052L;
    private Pattern typePattern = Pattern.compile("[1-5]");
    private Pattern levelPattern = Pattern.compile("[1-3]");
    private Pattern cubePattern = Pattern.compile("^([1-9]|[1-9]\\d|100)$");
    private Pattern stockPattern = Pattern.compile("^([1-9]|[1-9]\\d|[1-9]\\d{2}|[12]\\d{3}|3000)$");
    private Pattern suspensionPattern = Pattern.compile("[01]");
    private Pattern orderPattern = Pattern.compile("[1-6]");
    private Pattern rebalancingPattern = Pattern.compile("[1-3]");
    private Pattern cubeIdsPattern = Pattern.compile("^(((ZH)?[1-9]\\d*[,])*(ZH)?[1-9]\\d*)$");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("utf-8");
        response.setContentType("text/json;charset=utf-8");
        PrintWriter out = response.getWriter();

        String type = request.getParameter("type");
        String level = request.getParameter("level");
        String cubeLimit = request.getParameter("cubeLimit");
        String stockLimit = request.getParameter("stockLimit");
        String suspension = request.getParameter("suspension");
        String orderType = request.getParameter("order");
        String rebalancingType = request.getParameter("rebalancing");
        String cubeIds = request.getParameter("cubeIds");

        LeekResponse leekResponse = null;
        if (type == null) {
            type = "1";
        }
        if (level == null) {
            level = "1";
        }
        if (cubeLimit == null) {
            cubeLimit = "100";
        }
        if (stockLimit == null) {
            stockLimit = "3000";
        }
        if (suspension == null) {
            suspension = "1";
        }
        if (orderType == null) {
            orderType = type.equals("3") ? "5" : "1";
        }
        if (rebalancingType == null) {
            rebalancingType = "1";
        }

        if (cubeIds != null && !cubeIdsPattern.matcher(cubeIds.replaceAll(" ","")).matches()) {
            leekResponse = LeekResponse.errorParameterResponse();
            out.print(JSON.toJSONString(leekResponse));
            return;
        }

        if (!typePattern.matcher(type).matches()
                || !levelPattern.matcher(level).matches()
                || !cubePattern.matcher(cubeLimit).matches()
                || !stockPattern.matcher(stockLimit).matches()
                || !suspensionPattern.matcher(suspension).matches()
                || !orderPattern.matcher(orderType).matches()
                || !rebalancingPattern.matcher(rebalancingType).matches()) {
            leekResponse = LeekResponse.errorParameterResponse();
            out.print(JSON.toJSONString(leekResponse));
            return;
        }

        int typeInt = Integer.valueOf(type);
        int levelInt = Integer.valueOf(level);
        int cubeLimitInt = Integer.valueOf(cubeLimit);
        int stockLimitInt = Integer.valueOf(stockLimit);
        int suspensionInt = Integer.valueOf(suspension);
        int orderTypeInt = Integer.valueOf(orderType);
        if (typeInt == 3 && orderTypeInt < 5 || (typeInt < 3 && orderTypeInt > 4)) {
            leekResponse = LeekResponse.errorParameterResponse();
            out.print(JSON.toJSONString(leekResponse));
            return;
        }

        AnalysisTask analysisTask = new AnalysisTask();
        switch (type) {
            case "1":
                LeekResult<AlsStock> stockData;
                if (cubeIds == null) {
                    stockData = analysisTask.stockRankList(levelInt, cubeLimitInt, stockLimitInt,
                            suspensionInt == 0, OrderType.getType(orderType));
                } else {
                    int count = cubeIds.split(",").length;
                    stockData = analysisTask.stockRankList(Util.dealCubeIds(cubeIds), count, stockLimitInt,
                            suspensionInt == 0, OrderType.getType(orderType));
                }

                leekResponse = LeekResponse.generateResponse(stockData);
                break;
            case "2":
                LeekResult<AlsSegment> segmentData;
                if (cubeIds == null) {
                    segmentData = analysisTask.segmentRankList(levelInt, cubeLimitInt, OrderType.getType(orderType));
                } else {
                    int count = cubeIds.split(",").length;
                    segmentData = analysisTask.segmentRankList(Util.dealCubeIds(cubeIds), count, OrderType.getType(orderType));
                }
                leekResponse = LeekResponse.generateResponse(segmentData);
                break;
            case "3":
                LeekResult<AlsRebalancing> rebalancingData;
                if (cubeIds == null) {
                    rebalancingData = analysisTask.rebalancingRankList(levelInt, cubeLimitInt,
                            stockLimitInt, OrderType.getType(orderType), RebalancingType.getType(rebalancingType));
                } else {
                    int count = cubeIds.split(",").length;
                    rebalancingData = analysisTask.rebalancingRankList(Util.dealCubeIds(cubeIds), count, OrderType.getType(orderType),
                            RebalancingType.getType(rebalancingType));
                }
                leekResponse = LeekResponse.generateResponse(rebalancingData);
                break;
            case "4":
                LeekResult<AlsCube> cubeData = analysisTask.cubeRankList(levelInt, Integer.MAX_VALUE);
                leekResponse = LeekResponse.generateResponse(cubeData);
                break;
            case "5":
                LeekResult<AlsCube> snowballData = analysisTask.snowballCubeList(levelInt);
                leekResponse = LeekResponse.generateResponse(snowballData);
                break;
            default:

        }

        if (null == leekResponse) {
            leekResponse = LeekResponse.errorDatabaseResponse();
        }

        PropertyFilter propertyFilter = (object, name, value) -> {
            if (typeInt > 2 && "cash".equals(name)) {
                return false;
            }
            return typeInt != 5 || !"showDaysCount".equals(name);
        };
        out.print(JSON.toJSONString(leekResponse, propertyFilter));
    }

}
