package club.moredata.common.db.sql;

/**
 * 调仓类型
 *
 * @author yeluodev1226
 */
public enum RebalancingType {

    /**
     * 买入、卖出、全部
     */
    IN("2"),
    OUT("3"),
    ALL("1");

    private String value;

    RebalancingType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static RebalancingType getType(String value) {
        RebalancingType type;
        switch (value) {
            case "2":
                type = IN;
                break;
            case "3":
                type = OUT;
                break;
            case "1":
            default:
                type = ALL;
                break;
        }
        return type;
    }
}
