package club.moredata.api.servlet;

import club.moredata.api.model.LeekResponse;
import club.moredata.api.spider.StockSpider;
import club.moredata.api.task.CubeTask;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.PropertyFilter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 定时抓取任务
 *
 * @author yeluodev1226
 */
@WebServlet(name = "SpiderServlet", urlPatterns = "/spider/*")
public class SpiderServlet extends BaseServlet {
    private static final long serialVersionUID = -1575662711817085735L;

    @Override
    public void dealRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        String matchPath = request.getHttpServletMapping().getMatchValue();
        LeekResponse leekResponse;
        //TODO 暂时不去处理任务执行的结果，直接返回任务是否执行即可
        switch (matchPath) {
            //更新风云榜组合，rank_cubes表
            case "top/list":
                new CubeTask().fetchCubeRankList();
                leekResponse = LeekResponse.successResponse(null, "风云榜组合数据正在更新......");
                break;

            //更新风云榜组合调仓情况，rank_cubes表字段view_rebalancing_id
            case "top/detail":
                new CubeTask().fetchCubeDetail();
                leekResponse = LeekResponse.successResponse(null);
                break;

            //更新股票数据
            case "stock":
                new StockSpider().updateStockInfos();
                leekResponse = LeekResponse.successResponse(null, "股票信息数据正在更新......");
                break;

            //更新待更新组合
            case "cube/update":
                new CubeTask().fetchPendingCubeDetail();
                leekResponse = LeekResponse.successResponse(null,"正在更新组合数据......");
                break;

            //重置待更新任务队列
            case "cube/reset":
                new CubeTask().resetPendingCubeList();
                leekResponse = LeekResponse.successResponse(null,"待更新组合队列正在重置......");
                break;

            //添加待更新任务
            case "cube/pending":
                new CubeTask().updatePendingCubeList();
                leekResponse = LeekResponse.successResponse(null,"待更新组合队列正在更新......");
                break;

            default:
                leekResponse = LeekResponse.errorURLResponse();
                break;
        }

        out.print(JSON.toJSONString(leekResponse,(PropertyFilter) (object, name, value) -> !"data".equals(name)));
    }
}
