import java.util.Date;
import java.util.List;

public class ReportImpl implements Report, MAReport{
    Date asOf;
    String symbol;
    List<Product> productList;
    List<String> symbolList;
    List<String> underlyingList;
    List<TradeRecord> marketDataList;
    List<TradeRecord> dailyInventoryList;
    List<TradeRecord> dailyPnlList;

    public ReportImpl(Date asOf, String symbol){
        this.asOf = asOf;
        this.symbol = symbol;
        productList = Main.getProductList();
        symbolList = Main.getSymbolList();
        underlyingList = Main.getUnderlyingList();
        marketDataList = Main.getMarketDataList();
        dailyInventoryList = Main.getDailyInventoryList();
        dailyPnlList = Main.getDailyPnlList();
    }

    /**
     * Get report date
     * @return the date of report
     */
    public Date getAsOf() {
        return asOf;
    }


    /**
     * Get warrant symbol
     * @return the selected warrant symbol
     */
    public String getSymbol() {
        return symbol;
    }


    /**
     * Get stock name of the current warrant symbol as of the selected date
     * @return the underlying stock of "symbol"
     */
    public String getUnderlying() {
        for (int i = 0; i < productList.size(); i++) {
            if (productList.get(i).symbol.equals(symbol) && asOf.compareTo(productList.get(i).listDate) >= 0 && asOf.compareTo(productList.get(i).expDate) <= 0)
                return productList.get(i).underlying;
        }
        System.out.println("Input date or warrant symbol is incorrect.");
        return null;
    }


    /**
     * Get strike value of the current warrant symbol as of the selected date
     * @return strike value
     */
    public double getStrike() {
        for (int i = 0; i < productList.size(); i++) {
            if (productList.get(i).symbol.equals(symbol) && asOf.compareTo(productList.get(i).listDate) >= 0 && asOf.compareTo(productList.get(i).expDate) <= 0)
                return productList.get(i).strike;
        }
        System.out.println("Input date or warrant symbol is incorrect.");
        return 0;
    }


    /**
     * Get whether the warrant is a Call warrant or Put warrant
     * @return call or put
     */
    public String getCallPut() {
        for (int i = 0; i < productList.size(); i++) {
            if (productList.get(i).symbol.equals(symbol) && asOf.compareTo(productList.get(i).listDate) >= 0 && asOf.compareTo(productList.get(i).expDate) <= 0)
                return productList.get(i).callPut;
        }
        System.out.println("Input date or warrant symbol is incorrect.");
        return null;
    }


    /**
     * Get the issuer of the warrant
     * @return the name of issuer
     */
    public String getIssuer() {
        for (int i = 0; i < productList.size(); i++) {
            if (productList.get(i).symbol.equals(symbol) && asOf.compareTo(productList.get(i).listDate) >= 0 && asOf.compareTo(productList.get(i).expDate) <= 0)
                return productList.get(i).issuer;
        }
        System.out.println("Input date or warrant symbol is incorrect.");
        return null;
    }


    /**
     * Get the stock value of the selected date
     * @return underlying stock value of "asOf"
     */
    public double getUnderlyingLast() {
        for (int i = 0; i < marketDataList.size(); i++){
            if (asOf.compareTo(marketDataList.get(i).date) == 0)
                return marketDataList.get(i).values[underlyingList.indexOf(getUnderlying())];
        }

        System.out.println("Input date or warrant symbol is incorrect.");
        return 0;
    }


    /**
     * Get inventory value
     * @return inventory value
     */
    public double getInventory() {
        for (int i = 0; i < symbolList.size(); i++) {
            if (symbolList.get(i).equals(symbol)) {
                for (int j = 0; j < dailyInventoryList.size(); j++) {
                    if (dailyInventoryList.get(j).date.equals(asOf))
                        return dailyInventoryList.get(j).values[i];
                }
            }
        }
        System.out.println("Input date or warrant symbol is incorrect.");
        return 0;
    }


    /**
     * Get PnL value
     * @return PnL value
     */
    public double getPnl() {
        for (int i = 0; i < symbolList.size(); i++) {
            if (symbolList.get(i).equals(symbol)) {
                for (int j = 0; j < dailyPnlList.size(); j++) {
                    if (dailyPnlList.get(j).date.equals(asOf))
                        return dailyPnlList.get(j).values[i];
                }
            }
        }
        System.out.println("Input date or warrant symbol is incorrect.");
        return 0;
    }


    /**
     * Get 3-Day Moving Average
     * @return 3-Day Moving Average value
     */
    public double get3DMAPnL() {
        int productIndex = -1;
        double movAvg = 0.0;

        // compare symbol > compare date range > get product index
        for (int i = 0; i < productList.size(); i++) {
            if (productList.get(i).symbol.equals(symbol) && asOf.compareTo(productList.get(i).listDate) >= 0 && asOf.compareTo(productList.get(i).expDate) <= 0){
                productIndex = i;
                break;
            }
        }

        // if only 1 day, return 1 day
        // if only 2 days, return average of 2 days
        // return normal 3-day moving average for 3 days or more
        if (productIndex > -1) {
            int day = Math.toIntExact((asOf.getTime() - productList.get(productIndex).listDate.getTime())/(1000*60*60*24));

            if (day == 0) {
                movAvg = dailyPnlList.get(0).values[symbolList.indexOf(symbol)];
            } else if (day == 1) {
                movAvg = (dailyPnlList.get(0).values[symbolList.indexOf(symbol)] + dailyPnlList.get(1).values[symbolList.indexOf(symbol)]) / 2;
            } else {
                movAvg = (dailyPnlList.get(day - 2).values[symbolList.indexOf(symbol)]
                        + dailyPnlList.get(day - 1).values[symbolList.indexOf(symbol)]
                        + dailyPnlList.get(day).values[symbolList.indexOf(symbol)]) / 3;
            }
        }
        else{
            System.out.println("Input date or warrant symbol is incorrect.");
        }
        return movAvg;
    }
}
