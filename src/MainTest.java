import org.testng.Assert;
import org.testng.annotations.Test;
import java.util.Date;

public class MainTest {
    MAReport report;
    MAReport report2;
    MAReport report3;

    @Test
    void init(){
        Main.init();
    }

    @Test
    public void testAddData() throws Exception{
        // fake data
        Main.productList.add(new Product(new Date(120, 5, 1), new Date(120, 5, 10), "12345.HK", "AAA.HK", 270.0, "Put", "BankA"));
        Main.symbolList.add("12345.HK");
        Main.underlyingList.add("AAA.HK");
        Main.issuerList.add("BankA");

        Main.marketDataList.add(new TradeRecord(new Date(120, 5, 1), new double[]{276.5}));
        Main.dailyInventoryList.add(new TradeRecord(new Date(120, 5, 1), new double[]{1000}));
        Main.dailyPnlList.add(new TradeRecord(new Date(120, 5, 1), new double[]{-341.7}));
        Main.dailyPnlList.add(new TradeRecord(new Date(120, 5, 2), new double[]{-198.2}));
        Main.dailyPnlList.add(new TradeRecord(new Date(120, 5, 3), new double[]{38.6}));

        report = Main.repo.getMAReport(Main.productList.get(0).listDate, Main.productList.get(0).symbol);
    }


    @Test
    public void testGetDate() throws Exception{
        Assert.assertEquals(report.getAsOf(), new Date(120, 5, 1));

    }

    @Test
    public void testGetSymbol() throws Exception{
        Assert.assertEquals(report.getSymbol(), "12345.HK");
    }

    @Test
    public void testGetUnderlying() throws Exception{
        Assert.assertEquals(report.getUnderlying(), "AAA.HK");
    }

    @Test
    public void testGetStrike() throws Exception{
        Assert.assertEquals(report.getStrike(), 270.0);
    }

    @Test
    public void testGetCallPut() throws Exception{
        Assert.assertEquals(report.getCallPut(), "Put");
    }

    @Test
    public void testGetIssuer() throws Exception{
        Assert.assertEquals(report.getIssuer(), "BankA");
    }

    @Test
    public void testGetUnderlyingLast() throws Exception{
        Assert.assertEquals(report.getUnderlyingLast(), 276.5);
    }

    @Test
    public void testGetInventory() throws Exception{
        Assert.assertEquals(report.getInventory(), 1000);
    }

    @Test
    public void testGetPnl() throws Exception{
        Assert.assertEquals(report.getPnl(), -341.7);
    }

    @Test
    public void testGet3DMAPnLDay1() throws Exception{
        Assert.assertEquals(report.get3DMAPnL(), -341.7);
    }

    @Test
    public void testGet3DMAPnLDay2() throws Exception{
        report2 = Main.repo.getMAReport(new Date(120, 5, 2), Main.productList.get(0).symbol);
        Assert.assertEquals(report2.get3DMAPnL(), -269.95);
    }

    @Test
    public void testGet3DMAPnLDay3() throws Exception{
        report3 = Main.repo.getMAReport(new Date(120, 5, 3), Main.productList.get(0).symbol);
        Assert.assertEquals(report3.get3DMAPnL(), -167.1);
    }
}