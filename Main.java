import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

class Stock {
    private String symbol;
    private String name;
    private String marketCap;
    private double lastPrice;
    private double PE;
    private double ROE;
    private double DY;

    public Stock(String symbol, String name, String marketCap, double lastPrice, double PE, double ROE, double DY) {
        this.symbol = symbol;
        this.name = name;
        this.marketCap = marketCap;
        this.lastPrice = lastPrice;
        this.PE = PE;
        this.ROE = ROE;
        this.DY = DY;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public String getMarketCap() {
        return marketCap;
    }

    public double getLastPrice() {
        return lastPrice;
    }

    public double getPE() {
        return PE;
    }

    public double getROE() {
        return ROE;
    }

    public double getDY() {
        return DY;
    }
}
class StockOrder {
    private String symbol;
    private int quantity;
    private double price;
    private double expectedPrice;
    private String orderType;
    private long timeStamp;

    public StockOrder(String symbol, int quantity, double price, double expectedPrice, String orderType) {
        this.symbol = symbol;
        this.quantity = quantity;
        this.price = price;
        this.expectedPrice = expectedPrice;
        this.orderType = orderType;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public String getOrderType() {
        return orderType;
    }

    public double getExpectedPrice() {
        return expectedPrice;
    }

    public long getTimeStamp() {
        return timeStamp;
    }
}
class StockTradingApp {
    private List<Stock> stockList;
    private Queue<StockOrder> buyOrders;
    private Queue<StockOrder> sellOrders;
    private double accountBalance;
    public StockTradingApp() {
        stockList = new ArrayList<>();
        buyOrders = new LinkedList<>();
        sellOrders = new LinkedList<>();
        accountBalance = 50000;
    }

    public void retrieveStockList() throws IOException {
        String url = "https://www.malaysiastock.biz/Listed-Companies.aspx";
        Document doc = Jsoup.connect(url).get();
        Element table = doc.selectFirst("table.marketWatch"); //<table> class name

        Elements rows = table.select("tr:not(:first-child)");
        for (Element row : rows) {
            Elements cells = row.select("td");
            if (cells.size() >= 8) {
                String company = cells.get(0).text();
                String symbol = company.split(" ")[0];
                String[] parts = company.split("\\s*\\(|\\)\\s*");
                String name = parts[1] + " " + parts[2].replace("MAIN", "");
                String marketCap = cells.get(3).text();
                String lastPriceStr = cells.get(4).text();
                String peStr = cells.get(5).text();
                String dyStr = cells.get(6).text();
                String roeStr = cells.get(7).text();

                double lastPrice = 0.0;
                if (!lastPriceStr.equals("-")) {
                    lastPrice = Double.parseDouble(lastPriceStr);
                }

                double pe = 0.0;
                if (!peStr.equals("-")) {
                    pe = Double.parseDouble(peStr);
                }

                double dy = 0.0;
                if (!dyStr.equals("-")) {
                    dy = Double.parseDouble(dyStr);
                }

                double roe = 0.0;
                if (!roeStr.equals("-")) {
                    roe = Double.parseDouble(roeStr);
                }

                Stock stock = new Stock(symbol, name, marketCap, lastPrice, pe, dy, roe);
                stockList.add(stock);
            }
        }
    }


    public void displayStockList() {
        System.out.println("Stock List:");
        System.out.printf("%-10s %-50s %-15s %-10s %-10s %-11s %-10s", "Symbol", "Name", "Market Price", "Last Price", "PE", "DY", "ROE");
        System.out.println();
        for (Stock stock : stockList) {
            System.out.printf("%-10s %-50s %-15s %-10s %-10.2f %-10.2f %5.2f", stock.getSymbol(), stock.getName(), stock.getMarketCap(), stock.getLastPrice(), stock.getPE(), stock.getDY(), stock.getROE());
            System.out.println();
        }
    }

    public void placeOrder(String symbol, int quantity, double price, double expectedPrice, String orderType) {
        // Find the current market price for the symbol
        /*double marketPrice = getCurrentMarketPrice(symbol);

        // Calculate the acceptable price range
        double lowerRange = marketPrice - (0.01 * marketPrice);
        double upperRange = marketPrice + (0.01 * marketPrice);

        // Check if the price falls within the acceptable range
        if (price >= lowerRange && price <= upperRange) {
        // Match orders
        */if (orderType.equalsIgnoreCase("buy")) {
            for (StockOrder sellOrder : sellOrders) {
                if (sellOrder.getSymbol().equalsIgnoreCase(symbol) && Math.abs(sellOrder.getPrice() - price) <= (0.01 * price) && Math.abs(sellOrder.getPrice() - expectedPrice) <= (0.01 * expectedPrice)) {
                    // Execute the trade
                    executeTrade(sellOrder, new StockOrder(symbol, quantity, price, expectedPrice, orderType));
                    return;
                }
            }
            // Add the buy order to the pending list
            buyOrders.add(new StockOrder(symbol, quantity, price, expectedPrice, orderType));
            System.out.println("Buy order placed successfully!");
        } else if (orderType.equalsIgnoreCase("sell")) {
            for (StockOrder buyOrder : buyOrders) {
                if (buyOrder.getSymbol().equalsIgnoreCase(symbol) && Math.abs(buyOrder.getPrice() - price) <= (0.01 * price) && Math.abs(buyOrder.getPrice() - expectedPrice) <= (0.01 * expectedPrice)) {
                    // Execute the trade
                    executeTrade(new StockOrder(symbol, quantity, price, expectedPrice, orderType), buyOrder);
                    return;
                }
            }
            // Add the sell order to the pending list
            sellOrders.add(new StockOrder(symbol, quantity, price, expectedPrice, orderType));
            System.out.println("Sell order placed successfully!");
        } else {
            System.out.println("Invalid order type. Please enter 'buy' or 'sell'.");
        }
        /*} else {
            System.out.println("Price is not within the acceptable range. Current market price: " + marketPrice);
        }
    */}
    private void executeTrade(StockOrder sellOrder, StockOrder buyOrder) {
        // Execute the trade by updating the user's portfolio, removing matched orders, etc.
        System.out.println("Trade executed successfully!");
        System.out.println("Symbol: " + sellOrder.getSymbol());
        System.out.println("Quantity: " + sellOrder.getQuantity());
        System.out.println("Price: " + sellOrder.getPrice());
        System.out.println("Buyer: " + buyOrder.getOrderType());
        System.out.println("Seller: " + sellOrder.getOrderType());
        // Update user's portfolio, remove matched orders, etc.
        // ...
        // Remove the matched orders from the pending lists
        buyOrders.remove(buyOrder);
        sellOrders.remove(sellOrder);
    }
    public boolean isValidSymbol(String symbol) {
        for (Stock stock : stockList) {
            if (stock.getSymbol().equalsIgnoreCase(symbol)) {
                return true;
            }
        }
        return false;
    }
    public boolean isAccountBalanceNonNegative() {
        return accountBalance >= 0;
    }

