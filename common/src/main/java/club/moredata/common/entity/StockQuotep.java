package club.moredata.common.entity;

import java.io.Serializable;

/**
 * 组合个股简要信息
 *
 * @author yeluodev1226
 */
public class StockQuotep implements Serializable {

    private static final long serialVersionUID = -26788428669590002L;
    private String symbol;
    private double current;
    private int flag;
    private double percentage;
    private double change;
    private String name;
    private String time;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getCurrent() {
        return current;
    }

    public void setCurrent(double current) {
        this.current = current;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public double getChange() {
        return change;
    }

    public void setChange(double change) {
        this.change = change;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
