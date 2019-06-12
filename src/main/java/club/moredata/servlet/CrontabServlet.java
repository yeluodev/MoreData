package club.moredata.servlet;

import club.moredata.model.LeekResponse;
import club.moredata.task.CubeTask;
import club.moredata.task.UpdateTask;
import com.alibaba.fastjson.JSON;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author yeluodev1226
 */
@WebServlet(name = "CrontabServlet",urlPatterns = "/crontab/*")
public class CrontabServlet extends BaseServlet {
    private static final long serialVersionUID = -2675329387870425002L;

    @Override
    public void dealRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        String matchPath = request.getHttpServletMapping().getMatchValue();
        CubeTask task = new CubeTask();
        LeekResponse leekResponse;
        //TODO 暂时不去处理任务执行的结果，直接返回任务是否执行即可
        switch (matchPath) {
            case "list":
                task.fetchCubeRankListAndInsert2DB();
                leekResponse = LeekResponse.successResponse(null);
                break;
            case "detail":
                task.fetchCubeDetailAndInsert2DB();
                leekResponse = LeekResponse.successResponse(null);
                break;
            case "cube/update":
                UpdateTask.getInstance().runUpdateTask();
                leekResponse = LeekResponse.successResponse(null);
                break;
            case "cube/reset":
                UpdateTask.getInstance().resetUpdateTask();
                leekResponse = LeekResponse.successResponse(null);
                break;
            default:
                leekResponse = LeekResponse.errorURLResponse();
                break;
        }

        out.print(JSON.toJSONString(leekResponse));
    }
}
