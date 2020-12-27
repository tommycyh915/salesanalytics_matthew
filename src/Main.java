import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {
    // define the date format in files
    static DateFormat dFormat = new SimpleDateFormat("M/d/yyyy");

    // basic information
    static List<Product> productList;
    static List<String> symbolList;
    static List<String> underlyingList;
    static List<String> issuerList;

    // trade data
    static List<TradeRecord> marketDataList;
    static List<TradeRecord> dailyInventoryList;
    static List<TradeRecord> dailyPnlList;

    // Report Generator
    static RepositoryImpl repo;

    // getter functions
    /** @return list of Products **/
    static List<Product> getProductList(){
        return productList;
    }

    /** @return list of warrant symbols **/
    static List<String> getSymbolList(){
        return symbolList;
    }

    /** @return list of underlying stocks **/
    static List<String> getUnderlyingList(){
        return underlyingList;
    }

    /** @return list of issuers **/
    static List<String> getIssuerList(){
        return issuerList;
    }

    /** @return list of trade records in Market Data **/
    static List<TradeRecord> getMarketDataList(){
        return marketDataList;
    }

    /** @return list of trade records in Daily Inventory Data **/
    static List<TradeRecord> getDailyInventoryList(){
        return dailyInventoryList;
    }

    /** @return list of trade records in Daily PnL Data **/
    static List<TradeRecord> getDailyPnlList(){
        return dailyPnlList;
    }

    /**
     * Initializing parameters
     */
    static void init(){
        productList = new ArrayList<>();
        symbolList = new ArrayList<>();
        underlyingList = new ArrayList<>();
        issuerList = new ArrayList<>();
        marketDataList = new ArrayList<>();
        dailyInventoryList = new ArrayList<>();
        dailyPnlList = new ArrayList<>();

        repo = new RepositoryImpl();
    }

    /**
     * Import data from Production Definition
     * @param filePath the path for the file load from
     */
    static void importProductDefinition(String filePath){
        String line;
        String splitter = ",";
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(filePath));
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                String[] tmp = line.split(splitter);

                // skipping the first line to read (column titles)
                if (isFirstLine){
                    isFirstLine = false;
                    continue;
                }

                // import data to class instances line by line
                // Product(Date listDate, Date expDate, String symbol, String underlying, double strike, String callPut, String issuer)
                productList.add(new Product(dFormat.parse(tmp[0]), dFormat.parse(tmp[1]), tmp[2], tmp[3], Double.parseDouble(tmp[4]), tmp[5], tmp[6]));

                // gather information for report generation and importing remaining files
                if (!symbolList.contains(tmp[2]))
                    symbolList.add(tmp[2]);

                if (!underlyingList.contains(tmp[3]))
                    underlyingList.add(tmp[3]);

                if (!issuerList.contains(tmp[6]))
                    issuerList.add(tmp[6]);
            }
            System.out.println("Import Production Definition data successful");
        } catch (Exception e) {
            throw new RuntimeException("Import File Error on Production Definition: ", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    throw new RuntimeException("Import File Error on Production Definition: ", e);
                }
            }
        }
    }

    /**
     * Import data from Market Data
     * @param filePath the path for the file load from
     */
    static void importMarketData(String filePath){
        String line;
        String splitter = ",";
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filePath));
            boolean isFirstLine = true;

            int[] colOrder = new int[underlyingList.size()];

            while ((line = reader.readLine()) != null) {
                String[] tmp = line.split(splitter);

                // skipping the first line to be stored into data (column titles), instead, verify and index the column titles
                if (isFirstLine){
                    // verify underlying stocks
                    // if market contains more or less stocks than in product definition
                    if (tmp.length - 1 != underlyingList.size())
                        throw new Exception("The number of stocks in MarketData is not the same as in Product Definition");

                    // if market data contains stocks differs from product definition
                    for (int i = 1; i < tmp.length; i++){
                        if (!underlyingList.contains(tmp[i]))
                            throw new Exception("The stocks in MarketData is different from Product Definition");
                    }

                    // rearrange the order of columns, to match with the order in product definition
                    for (int i = 0; i < underlyingList.size(); i++)
                        colOrder[i] = underlyingList.indexOf(tmp[i+1]); // records which column the data should be reorder to

                    // ending first line process
                    isFirstLine = false;
                    continue;
                }

                // data to double array and rearrangement
                double[] values = new double[underlyingList.size()];
                for (int i = 1; i < tmp.length; i++)
                    values[colOrder[i-1]] = Double.parseDouble(tmp[i]);

                // import data to market data list, each day a row (data list item)
                marketDataList.add(new TradeRecord(dFormat.parse(tmp[0]), values));
            }
            System.out.println("Import Market Data successful");
        } catch (Exception e) {
            throw new RuntimeException("Import File Error on Market Data: ", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    throw new RuntimeException("Import File Error on Market Data: ", e);
                }
            }
        }
    }

    /**
     * Import data from Daily Inventory and Daily PnL
     * Note: File name must contain "DailyInventory" or "DailyPnL"
     * as this will be used to determine where the data will be imported
     *
     * @param filePath the path for the file load from
     */
    static void importDailyData(String filePath){
        String line;
        String splitter = ",";
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filePath));
            boolean isFirstLine = true;

            int[] colOrder = new int[symbolList.size()];

            while ((line = reader.readLine()) != null) {
                String[] tmp = line.split(splitter);

                // skipping the first line to be stored into data (column titles), instead, verify and index the column titles
                if (isFirstLine){
                    // verify underlying stocks
                    // if market contains more or less stocks than in product definition
                    if (tmp.length - 1 != symbolList.size()){
                        if(filePath.contains("DailyInventory"))
                            throw new Exception("The number of warrants in DailyInventory is not the same as in Product Definition");
                        else if(filePath.contains("DailyPnL"))
                            throw new Exception("The number of warrants in DailyPnL is not the same as in Product Definition");
                        else
                            throw new Exception("File content error");
                    }


                    // if market data contains stocks differs from product definition
                    for (int i = 1; i < tmp.length; i++){
                        if (!symbolList.contains(tmp[i])) {
                            if(filePath.contains("DailyInventory"))
                                throw new Exception("The warrant symbol in DailyInventory is different from Product Definition");
                            else if(filePath.contains("DailyPnL"))
                                throw new Exception("The warrant symbol in DailyPnL is different from Product Definition");
                            else
                                throw new Exception("File content error");
                        }
                    }

                    // rearrange the order of columns, to match with the order in product definition
                    for (int i = 0; i < symbolList.size(); i++)
                        colOrder[i] = symbolList.indexOf(tmp[i+1]); // records which column the data should be reorder to

                    // ending first line process
                    isFirstLine = false;
                    continue;
                }

                // data to double array and rearrangement
                double[] values = new double[symbolList.size()];
                for (int i = 1; i < tmp.length; i++)
                    values[colOrder[i-1]] = Double.parseDouble(tmp[i]);

                // import data to market data list, each day a row (data list item)
                if(filePath.contains("DailyInventory"))
                    dailyInventoryList.add(new TradeRecord(dFormat.parse(tmp[0]), values));
                else if(filePath.contains("DailyPnL"))
                    dailyPnlList.add(new TradeRecord(dFormat.parse(tmp[0]), values));
            }

            if(filePath.contains("DailyInventory"))
                System.out.println("Import Daily Inventory Data successful");
            else if(filePath.contains("DailyPnL"))
                System.out.println("Import Daily PnL Data successful");

        } catch (Exception e) {
            throw new RuntimeException("Import File Error on Market Data: ", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    throw new RuntimeException("Import File Error on Market Data: ", e);
                }
            }
        }
    }

    /**
     * Print imported data
     * can be used for debug or verification
     */
    static void printImportedData(){
        System.out.println("\n--- Product List ---");
        System.out.println("ListDate\tExpDate\tSymbol\tUnder\tStrike\tCallPut\tIssuer");
        for (int i = 0; i < productList.size(); i++)
            System.out.println(productList.get(i).listDate + "\t" + productList.get(i).expDate + "\t" + productList.get(i).symbol + "\t" + productList.get(i).underlying + "\t" + productList.get(i).strike + "\t" + productList.get(i).callPut + "\t" + productList.get(i).issuer);


        System.out.println("\n--- Issuer List ---");
        for (int i = 0; i < issuerList.size(); i++)
            System.out.println(issuerList.get(i));


        System.out.println("\n--- Market Data ---");
        for (int i = 0; i < underlyingList.size(); i++)
            System.out.print(underlyingList.get(i) + "\t");
        System.out.println();
        for (int i = 0; i < marketDataList.size(); i++){
            for (int j = 0; j < marketDataList.get(i).values.length; j++)
                System.out.print(marketDataList.get(i).values[j] + "\t");
            System.out.println();
        }


        System.out.println("\n--- Daily Inventory ---");
        for (int i = 0; i < symbolList.size(); i++)
            System.out.print(symbolList.get(i) + "\t");
        System.out.println();
        for (int i = 0; i < dailyInventoryList.size(); i++){
            for (int j = 0; j < dailyInventoryList.get(i).values.length; j++)
                System.out.print(dailyInventoryList.get(i).values[j] + "\t");
            System.out.println();
        }


        System.out.println("\n--- Daily PnL ---");
        for (int i = 0; i < symbolList.size(); i++)
            System.out.print(symbolList.get(i) + "\t");
        System.out.println();
        for (int i = 0; i < dailyPnlList.size(); i++){
            for (int j = 0; j < dailyPnlList.get(i).values.length; j++)
                System.out.print(dailyPnlList.get(i).values[j] + "\t");
            System.out.println();
        }

    }

    /**
     * Get day value from a given date
     * @param date a given date
     * @return day value as integer
     */
    static int getDay(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Get month value from a given date
     * @param date a given date
     * @return month value as integer, follows definition of java.util.Date, where January is 0
     */
    static int getMonth(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MONTH);
    }

    /**
     * Get year value from a given date
     * @param date a given date
     * @return year value as integer
     */
    static int getYear(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.YEAR);
    }

    /**
     * Calculate and print the sum of monthly PnL on each issuer
     * @param date a given date
     * @param symbol a given warrant symbol, like "12345.HK"
     */
    static void getMonthlyPnlReport(Date date, String symbol) {
        int year = getYear(date);
        int month = getMonth(date);

        // result under issuers
        double[] pnlSum = new double[issuerList.size()];

        while (getMonth(date) == month) {
            Report report = repo.getReport(date, symbol);

            // add daily PnL to the issuer based on the order in issuerList
            if (report.getIssuer() != null)
                pnlSum[issuerList.indexOf(report.getIssuer())] += report.getPnl();
            else
                return; // invalid input

            // day increment
            date = new Date(date.getTime() + (24 * 60 * 60 * 1000));
        }

        // print report
        System.out.println("\nMonthly PnL Report of " + year + "-" + (month+1));
        System.out.println("| Issuer | PnL ($)  |");
        System.out.println("| ------ |:--------:|");
        for (int i = 0; i < issuerList.size(); i++)
            System.out.println("| " + issuerList.get(i) + "\t | " + String.format("%.2f",pnlSum[i]) + "\t|");

    }


    /**
     * Calculate and print the 3-Day Moving Average on a selected warrant symbol
     * @param date a given date
     * @param symbol a given warrant symbol, like "12345.HK"
     */
    static void getMAReport(Date date, String symbol){
        Date listDate = date;
        Date expDate = date;
        String issuer = "";

        // find which day to start and end the warrant
        boolean isProductVaild = false;
        for (int i = 0; i < productList.size(); i++) {
            if (productList.get(i).symbol.equals(symbol) && date.compareTo(productList.get(i).listDate) >= 0 && date.compareTo(productList.get(i).expDate) <= 0){
                listDate = productList.get(i).listDate;
                expDate = productList.get(i).expDate;
                isProductVaild = true;
                break;
            }
        }

        // End if no valid product found
        if (!isProductVaild){
            System.out.println("Input date or warrant symbol is incorrect.");
            return;
        }

        // total days of the warrant last for
        int lifespan = Math.toIntExact((expDate.getTime() - listDate.getTime())/(1000*60*60*24));

        double[] pnl = new double[lifespan + 1];
        double[] movAvg = new double[lifespan + 1];

        // move to day 1
        date = listDate;

        // looping to add record day by day
        for (int i = 0; i <= lifespan; i++) {
            MAReport report = repo.getMAReport(date, symbol);
            pnl[i] = report.getPnl();
            movAvg[i] = report.get3DMAPnL();

            if (i == 0){
                issuer = report.getIssuer();
            }

            // day increment
            date = new Date(date.getTime() + (24 * 60 * 60 * 1000));
        }

        // print report
        System.out.print("\n3 days Moving Average of " + symbol + " issued by " + issuer + " ");
        System.out.println("from " + getYear(listDate) + "-" + (getMonth(listDate)+1) + "-" + getDay(listDate) + " to " + getYear(expDate) + "-" + (getMonth(expDate)+1) + "-" + getDay(expDate));

        System.out.println("| Date\t\t | Day\t | PnL\t\t | MovingAvgPnL\t|");
        System.out.println("| ---------- |:-----:|:---------:|:------------:|");
        date = listDate;
        for (int i = 0; i <= lifespan; i++) {
            System.out.println("| " + getYear(date) + "-" + (getMonth(date)+1) + "-" + getDay(date) + "\t | " + (i+1) + "\t | " + String.format("%.2f", pnl[i]) + "\t | " + String.format("%.2f", movAvg[i]) + "\t\t|");
            date = new Date(date.getTime() + (24 * 60 * 60 * 1000));
        }

    }


    /**
     * Calculate and print the 3-Day Moving Average on a selected warrant symbol
     * @param date a given date
     * @param symbol a given warrant symbol, like "12345.HK"
     */
    static void getDailyReport(Date date, String symbol){
        // get report
        Report report = repo.getReport(date, symbol);

        // verification
        if (report.getIssuer() == null)
            return; // invalid input

        // print report
        System.out.println("\nDaily Report of " + symbol + " on " + getYear(date) + "-" + (getMonth(date)+1) + "-" + (getDay(date)));
        System.out.println("Symbol:\t\t\t" + report.getSymbol());
        System.out.println("Underlying:\t\t" + report.getUnderlying());
        System.out.println("Strike:\t\t\t" + String.format("%.2f",report.getStrike()));
        System.out.println("CallPut:\t\t" + report.getCallPut());
        System.out.println("Issuer:\t\t\t" + report.getIssuer());
        System.out.println("LastStockPrice:\t" + String.format("%.2f",report.getUnderlyingLast()));
        System.out.println("Inventory:\t\t" + String.format("%.2f",report.getInventory()));
        System.out.println("PnL:\t\t\t" + String.format("%.2f",report.getPnl()));
    }


    /**
     * wait user to input a number until the choice is desired one
     * @param a the lower limit of choice
     * @param b the upper limit of choice
     * @return the choice
     */
    static int inputNum(int a, int b){
        // a = lower boundary, b = upper boundary
        int choice = 0;
        int validInput = 0;
        Scanner scanner = new Scanner(System.in);
        do {
            System.out.print("Please input your choice: ");
            if(scanner.hasNextInt()){
                choice = scanner.nextInt();
                validInput = 1;
            }
            else
                scanner.next();
        } while (choice < a || choice > b || validInput == 0);
        return choice;
    }


    /**
     * wait user to input date with correct format
     * @return the selected date
     */
     static Date inputDate(){
        Date date = new Date();
        boolean isValidInput;
        Scanner scanner = new Scanner(System.in);

        do {
            System.out.print("Please input the date, formatted as M/d/yyyy: ");
            isValidInput = true;
            try {
                date = dFormat.parse(scanner.nextLine());
            } catch (Exception e) {
                System.out.println("Date Invalid, please input again");
                isValidInput = false;
            }
        } while (!isValidInput);
        return date;
    }

    /**
     * input symbol
     * @return symbol
     */
    static String inputSymbol(){
        Scanner scanner = new Scanner( System.in );
        System.out.print("Please input the symbol: ");
        return scanner.nextLine();
    }


    /**
     * input file Path
     * @return file path
     */
    static String inputFilePath(){
        Scanner scanner = new Scanner( System.in );
        System.out.print("\nPlease input the file path: ");
        return scanner.nextLine();
    }


    /**
     * Main method
     * @param args arguments for main method
     */
    public static void main(String[] args){
        init();
        importProductDefinition("SalesAnalytics/ProductDefinition.csv");
        importMarketData("SalesAnalytics/MarketData.csv");
        importDailyData("SalesAnalytics/DailyInventory.csv");
        importDailyData("SalesAnalytics/DailyPnL.csv");

        int choice = 0;
        do {
            // welcoming message
            System.out.println("\nWelcome to Sales Report Repository Tool");
            System.out.println("1. Print Imported Data\n2. Show Monthly PnL Report\n3. Show 3-Day Moving Average Report\n4. Show Daily Report\n5. Print Demo Data\n6. Exit");

            //choice and actions
            choice = inputNum(1, 6);
            switch (choice) {
                // Print Imported Data
                case 1:
                    printImportedData();
                    break;

                // Show Monthly PnL Report
                case 2:
                    getMonthlyPnlReport(inputDate(), inputSymbol());
                    break;

                // Show 3-Day Moving Average Report
                case 3:
                    getMAReport(inputDate(), inputSymbol());
                    break;

                // Show Daily Report
                case 4:
                    getDailyReport(inputDate(), inputSymbol());
                    break;

                // Print Demo Data
                case 5:
                    getMonthlyPnlReport(new Date(120, 5, 1), "12345.HK");
                    getMAReport(new Date(120, 5, 7), "12345.HK");
                    break;

                //exit
                case 6:
                    System.out.println("Good Bye!");
                    break;
            }
        } while (choice != 6);

    }

}
