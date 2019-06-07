package club.moredata.servlet;

import club.moredata.entity.Post;
import club.moredata.model.LeekResponse;
import club.moredata.task.AutoTask;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.PropertyFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author yeluodev1226
 */
@WebServlet(name = "PostServlet", urlPatterns = "/post")
public class PostServlet extends HttpServlet {

    private static final long serialVersionUID = -8738364596607282170L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("utf-8");
        response.setContentType("text/json;charset=utf-8");
        PrintWriter out = response.getWriter();

        LeekResponse leekResponse;

        String fileName = request.getParameter("file");
        if (fileName == null) {
            leekResponse = LeekResponse.missingParameterResponse();
            out.print(JSON.toJSONString(leekResponse));
            return;
        }

        AutoTask autoTask = new AutoTask();
        Post post = autoTask.newPost("/var/local/image/club.moredata/source/" + fileName);
        if (post == null) {
            leekResponse = LeekResponse.errorResponse(LeekResponse.ERROR_OTHER, LeekResponse.MSG_ERROR_OTHER);
        } else {
            leekResponse = LeekResponse.successResponse(post);
        }

        out.print(JSON.toJSONString(leekResponse));
    }


}