    // Method to be called before the market closes
    public void checkAccountBalanceBeforeMarketCloses() {
        if (!isAccountBalanceNonNegative()) {
            System.out.println("Your account balance is negative. Please settle your outstanding debts before the market closes.");
            // Optionally, you can cancel any pending orders or take other actions based on your requirements
        } else {
            System.out.println("Your account balance is positive. You are ready for the market to close.");
        }
    }
    public void cancelOrder(String symbol, String orderType) {
        Queue<StockOrder> orders;
        if (orderType.equalsIgnoreCase("buy")) {
            orders = buyOrders;
        } else if (orderType.equalsIgnoreCase("sell")) {
            orders = sellOrders;
        } else {
            System.out.println("Invalid order type. Please enter 'buy' or 'sell'.");
            return;
        }

        StockOrder longestTimeOrder = null;
        StockOrder highestAmountOrder = null;
        int longestTime = Integer.MIN_VALUE;
        double highestAmount = Double.MIN_VALUE;

        Iterator<StockOrder> iterator = orders.iterator();
        while (iterator.hasNext()) {
            StockOrder order = iterator.next();
            if (order.getSymbol().equalsIgnoreCase(symbol)) {
                if (order.getQuantity() > highestAmount) {
                    highestAmountOrder = order;
                    highestAmount = order.getQuantity();
                }
                long orderTime = order.getTimeStamp(); // Replace 'timeStamp' with the actual timestamp field in StockOrder class
                if (orderTime > longestTime) {
                    longestTimeOrder = order;
                    longestTime = (int)orderTime;
                }
            }
        }

        if (longestTimeOrder != null) {
            orders.remove(longestTimeOrder);
            System.out.println("Cancelled order based on longest time: " + longestTimeOrder.getSymbol());
        } else if (highestAmountOrder != null) {
            orders.remove(highestAmountOrder);
            System.out.println("Cancelled order based on highest amount: " + highestAmountOrder.getSymbol());
        } else {
            System.out.println("No matching orders found to cancel.");
        }
    }

    public void displayPendingOrders() {
        System.out.println("Pending Buy Orders:");
        for (StockOrder order : buyOrders) {
            System.out.println(order.getSymbol() + " - Quantity: " + order.getQuantity() + ", Price: " + order.getPrice() + ", Expected Price: " + order.getExpectedPrice());
        }

        System.out.println("Pending Sell Orders:");
        for (StockOrder order : sellOrders) {
            System.out.println(order.getSymbol() + " - Quantity: " + order.getQuantity() + ", Price: " + order.getPrice() + ", Expected Price: " + order.getExpectedPrice());
        }
    }

}
    public class Main {
        public static void main(String[] args) {
                try {
                    StockTradingApp app = new StockTradingApp();
                    app.retrieveStockList();
                    app.displayStockList();

                    Scanner scanner = new Scanner(System.in);

            while (true) {
                // Prompt the user to enter the order details
                System.out.print("Enter the stock symbol: ");
                String symbol = scanner.nextLine();

                // Check if the symbol exists in the stock list
                if (!app.isValidSymbol(symbol)) {
                    System.out.println("Invalid stock symbol. Please enter a valid symbol from the stock list.");
                    return; // Exit the program
                }

                System.out.print("Enter the quantity: ");
                int quantity = scanner.nextInt();
                // Validate the order
                if (quantity < 100) {
                    System.out.println("Minimum order quantity is 100 shares (one lot).");
                    return;
                }


                System.out.print("Enter the price: ");
                double price = scanner.nextDouble();

                System.out.println("Enter the expected price: ");
                double expectedPrice = scanner.nextDouble();

                scanner.nextLine(); // Consume the newline character

                System.out.print("Enter the order type (buy/sell): ");
                String orderType = scanner.nextLine();

                // Place the order
                app.placeOrder(symbol, quantity, price, expectedPrice, orderType);

                // Check account balance before market closes
                app.checkAccountBalanceBeforeMarketCloses();

                app.cancelOrder(symbol, orderType);
                app.displayPendingOrders();
            }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

