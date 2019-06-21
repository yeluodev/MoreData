package club.moredata.api.model;

import java.io.Serializable;

/**
 * @author yeluodev1226
 */
public class AlsSegment implements Serializable {

    private static final long serialVersionUID = -3605612819953442102L;
    private int rank = 0;
    private String segmentName = "";
    private String segmentColor = "";
    private double weight = 0;
    private int count = 0;
    private double percent = 0;
    private double percentWithCash = 0;

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getSegmentName() {
        return segmentName;
    }

    public void setSegmentName(String segmentName) {
        this.segmentName = segmentName;
    }

    public String getSegmentColor() {
        return segmentColor;
    }

    public void setSegmentColor(String segmentColor) {
        this.segmentColor = segmentColor;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getPercent() {
        return percent;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }

    public double getPercentWithCash() {
        return percentWithCash;
    }

    public void setPercentWithCash(double percentWithCash) {
        this.percentWithCash = percentWithCash;
    }
}
