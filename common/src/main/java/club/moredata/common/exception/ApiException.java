package club.moredata.common.exception;

/**
 * 雪球api访问异常
 *
 * @author yeluodev1226
 */
public class ApiException extends Exception {
    private static final long serialVersionUID = 2649028163926090476L;

    /**
     * 出错股票代码
     */
    private String symbol;

    public ApiException(String message) {
        super(message);
        this.symbol = "";
    }

    public ApiException(String symbol, String message) {
        super(message);
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
