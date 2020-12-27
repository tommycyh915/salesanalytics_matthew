import java.util.Date;

public class Product {
    Date listDate;
    Date expDate;
    String symbol;
    String underlying;
    double strike;
    String callPut;
    String issuer;

    public Product(Date listDate, Date expDate, String symbol, String underlying, double strike, String callPut, String issuer){
        this.listDate = listDate;
        this.expDate = expDate;
        this.symbol = symbol;
        this.underlying = underlying;
        this.strike = strike;
        this.callPut = callPut;
        this.issuer = issuer;
    }

}
