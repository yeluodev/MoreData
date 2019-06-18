package club.moredata.task;

import club.moredata.api.ApiManager;
import club.moredata.db.OrderType;
import club.moredata.db.SQLBuilder;
import club.moredata.entity.Post;
import club.moredata.entity.Rebalancing;
import club.moredata.entity.UploadImage;
import club.moredata.model.AlsStock;
import club.moredata.model.LeekResult;
import club.moredata.model.RebStock;
import club.moredata.util.Arith;
import club.moredata.util.DBPoolConnection;
import club.moredata.util.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.PropertyFilter;
import com.google.gson.Gson;
import net.coobird.thumbnailator.Thumbnails;
import okhttp3.Response;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 自动化任务--发帖/调仓
 *
 * @author yeluodev1226
 */
public class AutoTask {

    public static void main(String[] args) {
        AutoTask task = new AutoTask();
//        Rebalancing result = task.rebalancingCube(1, 100, 15, false, true, OrderType.WEIGHT_DESC);
//        System.out.println(JSON.toJSONString(result));

//        task.compressImage("");

//        System.out.println(task.newPost(""));
        task.trackCube(1, 100, 15, true, false, OrderType.WEIGHT_DESC);
    }

    private LeekResult<RebStock> trackCube(LeekResult<AlsStock> alsStockLeekResult, boolean noCash){
        List<AlsStock> stockList = alsStockLeekResult.getList();
        List<RebStock> rebStockList = new ArrayList<>();
        int total = 0;
        for (AlsStock stock : stockList) {
            RebStock rebStock = new RebStock();
            rebStock.setStockId(stock.getStockId());
            rebStock.setStockName(stock.getStockName());
            rebStock.setSegmentName(stock.getSegmentName());
            double percent = noCash ? stock.getPercent() : stock.getPercentWithCash();
            rebStock.setWeight((int) percent);
            rebStock.setSourceWeight((int) percent);
            rebStock.setSourceDecimal(Arith.sub(percent, (int) percent));
            rebStockList.add(rebStock);

            total += rebStock.getWeight();
        }

        int cash = noCash ? 0 : (int) alsStockLeekResult.getCash();
        int desTotal = 100 - cash;
        if (total < desTotal) {
            Collections.sort(rebStockList);
            Collections.reverse(rebStockList);
            for (RebStock rebStock : rebStockList) {
                rebStock.setWeight(rebStock.getSourceWeight() + 1);
                total++;
                if (total >= desTotal) {
                    break;
                }
            }
        }

        rebStockList.sort(Comparator.comparingInt(RebStock::getWeight));
        Collections.reverse(rebStockList);

        LeekResult<RebStock> result = new LeekResult<>();
        result.setCash(cash);
        result.setCount(rebStockList.size());
        result.setUpdatedAt(DateUtil.getInstance().getDate(System.currentTimeMillis()));
        result.setList(rebStockList);

        return result;
    }

    public LeekResult<RebStock> trackCube(int level, int cubeLimit, int stockLimit, boolean removeSuspensionStock,
                                    boolean noCash, OrderType orderType) {
        AnalysisTask task = new AnalysisTask();
        LeekResult<AlsStock> leekResult = task.stockRankList(level, cubeLimit, stockLimit, removeSuspensionStock,
                orderType);
        return trackCube(leekResult,noCash);
    }

    public LeekResult<RebStock> trackCube(String cubeIds, int cubeSize,int stockLimit, boolean removeSuspensionStock,
                                          boolean noCash, OrderType orderType) {
        AnalysisTask task = new AnalysisTask();
        LeekResult<AlsStock> leekResult = task.stockRankList(cubeIds, cubeSize, stockLimit, removeSuspensionStock,
                orderType);
        return trackCube(leekResult,noCash);
    }

    /**
     * 调仓
     *
     * @param level
     * @param cubeLimit
     * @param stockLimit
     * @param removeSuspensionStock
     * @param noCash
     * @param orderType
     */
    public Rebalancing rebalancingCube(int level, int cubeLimit, int stockLimit, boolean removeSuspensionStock,
                                       boolean noCash, OrderType orderType) {
        Rebalancing rebalancing = null;
        LeekResult<RebStock> leekResult = trackCube(level, cubeLimit, stockLimit, removeSuspensionStock, noCash, orderType);

        int cubeId = 0;
        String date = DateUtil.getInstance().getDate(System.currentTimeMillis());
        String format = "%s风云榜跟踪-%s每日自动调仓 by yeluodev1226";
        String comment = "";
        switch (level) {
            case 1:
                cubeId = 2002629;
                comment = String.format(format, date, "季度榜");
                break;
            case 2:
                cubeId = 2002630;
                comment = String.format(format, date, "半年榜");
                break;
            case 3:
                cubeId = 2002631;
                comment = String.format(format, date, "年度榜");
                break;
            default:
                break;
        }
//        cubeId = 1982218;
        String holdings = JSON.toJSONString(leekResult.getList(), (PropertyFilter) (object, name, value) -> !name.equals("source_weight") && !name.equals("source_decimal"));

        Response response = ApiManager.getInstance().rebalancingCube(cubeId, (int) leekResult.getCash(), holdings, comment);
        if (response.code() >= 200 && response.code() < 300) {
            try {
                String result = response.body().string();
                Gson gson = new Gson();
                rebalancing = gson.fromJson(result, Rebalancing.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return rebalancing;
    }

    public void compressImage(String filePath) {
        try {
            Thumbnails.of(new File("C:\\Users\\yeluodev1226\\Desktop\\test.png"))
                    .scale(1f)
                    .outputQuality(0.8f)
                    .toFile("D:\\tiny_test.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发帖
     *
     * @param fileName
     * @return
     */
    public Post newPost(String fileName) {
        Connection connection = null;
        UploadImage uploadImage = null;
        Post post = null;
        try {
            File file = new File(fileName);
            Response uploadResponse = ApiManager.getInstance().uploadImage(file);
            if (uploadResponse != null && uploadResponse.code() >= 200 && uploadResponse.code() < 300) {
                String result = uploadResponse.body().string();
                System.out.println(result);
                Gson gson = new Gson();
                uploadImage = gson.fromJson(result, UploadImage.class);
            }

            connection = DBPoolConnection.getInstance().getConnection();
            PreparedStatement queryPs = connection.prepareStatement(SQLBuilder.buildStockTop3Query());
            ResultSet resultSet = queryPs.executeQuery();
            StringBuilder stringBuilder = new StringBuilder();
            while (resultSet.next()) {
                stringBuilder.append("$");
                stringBuilder.append(resultSet.getString(1));
                stringBuilder.append("(");
                stringBuilder.append(resultSet.getString(2));
                stringBuilder.append(")$ ");
            }

            stringBuilder.append("每日风云榜跟踪");
            stringBuilder.append(DateUtil.getInstance().getDate(System.currentTimeMillis()));
            stringBuilder.append(" by yeluodev1226");
            stringBuilder.append("<br/><img class=\"ke_img\" src=\"http://xqimg.imedao.com/");
            stringBuilder.append(uploadImage.getFilename());
            stringBuilder.append("!custom.jpg\" />");
            System.out.println(stringBuilder.toString());

            Response response = ApiManager.getInstance().newPost(stringBuilder.toString());
            if (response != null && response.code() >= 200 && response.code() < 300) {
                String result = response.body().string();
                Gson gson = new Gson();
                post = gson.fromJson(result, Post.class);

            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != connection) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return post;
    }

}
