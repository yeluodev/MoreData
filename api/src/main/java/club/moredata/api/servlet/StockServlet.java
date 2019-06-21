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
                History<Stock> hourChange = new History<>();
                hourChange.setTitle(hourHistory.getTitle());
                hourChange.setUpdatedAt(hourHistory.getUpdatedAt());
                hourChange.setList(hourHistory.getList().subList(20 * (pageInt - 1), 20 * pageInt));
                out.print(JSON.toJSONString(hourChange));
                break;
            case "change/day":
                History<Stock> dayChange = task.oneDayChange();
                out.print(JSON.toJSONString(dayChange));
                break;
            case "change/week":
                History<Stock> weekChange = task.oneWeekChange();
                out.print(JSON.toJSONString(weekChange));
                break;
            default:
                leekResponse = LeekResponse.errorURLResponse();
                out.print(JSON.toJSONString(leekResponse));
                break;
        }

//        out.print(JSON.toJSONString(leekResponse,(PropertyFilter) (object, name, value) -> !"data".equals(name)));
    }
}
