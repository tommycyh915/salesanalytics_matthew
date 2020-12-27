import java.util.Date;

public interface Report {
    Date getAsOf();
    String getSymbol();
    String getUnderlying();
    double getStrike();
    String getCallPut();
    String getIssuer();
    double getUnderlyingLast();
    double getInventory();
    double getPnl();
}
