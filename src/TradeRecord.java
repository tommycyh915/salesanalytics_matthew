import java.util.Date;

public class TradeRecord {
    Date date;
    double[] values;

    public TradeRecord(Date date, double[] values){
        this.date = date;
        this.values = values;
    }
}
