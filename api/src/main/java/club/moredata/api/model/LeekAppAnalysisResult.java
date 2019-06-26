package club.moredata.api.model;

import java.io.Serializable;

/**
 * app端自定义组合分析结果封装
 * @author yeluodev1226
 */
public class LeekAppAnalysisResult implements Serializable {
    private static final long serialVersionUID = 614334135378558604L;

    private LeekResult<AlsStock> stock;
    private LeekResult<AlsSegment> segment;
    private LeekResult<AlsRebalancing> rebalancing;

    public LeekResult<AlsStock> getStock() {
        return stock;
    }

    public void setStock(LeekResult<AlsStock> stock) {
        this.stock = stock;
    }

    public LeekResult<AlsSegment> getSegment() {
        return segment;
    }

    public void setSegment(LeekResult<AlsSegment> segment) {
        this.segment = segment;
    }

    public LeekResult<AlsRebalancing> getRebalancing() {
        return rebalancing;
    }

    public void setRebalancing(LeekResult<AlsRebalancing> rebalancing) {
        this.rebalancing = rebalancing;
    }
}
