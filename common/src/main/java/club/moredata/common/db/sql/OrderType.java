package club.moredata.common.db.sql;

/**
 * 排序规则
 *
 * @author yeluodev1226
 */
public enum OrderType {

    /**
     * 计数降序/升序、比重降序/升序、调仓比重降序/升序
     */
    WEIGHT_DESC("1"),
    WEIGHT_ASC("2"),
    COUNT_DESC("3"),
    COUNT_ASC("4"),
    CHANGE_WEIGHT_DESC("5"),
    CHANGE_WEIGHT_ASC("6");

    private String value;

    OrderType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static OrderType getType(String value) {
        if (value == null) {
            return WEIGHT_DESC;
        }
        OrderType type;
        switch (value) {
            case "2":
                type = WEIGHT_ASC;
                break;
            case "3":
                type = COUNT_DESC;
                break;
            case "4":
                type = COUNT_ASC;
                break;
            case "5":
                type = CHANGE_WEIGHT_DESC;
                break;
            case "6":
                type = CHANGE_WEIGHT_ASC;
                break;
            case "1":
            default:
                type = WEIGHT_DESC;
                break;
        }
        return type;
    }
}
