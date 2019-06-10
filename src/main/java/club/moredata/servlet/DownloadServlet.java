package club.moredata.servlet;

import club.moredata.db.OrderType;
import club.moredata.db.RebalancingType;
import club.moredata.model.*;
import club.moredata.task.AnalysisTask;
import club.moredata.util.ExcelUtil;
import com.alibaba.fastjson.JSON;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author yeluodev1226
 */
@WebServlet(name = "DownloadServlet", urlPatterns = "/download/*")
public class DownloadServlet extends BaseServlet {

    private static final long serialVersionUID = 6286626351893740410L;
        public static final String EXCEL_FILE_PATH = "/var/local/club.moredata/excel/";
//    public static final String EXCEL_FILE_PATH = "D:/excel/";
    private Pattern levelPattern = Pattern.compile("[1-3]");

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
                for (AlsSegment stock : (List<AlsSegment>) leekResult.getList()) {
                    List<String> rowBody = new ArrayList<>();
                    rowBody.add(String.valueOf(stock.getRank()));
                    rowBody.add(String.valueOf(stock.getSegmentName()));
                    rowBody.add(String.valueOf(stock.getPercentWithCash()));
                    rowBody.add(String.valueOf(stock.getPercent()));
                    body.add(rowBody);
                }
                break;
            case "rebalancing":
                leekResult = task.rebalancingRankList(levelInt, 100, 3000,
                        OrderType.WEIGHT_DESC, RebalancingType.ALL);
                header = Arrays.asList(rebalancingHeadArr);
                for (AlsRebalancing stock : (List<AlsRebalancing>) leekResult.getList()) {
                    List<String> rowBody = new ArrayList<>();
                    rowBody.add(String.valueOf(stock.getRank()));
                    rowBody.add(String.valueOf(stock.getStockName()));
                    rowBody.add(String.valueOf(stock.getStockSymbol()));
                    rowBody.add(String.valueOf(stock.getPercent()));
                    body.add(rowBody);
                }
                break;
            case "cube":
                leekResult = task.cubeRankList(levelInt, Integer.MAX_VALUE);
                header = Arrays.asList(cubeHeadArr);
                for (AlsCube stock : (List<AlsCube>) leekResult.getList()) {
                    List<String> rowBody = new ArrayList<>();
                    rowBody.add(String.valueOf(stock.getRank()));
                    rowBody.add(String.valueOf(stock.getName()));
                    rowBody.add(String.valueOf(stock.getSymbol()));
                    rowBody.add(String.valueOf(stock.getScreenName()));
                    rowBody.add(String.valueOf(stock.getNetValue()));
                    rowBody.add(String.valueOf(stock.getShowDaysCount()));
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
        fileName = String.format("%s_%s_%s.xls",matchPath,filePrefix(levelInt),leekResult.getUpdatedAt().replaceAll("[: \\-]",
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
