package club.moredata.api.servlet.miniprogram;

import club.moredata.api.model.*;
import club.moredata.api.servlet.BaseServlet;
import club.moredata.api.task.AnalysisTask;
import club.moredata.common.db.sql.OrderType;
import club.moredata.common.db.sql.RebalancingType;
import com.alibaba.fastjson.JSON;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 小程序首页接口API
 *
 * @author yeluodev1226
 */
@WebServlet(name = "OverviewServlet", urlPatterns = "/mini/overview")
public class OverviewServlet extends BaseServlet {
    private static final long serialVersionUID = -1575662711817085735L;

    @Override
    public void dealRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();

        LeekResponse leekResponse;

        AnalysisTask analysisTask = new AnalysisTask();

        //TODO 图片地址待更新，加入缓存，不能每次访问都要去查数据库
        LeekResult<AlsCube> cubeLeekResult = analysisTask.snowballCubeList(3);
        Section<ItemEntity> cubeSection = new Section<>();
        cubeSection.setId(1);
        cubeSection.setTitle("风云榜");
        cubeSection.setSubtitle("雪球");
        cubeSection.setColor("#ff5722");
        cubeSection.setIcon("https://www.moredata.club/imgs/snowball.jpg");
        cubeSection.setUpdatedAt(cubeLeekResult.getUpdatedAt());
        List<ItemEntity> cubeList = new ArrayList<>(4);
        cubeLeekResult.getList().subList(0, 4).forEach(alsCube -> cubeList.add(new ItemEntity(alsCube.getRank(),
                alsCube.getName(), alsCube.getGainOnLevel())));
        cubeSection.setList(cubeList);

        LeekResult<AlsStock> stockLeekResult = analysisTask.stockRankList(3, 100, 4, false, OrderType.WEIGHT_DESC);
        Section<ItemEntity> stockSection = new Section<>();
        stockSection.setId(2);
        stockSection.setTitle("个股榜单");
        stockSection.setSubtitle("持仓比重");
        stockSection.setColor("#222831");
        stockSection.setIcon("https://www.moredata.club/imgs/snowball.jpg");
        stockSection.setUpdatedAt(stockLeekResult.getUpdatedAt());
        List<ItemEntity> stockList = new ArrayList<>(4);
        stockLeekResult.getList().subList(0, 4).forEach(alsStock -> stockList.add(new ItemEntity(alsStock.getRank(), alsStock.getStockName())));
        stockSection.setList(stockList);

        LeekResult<AlsSegment> segmentLeekResult = analysisTask.segmentRankList(3, 100, OrderType.WEIGHT_DESC);
        Section<ItemEntity> segmentSection = new Section<>();
        segmentSection.setId(3);
        segmentSection.setTitle("板块榜单");
        segmentSection.setSubtitle("持仓比重");
        segmentSection.setColor("#2d4059");
        segmentSection.setIcon("https://www.moredata.club/imgs/snowball.jpg");
        segmentSection.setUpdatedAt(segmentLeekResult.getUpdatedAt());
        List<ItemEntity> segmentList = new ArrayList<>(4);
        segmentLeekResult.getList().subList(0, 4).forEach(alsSegment -> segmentList.add(new ItemEntity(alsSegment.getRank(), alsSegment.getSegmentName())));
        segmentSection.setList(segmentList);

        LeekResult<AlsRebalancing> inLeekResult = analysisTask.rebalancingRankList(3, 100, 4,
                OrderType.CHANGE_WEIGHT_DESC, RebalancingType.IN);
        Section<ItemEntity> inSection = new Section<>();
        inSection.setId(4);
        inSection.setTitle("买入榜");
        inSection.setSubtitle("");
        inSection.setColor("#2b3595");
        inSection.setIcon("https://www.moredata.club/imgs/snowball.jpg");
        inSection.setUpdatedAt(inLeekResult.getUpdatedAt());
        List<ItemEntity> inList = new ArrayList<>(4);
        inLeekResult.getList().subList(0, 4).forEach(alsRebalancing -> inList.add(new ItemEntity(alsRebalancing.getRank(), alsRebalancing.getStockName())));
        inSection.setList(inList);

        LeekResult<AlsRebalancing> outLeekResult = analysisTask.rebalancingRankList(3, 100, 4,
                OrderType.CHANGE_WEIGHT_ASC, RebalancingType.OUT);
        Section<ItemEntity> outSection = new Section<>();
        outSection.setId(5);
        outSection.setTitle("卖出榜");
        outSection.setSubtitle("");
        outSection.setColor("#7045af");
        outSection.setIcon("https://www.moredata.club/imgs/snowball.jpg");
        outSection.setUpdatedAt(outLeekResult.getUpdatedAt());
        List<ItemEntity> outList = new ArrayList<>(4);
        outLeekResult.getList().subList(0, 4).forEach(alsRebalancing -> outList.add(new ItemEntity(alsRebalancing.getRank(), alsRebalancing.getStockName())));
        outSection.setList(outList);

        LeekResult<AlsCube> daysLeekResult = analysisTask.cubeRankList(3, 4);
        Section<ItemEntity> daysSection = new Section<>();
        daysSection.setId(6);
        daysSection.setTitle("组合榜");
        daysSection.setSubtitle("在榜天数");
        daysSection.setColor("#e14594");
        daysSection.setIcon("https://www.moredata.club/imgs/snowball.jpg");
        daysSection.setUpdatedAt(daysLeekResult.getUpdatedAt());
        List<ItemEntity> daysList = new ArrayList<>(4);
        daysLeekResult.getList().subList(0, 4).forEach(alsCube -> daysList.add(new ItemEntity(alsCube.getRank(), alsCube.getName())));
        daysSection.setList(daysList);

        List<Section> list = Arrays.asList(cubeSection, stockSection, segmentSection, inSection, outSection, daysSection);
        leekResponse = LeekResponse.successResponse(list);

        out.print(JSON.toJSONString(leekResponse));

    }

    static class ItemEntity implements Serializable {
        private static final long serialVersionUID = -4048109960388644878L;
        private int rank;
        private String name;
        private Double value;

        public ItemEntity(int rank, String name) {
            this(rank, name, null);
        }

        public ItemEntity(int rank, String name, Double value) {
            this.rank = rank;
            this.name = name;
            this.value = value;
        }

        public int getRank() {
            return rank;
        }

        public void setRank(int rank) {
            this.rank = rank;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }
    }
}
