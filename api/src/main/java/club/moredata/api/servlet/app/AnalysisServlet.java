package club.moredata.api.servlet.app;

import club.moredata.api.model.*;
import club.moredata.api.servlet.BaseServlet;
import club.moredata.api.task.AnalysisTask;
import club.moredata.common.db.sql.OrderType;
import club.moredata.common.db.sql.RebalancingType;
import club.moredata.common.util.Util;
import com.alibaba.fastjson.JSON;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 组合数据分析接口
 *
 * @author yeluodev1226
 */
@WebServlet(name = "AppAnalysisServlet", urlPatterns = "/app/analysis")
public class AnalysisServlet extends BaseServlet {

    private static final long serialVersionUID = 6369933881428115052L;

    @Override
    public void dealRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        LeekResponse leekResponse;
        String cubeIds = request.getParameter("cubeIds");

        if (cubeIds == null) {
            leekResponse = LeekResponse.errorParameterResponse();
            out.print(JSON.toJSONString(leekResponse));
            return;
        } else if (!cubeIdsPattern.matcher(cubeIds.replaceAll(" ", "")).matches()) {
            leekResponse = LeekResponse.errorParameterResponse();
            out.print(JSON.toJSONString(leekResponse));
            return;
        }

        AnalysisTask analysisTask = new AnalysisTask();

        int count = cubeIds.split(",").length;
        LeekResult<AlsStock> tempStockData = analysisTask.stockRankList(Util.dealCubeIds(cubeIds), count,
                30 * count, false, OrderType.WEIGHT_DESC);
        LeekResult<AlsSegment> tempSegmentData = analysisTask.segmentRankList(Util.dealCubeIds(cubeIds),
                count, OrderType.WEIGHT_DESC);
        LeekResult<AlsRebalancing> tempRebalancingData =
                analysisTask.rebalancingRankList(Util.dealCubeIds(cubeIds), count, OrderType.CHANGE_WEIGHT_DESC,
                        RebalancingType.ALL);

        LeekAppAnalysisResult result = new LeekAppAnalysisResult();
        result.setStock(tempStockData);
        result.setSegment(tempSegmentData);
        result.setRebalancing(tempRebalancingData);
        leekResponse = LeekResponse.generateResponse(result);

        if (null == leekResponse) {
            leekResponse = LeekResponse.errorDatabaseResponse();
        }

        out.print(JSON.toJSONString(leekResponse));
    }

}
