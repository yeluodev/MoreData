package club.moredata.api.servlet;

import club.moredata.api.model.LeekResponse;
import club.moredata.api.task.StockTask;
import club.moredata.common.model.History;
import club.moredata.common.model.Stock;
import com.alibaba.fastjson.JSON;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * 股票相关接口
 *
 * @author yeluodev1226
 */
@WebServlet(name = "StockServlet", urlPatterns = "/stock/*")
public class StockServlet extends BaseServlet {
    private static final long serialVersionUID = -1575662711817085735L;

    @Override
    public void dealRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        LeekResponse leekResponse;

        String matchPath = request.getHttpServletMapping().getMatchValue();
        if (matchPath == null) {
            leekResponse = LeekResponse.errorURLResponse();
            out.print(JSON.toJSONString(leekResponse));
            return;
        }

        String page = request.getParameter("page");
        if (page == null) {
            page = "1";
        }

        if (!pagePattern.matcher(page).matches()) {
            leekResponse = LeekResponse.errorParameterResponse();
            out.print(JSON.toJSONString(leekResponse));
            return;
        }

        int pageInt = Integer.valueOf(page);

        StockTask task = new StockTask();
        switch (matchPath) {
            case "history":
                List<History<Stock>> list = task.stockHistoryList();
                out.print(JSON.toJSONString(list));
                break;
            case "change/hour":
                History<Stock> hourHistory = task.oneHourChange();
                if (hourHistory == null) {
                    out.print(JSON.toJSONString(LeekResponse.errorResponse(LeekResponse.ERROR_OTHER,
                            "暂无历史数据，无法展示变化情况")));
                } else {
                    printChange(out, hourHistory, pageInt);
                }
                break;
            case "change/day":
                History<Stock> dayHistory = task.oneDayChange();
                if (dayHistory == null) {
                    out.print(JSON.toJSONString(LeekResponse.errorResponse(LeekResponse.ERROR_OTHER,
                            "历史数据不足一天，无法展示变化情况")));
                } else {
                    printChange(out, dayHistory, pageInt);
                }
                break;
            case "change/week":
                History<Stock> weekHistory = task.oneWeekChange();
                if (weekHistory == null) {
                    out.print(JSON.toJSONString(LeekResponse.errorResponse(LeekResponse.ERROR_OTHER,
                            "历史数据不足一周，无法展示变化情况")));
                } else {
                    printChange(out, weekHistory, pageInt);
                }
                break;
            default:
                leekResponse = LeekResponse.errorURLResponse();
                out.print(JSON.toJSONString(leekResponse));
                break;
        }

    }

    private void printChange(PrintWriter pw, History<Stock> history, int page) {
        History<Stock> weekChange = new History<>();
        weekChange.setTitle(history.getTitle());
        weekChange.setUpdatedAt(history.getUpdatedAt());
        weekChange.setList(history.getList().subList(20 * (page - 1), 20 * page));
        pw.print(JSON.toJSONString(weekChange));
    }
}
