import java.util.Date;

public interface Repository {
    Report getReport(Date asOf, String symbol);
    Report getMAReport(Date asOf, String symbol);
}
