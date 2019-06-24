package club.moredata.api.servlet;

import club.moredata.api.model.LeekResponse;
import club.moredata.api.task.StockTask;
import club.moredata.common.model.History;
import club.moredata.common.model.Stock;
import club.moredata.common.net.SyncApi;
import com.alibaba.fastjson.JSON;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 股票相关接口
 *
 * @author yeluodev1226
 */
@WebServlet(name = "StockServlet", urlPatterns = "/stock/*")
public class StockServlet extends BaseServlet {
    private static final long serialVersionUID = -1575662711817085735L;

    /**
     * 结果排序：
     * 1/2-关注数
     * 3/4-讨论数
     * 5/6-关注数变化
     * 7/8-讨论数变化
     */
    private Pattern orderPattern = Pattern.compile("[1-8]");
    private Pattern allPattern = Pattern.compile("true|false");

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
        String order = request.getParameter("order");
        String all = request.getParameter("all");
        String symbols = request.getParameter("symbols");
        if (page == null) {
            page = "1";
        }
        if (order == null) {
            order = "1";
        }
        if (all == null) {
            all = "false";
        }

        if (!pagePattern.matcher(page).matches()
                || !orderPattern.matcher(order).matches()
                || !allPattern.matcher(all).matches()) {
            leekResponse = LeekResponse.errorParameterResponse();
            out.print(JSON.toJSONString(leekResponse));
            return;
        }

        int pageInt = Integer.valueOf(page);
        int orderInt = Integer.valueOf(order);
        boolean allInOnePage = Boolean.valueOf(all);

        StockTask task = new StockTask();
        switch (matchPath) {
            case "now":
                History<Stock> stockInfos = resortStockList(task.stockInfoAtNow(), orderInt);
                out.print(JSON.toJSONString(LeekResponse.successResponse(stockInfos)));
                break;
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
                    printChange(out, hourHistory, pageInt, orderInt, allInOnePage);
                }
                break;
            case "change/day":
                History<Stock> dayHistory = task.oneDayChange();
                if (dayHistory == null) {
                    out.print(JSON.toJSONString(LeekResponse.errorResponse(LeekResponse.ERROR_OTHER,
                            "历史数据不足一天，无法展示变化情况")));
                } else {
                    printChange(out, dayHistory, pageInt, orderInt, allInOnePage);
                }
                break;
            case "change/week":
                History<Stock> weekHistory = task.oneWeekChange();
                if (weekHistory == null) {
                    out.print(JSON.toJSONString(LeekResponse.errorResponse(LeekResponse.ERROR_OTHER,
                            "历史数据不足一周，无法展示变化情况")));
                } else {
                    printChange(out, weekHistory, pageInt, orderInt, allInOnePage);
                }
                break;
            case "trade":
                List<Stock> stockList = SyncApi.getInstance().fetchStockTradeInfos(symbols);
                leekResponse = LeekResponse.successResponse(stockList);
                out.print(JSON.toJSONString(leekResponse));
                break;
            default:
                leekResponse = LeekResponse.errorURLResponse();
                out.print(JSON.toJSONString(leekResponse));
                break;
        }

    }

    /**
     * 根据参数重新给股票列表进行排序
     *
     * @param history 股票信息
     * @param order   排序
     * @return 排序后结果
     */
    private History<Stock> resortStockList(History<Stock> history, int order) {
        switch (order) {
            case 1:
                history.getList().sort(Comparator.comparingInt(Stock::getFollowers));
                Collections.reverse(history.getList());
                break;
            case 2:
                history.getList().sort(Comparator.comparingInt(Stock::getFollowers));
                break;
            case 3:
                history.getList().sort(Comparator.comparingInt(Stock::getStatuses));
                Collections.reverse(history.getList());
                break;
            case 4:
                history.getList().sort(Comparator.comparingInt(Stock::getStatuses));
                break;
            case 5:
                history.getList().sort(Comparator.comparingInt(Stock::getFollowersChange));
                Collections.reverse(history.getList());
                break;
            case 6:
                history.getList().sort(Comparator.comparingInt(Stock::getFollowersChange));
                break;
            case 7:
                history.getList().sort(Comparator.comparingInt(Stock::getStatusesChange));
                Collections.reverse(history.getList());
                break;
            case 8:
                history.getList().sort(Comparator.comparingInt(Stock::getStatusesChange));
                break;
            default:
                break;
        }

        return history;
    }

    /**
     * 股票数据变化
     *
     * @param pw      打印输出
     * @param history 源数据
     * @param page    页数
     * @param order   排序
     * @param all     是否显示全部
     */
    private void printChange(PrintWriter pw, History<Stock> history, int page, int order, boolean all) {
        History<Stock> result = resortStockList(history, order);
        if (all) {
            pw.print(JSON.toJSONString(LeekResponse.successResponse(result)));
            return;
        }
        History<Stock> change = new History<>();
        change.setTitle(result.getTitle());
        change.setUpdatedAt(result.getUpdatedAt());
        change.setList(result.getList().subList(20 * (page - 1), 20 * page));
        pw.print(JSON.toJSONString(LeekResponse.successResponse(change)));
    }
}
