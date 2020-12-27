import java.util.Date;

public class RepositoryImpl implements Repository{

    /**
     * Get a Report object
     * @param asOf the selected date for generate the report
     * @param symbol the symbol of the selected warrant
     * @return a daily report
     */
    public Report getReport(Date asOf, String symbol) {
        return new ReportImpl(asOf, symbol);
    }

    /**
     * Get a MAReport object with moving average
     * @return a daily report with moving average information
     */
    public MAReport getMAReport(Date asOf, String symbol) {
        return new ReportImpl(asOf, symbol);
    }
}
