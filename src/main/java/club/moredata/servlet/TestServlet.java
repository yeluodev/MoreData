package club.moredata.servlet;

import club.moredata.db.DbHelper;
import club.moredata.task.RedisTask;

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
@WebServlet(name = "TestServlet",urlPatterns = "/test")
public class TestServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("处理post");
        RedisTask.setValue("haha","hehe");
        PrintWriter out = response.getWriter();

        out.print("dhhsadhkhas");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("处理get");

        DbHelper.getCubeRank(1,1);

        PrintWriter out = response.getWriter();
        out.print(RedisTask.getValue("haha"));


    }
}
