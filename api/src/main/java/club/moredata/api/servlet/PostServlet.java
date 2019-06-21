package club.moredata.api.servlet;

import club.moredata.api.model.LeekResponse;
import club.moredata.api.task.AutoTask;
import club.moredata.common.entity.Post;
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
@WebServlet(name = "PostServlet", urlPatterns = "/post")
public class PostServlet extends BaseServlet {

    private static final long serialVersionUID = -8738364596607282170L;

    @Override
    public void dealRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        LeekResponse leekResponse;

        String fileName = request.getParameter("file");
        if (fileName == null) {
            leekResponse = LeekResponse.missingParameterResponse();
            out.print(JSON.toJSONString(leekResponse));
            return;
        }

        AutoTask autoTask = new AutoTask();
        Post post = autoTask.newPost("/var/local/image/club.club.moredata/source/" + fileName);
        if (post == null) {
            leekResponse = LeekResponse.errorResponse(LeekResponse.ERROR_OTHER, LeekResponse.MSG_ERROR_OTHER);
        } else {
            leekResponse = LeekResponse.successResponse(post);
        }

        out.print(JSON.toJSONString(leekResponse));
    }


}
