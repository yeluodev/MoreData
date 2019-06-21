package club.moredata.api.servlet;

import club.moredata.api.model.*;
import club.moredata.api.task.AnalysisTask;
import club.moredata.common.db.sql.OrderType;
import club.moredata.common.db.sql.RebalancingType;
import club.moredata.common.util.ExcelUtil;
import com.alibaba.fastjson.JSON;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author yeluodev1226
 */
@WebServlet(name = "DownloadServlet", urlPatterns = "/download/*")
public class DownloadServlet extends BaseServlet {

    private static final long serialVersionUID = 6286626351893740410L;
    private static final String EXCEL_FILE_PATH = "/var/local/club.moredata/excel/";
//    public static final String EXCEL_FILE_PATH = "D:/excel/";
//    public static final String EXCEL_FILE_PATH = "H:/excel/";

    private String[] stockHeadArr = new String[]{"序号", "名称", "代码", "所属板块", "持有组合数", "比重/%", "满仓比重/%",};
    private String[] segmentHeadArr = new String[]{"序号", "名称", "比重/%", "满仓比重/%",};
    private String[] rebalancingHeadArr = new String[]{"序号", "名称", "代码", "调仓比重/%",};
    private String[] cubeHeadArr = new String[]{"序号", "名称", "代码", "主理人", "净值", "在榜天数",};

    @Override
    public void dealRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String level = request.getParameter("level");

        if (level == null) {
            level = "1";
        }

        if (!levelPattern.matcher(level).matches()) {
            LeekResponse leekResponse = LeekResponse.errorParameterResponse();
            PrintWriter out = response.getWriter();
            out.print(JSON.toJSONString(leekResponse));
            return;
        }

        int levelInt = Integer.valueOf(level);


        String matchPath = request.getHttpServletMapping().getMatchValue();
        if (matchPath == null) {
            LeekResponse leekResponse = LeekResponse.errorURLResponse();
            response.getWriter().print(JSON.toJSONString(leekResponse));
            return;
        }

        AnalysisTask task = new AnalysisTask();
        String fileName = "";
        List<String> header = null;
        List<List<String>> body = new ArrayList<>();
        LeekResult leekResult = null;
        switch (matchPath) {
            case "stock":
                leekResult = task.stockRankList(levelInt, 100, 3000, false, OrderType.WEIGHT_DESC);
                header = Arrays.asList(stockHeadArr);
                for (AlsStock alsStock : (List<AlsStock>) leekResult.getList()) {
                    List<String> rowBody = new ArrayList<>();
                    rowBody.add(String.valueOf(alsStock.getRank()));
                    rowBody.add(String.valueOf(alsStock.getStockName()));
                    rowBody.add(String.valueOf(alsStock.getStockSymbol()));
                    rowBody.add(String.valueOf(alsStock.getSegmentName()));
                    rowBody.add(String.valueOf(alsStock.getCount()));
                    rowBody.add(String.valueOf(alsStock.getPercentWithCash()));
                    rowBody.add(String.valueOf(alsStock.getPercent()));
                    body.add(rowBody);
                }
                break;
            case "segment":
                leekResult = task.segmentRankList(levelInt, 100, OrderType.WEIGHT_DESC);
                header = Arrays.asList(segmentHeadArr);
                for (AlsSegment alsSegment : (List<AlsSegment>) leekResult.getList()) {
                    List<String> rowBody = new ArrayList<>();
                    rowBody.add(String.valueOf(alsSegment.getRank()));
                    rowBody.add(String.valueOf(alsSegment.getSegmentName()));
                    rowBody.add(String.valueOf(alsSegment.getPercentWithCash()));
                    rowBody.add(String.valueOf(alsSegment.getPercent()));
                    body.add(rowBody);
                }
                break;
            case "rebalancing":
                leekResult = task.rebalancingRankList(levelInt, 100, 3000,
                        OrderType.CHANGE_WEIGHT_DESC, RebalancingType.ALL);
                header = Arrays.asList(rebalancingHeadArr);
                for (AlsRebalancing alsRebalancing : (List<AlsRebalancing>) leekResult.getList()) {
                    List<String> rowBody = new ArrayList<>();
                    rowBody.add(String.valueOf(alsRebalancing.getRank()));
                    rowBody.add(String.valueOf(alsRebalancing.getStockName()));
                    rowBody.add(String.valueOf(alsRebalancing.getStockSymbol()));
                    rowBody.add(String.valueOf(alsRebalancing.getPercent()));
                    body.add(rowBody);
                }
                break;
            case "cube":
                leekResult = task.cubeRankList(levelInt, Integer.MAX_VALUE);
                header = Arrays.asList(cubeHeadArr);
                for (AlsCube alsCube : (List<AlsCube>) leekResult.getList()) {
                    List<String> rowBody = new ArrayList<>();
                    rowBody.add(String.valueOf(alsCube.getRank()));
                    rowBody.add(String.valueOf(alsCube.getName()));
                    rowBody.add(String.valueOf(alsCube.getSymbol()));
                    rowBody.add(String.valueOf(alsCube.getScreenName()));
                    rowBody.add(String.valueOf(alsCube.getNetValue()));
                    rowBody.add(String.valueOf(alsCube.getShowDaysCount()));
                    body.add(rowBody);
                }
                break;
            default:
                break;
        }

        if (header == null || header.size() == 0) {
            LeekResponse leekResponse = LeekResponse.errorParameterResponse();
            response.getWriter().print(JSON.toJSONString(leekResponse));
            return;
        }
        fileName = String.format("%s_%s_%s.xls", matchPath, filePrefix(levelInt), leekResult.getUpdatedAt().replaceAll("[: \\-]",
                ""));
        try {
            File file = new File(EXCEL_FILE_PATH + fileName);
            if (!file.exists()) {
                OutputStream outputStream = new FileOutputStream(EXCEL_FILE_PATH + fileName);
                ExcelUtil.generateExcel(header, body, outputStream);
            }
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            FileInputStream fis = new FileInputStream(file);
            OutputStream os = response.getOutputStream();
            int length;
            byte[] buffer = new byte[1024];
            while ((length = fis.read(buffer)) != -1) {
                os.write(buffer, 0, length);
            }
            os.flush();
            os.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String filePrefix(int level) {
        String prefix = "";
        switch (level) {
            case 1:
                prefix = "quarter";
                break;
            case 2:
                prefix = "halfyear";
                break;
            case 3:
                prefix = "year";
                break;
            default:
                break;
        }
        return prefix;
    }
}
