import java.io.IOException;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class TradingEngine {
    private List<Stock> stocks;
    private Map<Stock, List<Order>> buyOrders; // api de stock
    private Map<Stock, List<Order>> sellOrders;
    private Map<Stock, Integer> lotPool;
    API api = new API();
    private Database db;

    public TradingEngine() throws IOException {
        this.stocks = api.extractStocks();
        this.buyOrders = new HashMap<>();
        this.sellOrders = new HashMap<>();
        db = new Database();
        for (Stock stock : stocks) {
            buyOrders.put(stock, new ArrayList<>());
            sellOrders.put(stock, new ArrayList<>());
        }
        this.lotPool = new HashMap<>();
        for (Stock stock : stocks) {
            lotPool.put(stock, 500); // Initialize the lotpool with 500 shares for each stock
        }
    }

    public void executeOrder(Order order, Portfolio portfolio) throws IOException {
        replenishLotPoolDaily();
        if (order.getType() == Order.Type.BUY) {

            double currentPrice = api.getRealTimePrice(order.getStock().getSymbol()) * order.getShares();
            double expectedBuyingPrice = order.getExpectedBuyingPrice();

            if (isPriceWithinRange(expectedBuyingPrice, currentPrice, 1)) {
                if (isWithinInitialTradingPeriod()) {
                    boolean foundMatch = false;

                    for (Order orderDb : db.loadOrders(order.getUserKey(), Order.Type.SELL)) {
                        String symbolDb = orderDb.getStock().getSymbol();
                        if (symbolDb.equalsIgnoreCase(order.getStock().getSymbol())) {
                            tryExecuteBuyOrder(order, portfolio);
                            db.removeOrder(order.getUserKey(), order.getStock().getSymbol(), order.getShares(), Order.Type.SELL);
                            foundMatch = true;
                            break;
                        }
                    }

                    if (!foundMatch) {
                        tryExecuteBuyOrder(order, portfolio);
                        portfolio.addToTradeHistory(order);
                    }
                } else {
                    autoMatching(order, portfolio);
                }
            } else {
                System.out.println("The expected buying price is not within the acceptable range.\nOrder not placed.");
            }
        } else { // order type is sell
            boolean found = false;

            for (Map.Entry<Order, Integer> entry : portfolio.getHoldings().entrySet()) {
                Order orders = entry.getKey();
                String stockSymbol = orders.getStock().getSymbol();

                if (stockSymbol.equalsIgnoreCase(order.getStock().getSymbol())) {
                    double currentPrice = api.getRealTimePrice(order.getStock().getSymbol()) * order.getShares();
                    double expectedBuyingPrice = order.getExpectedSellingPrice();

                    if (isPriceWithinRange(expectedBuyingPrice, currentPrice, 1)) {
                        tryExecuteSellOrder(order, portfolio);
                        portfolio.addToTradeHistory(order);
                    } else {
                        System.out.println("The expected selling price is not within the acceptable range.\nOrder not placed.");
                    }

                    found = true;
                    break;
                }
            }
            if (!found) {
                System.out.println("Stock is not in holdings.");
            }
        }
    }

    public void autoMatching(Order order, Portfolio portfolio) {
        boolean foundMatch = false;

        // Condition 1: Find order in the sell order list
        for (Order orderDb : db.loadOrders(order.getUserKey(), Order.Type.SELL)) {
            String symbolDb = orderDb.getStock().getSymbol();
            if (symbolDb.equalsIgnoreCase(order.getStock().getSymbol())) {
                tryExecuteBuyOrder(order, portfolio);
                db.removeOrder(order.getUserKey(), order.getStock().getSymbol(), order.getShares(), Order.Type.SELL);
                foundMatch = true;
                break;
            }
        }
        if (!foundMatch) {
            // Condition 2: If not in the sell order list, check the lotpool

//            Map<Stock, Integer> lotpoolDb = db.getLotPool(); // in displaySellOrders d
//            for (Map.Entry<Stock, Integer> entry : lotpoolDb.entrySet()) {
//                Stock stockDb = entry.getKey();
//                int sharesDb = entry.getValue();
//                lotPool.put(stockDb, sharesDb);
//            }

            for (Map.Entry<Stock, Integer> entry : lotPool.entrySet()) {
                Stock stock = entry.getKey();
                int shares = entry.getValue();
                String stockSymbol = stock.getSymbol();

                if (stockSymbol.equalsIgnoreCase(order.getStock().getSymbol()) && order.getShares() <= shares) {
                    int updatedShares = lotPool.get(order.getStock()) - order.getShares();
                    if (updatedShares >= 0) {
                        tryExecuteBuyOrder(order, portfolio);
                        db.storeLotPool(order.getStock(), updatedShares);
                        lotPool.remove(order.getStock(), 500); // 500?
                        //foundMatch = true;
                        break;
                    } else {
                        System.out.println("Shares exceeded the amount of shares in the lotpool.");
                        //foundMatch = true;
                        break;
                    }
                } else {
                    // Condition 3: If not found in both conditions 1 and 2, stock is not available
                    System.out.println("Stock not available.");
                }
            }
        }
    }


    private void tryExecuteBuyOrder(Order order, Portfolio portfolio) { //need ziji remove from sellOrders/lotpool after this method is called
        //List<Order> orders = buyOrders.get(order.getStock()); //loop buy order,if enough money, add buy order into portfolio
        double price = order.getExpectedBuyingPrice();
        int shares = order.getShares();

        if (portfolio.getAccBalance() >= price) {
            double temp = portfolio.getAccBalance();
            temp -= price;
            portfolio.setAccBalance(temp);
            portfolio.addValue(order.getExpectedBuyingPrice());
            portfolio.addStock(order, shares);
            portfolio.addToTradeHistory(order);
//          db.removeOrder(order.getUserKey(), order.getStock().getSymbol(), order.getShares(), Order.Type.BUY);
            System.out.println("Buy order executed successfully.");
        } else {
            System.out.println("Not enough money");
        }
    }

    private void tryExecuteSellOrder(Order order, Portfolio portfolio) {
        double price = order.getExpectedSellingPrice();
        int shares = order.getShares();

        double temp = portfolio.getAccBalance();
        temp += price;
        portfolio.setAccBalance(temp);
        portfolio.removeValue(price);
        portfolio.removeStock(order, shares); // remove share num
        db.removeOrder(order.getUserKey(), order.getStock().getSymbol(), order.getShares(), Order.Type.SELL);
        System.out.println("Sell order executed successfully.");
    }

    public boolean executeBuyOrdersMatch(Order order, Portfolio portfolio) throws IOException {
        List<Order> buyOrders = db.loadOrders(order.getUserKey(), Order.Type.BUY);

        double currentPrice = api.getRealTimePrice(order.getStock().getSymbol()) * order.getShares();
        double expectedBuyingPrice = order.getExpectedBuyingPrice();

        for (Order orders : buyOrders) {
            if (isPriceWithinRange(expectedBuyingPrice, currentPrice, 1)) {
                tryExecuteBuyOrder(orders, portfolio);
                return true; // Order executed successfully within price range
            }
        }
        return false; // No matching order found within price range
    }
    public boolean isWithinInitialTradingPeriod() {
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime endTime = LocalDateTime.of(currentTime.getYear(), currentTime.getMonth(), currentTime.getDayOfMonth(), 0, 0)
                .plusDays(3); // Add 3 days to the current date
        return currentTime.isBefore(endTime);
    }

    public void replenishLotPoolDaily() {
        // Check if it's the start of a new trading day
        if (isStartOfTradingDay()) {
            // Reset the lotpool shares to 500 for each stock
            lotPool.clear();
            db.refreshLotPool();
            for (Stock stock : stocks) {
                lotPool.put(stock, 500);
            }
            System.out.println("Lot pool replenished for the day.");
        }
    }

    public boolean isStartOfTradingDay() {
        LocalTime marketOpenTime = LocalTime.of(9, 0); // Adjust the market open time according to your needs
        LocalTime currentTime = LocalTime.now();
        return currentTime.equals(marketOpenTime);
    }

    private boolean isPriceWithinRange(double price, double currentPrice, double rangePercentage) {
        double range = currentPrice * (rangePercentage / 100);
        double lowerLimit = currentPrice - range;
        double upperLimit = currentPrice + range;

        // Format values to 2 decimal points
        DecimalFormat decimalFormat = new DecimalFormat("#0.00");
        double formattedLowerLimit = Double.parseDouble(decimalFormat.format(lowerLimit));
        double formattedUpperLimit = Double.parseDouble(decimalFormat.format(upperLimit));
        double formattedPrice = Double.parseDouble(decimalFormat.format(price));

        return formattedPrice >= formattedLowerLimit && formattedPrice <= formattedUpperLimit;
    }

    public boolean isWithinTradingHours() { // Check trading hours
        // Get the current day and time
        LocalDateTime currentTime = LocalDateTime.now();
        DayOfWeek currentDay = currentTime.getDayOfWeek();
        LocalTime currentTimeOfDay = currentTime.toLocalTime();

        // Check if it's a weekday (Monday to Friday) and within regular market hours
        if (currentDay != DayOfWeek.SATURDAY && currentDay != DayOfWeek.SUNDAY) {
            LocalTime marketOpenTime1 = LocalTime.of(9, 0);
            LocalTime marketCloseTime1 = LocalTime.of(12, 30);
            LocalTime marketOpenTime2 = LocalTime.of(14, 30);
            LocalTime marketCloseTime2 = LocalTime.of(17, 0);
            return (currentTimeOfDay.isAfter(marketOpenTime1) && currentTimeOfDay.isBefore(marketCloseTime1))
                    || (currentTimeOfDay.isAfter(marketOpenTime2) && currentTimeOfDay.isBefore(marketCloseTime2));
        }
        return false;
    }

    public void cancelBuyOrder(List<Order> orders, Portfolio portfolio) {
        Database db = new Database();
        if (!orders.isEmpty()) {
            displayBuyOrders(orders);
            System.out.println("Choose the cancel option: ");
            System.out.println("1. Cancel based on longest time");
            System.out.println("2. Cancel based on highest price");

            Scanner scanner = new Scanner(System.in);
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            switch (choice) {
                case 1:
                    Order orderToCancelByTime = getOrderWithLongestTime(orders);
                    db.removeOrder(orderToCancelByTime.getUserKey(), orderToCancelByTime.getStock().getSymbol(), orderToCancelByTime.getShares(), Order.Type.BUY);
                    //portfolio.removeStock(orderToCancelByTime, orderToCancelByTime.getShares()); // no need bcs not in holdings
                    System.out.println("Buy order canceled based on longest time successfully.");
                    break;
                case 2:
                    Order orderToCancelByPrice = getOrderWithHighestPrice(orders);
                    db.removeOrder(orderToCancelByPrice.getUserKey(), orderToCancelByPrice.getStock().getSymbol(), orderToCancelByPrice.getShares(), Order.Type.BUY);
                    //portfolio.removeStock(orderToCancelByPrice, orderToCancelByPrice.getShares());
                    System.out.println("Buy order canceled based on highest price successfully.");
                    break;
                default:
                    System.out.println("Invalid choice. Buy order cancellation canceled.");
                    break;
            }
        } else {
            System.out.println("No buy orders available.");
        }
    }

    private Order getOrderWithLongestTime(List<Order> orders) { // no return longest time
        Order orderWithLongestTime = null;
        LocalDateTime longestTime = LocalDateTime.MAX;

        for (Order order : orders) {
            LocalDateTime orderTime = order.getTimestamp();
            if (orderTime.compareTo(longestTime) < 0) {
                longestTime = orderTime;
                orderWithLongestTime = order;
            }
        }

        return orderWithLongestTime;
    }

    private Order getOrderWithHighestPrice(List<Order> orders) {
        Order orderWithHighestPrice = null;
        double highestPrice = Double.MIN_VALUE;

        for (Order order : orders) {
            double orderPrice = order.getExpectedBuyingPrice();
            if (orderPrice > highestPrice) {
                highestPrice = orderPrice;
                orderWithHighestPrice = order;
            }
        }

        return orderWithHighestPrice;
    }


    public void closeMarket(double accountBalance) { // argument = portfolio.getAccBalance()
        if (isWithinTradingHours()) {
            System.out.println("The market is still open. Cannot close the market now.");
            return;
        }else {

            // Check if the account balance is non-negative
            if (accountBalance >= 0) {
                System.out.println("Market closed successfully.");
                // Perform any necessary actions to finalize the market closing

                // Reset the buy and sell orders
                buyOrders.clear();
                sellOrders.clear();
                for (Stock stock : stocks) {
                    buyOrders.put(stock, new ArrayList<>());
                    sellOrders.put(stock, new ArrayList<>());
                }
            } else {
                System.out.println("Cannot close the market. Account balance is negative.");
            }
        }
    }

    public void displaySuggestedPrice(String stockSymbol, int quantity) throws IOException {
        Stock stock = null;

        for (Stock s : stocks) {
            if (s.getSymbol().equalsIgnoreCase(stockSymbol)) {
                stock = s;
                break;
            }
        }
        if (stock != null) {
            double currentPrice = api.getRealTimePrice(stock.getSymbol()) * quantity;
            double lowerLimit = currentPrice * 0.99; // 1% below the current price
            double upperLimit = currentPrice * 1.01;

            DecimalFormat decimalFormat = new DecimalFormat("#0.00");
            String formattedLowerLimit = decimalFormat.format(lowerLimit);
            String formattedUpperLimit = decimalFormat.format(upperLimit);

            System.out.println("Suggested price range for " + stockSymbol + ": " + formattedLowerLimit + " - " + formattedUpperLimit);
        } else {
            System.out.println("Stock with symbol " + stockSymbol + " not found.");
        }
    }

    public void displayLotpoolSellOrders(Map<Stock, Integer> lotpoolDb, List<Order> sellOrders) { // sellOrderList
        lotpoolDb = db.getLotPool();
        for (Map.Entry<Stock, Integer> entry : lotpoolDb.entrySet()) {
            Stock stockDb = entry.getKey();
            int sharesDb = entry.getValue();
            lotPool.put(stockDb, sharesDb);
        }

        System.out.println("Orders available: ");
        System.out.printf("%-15s %-10s\n", "Stock", "Shares");

        for (Map.Entry<Stock, Integer> entry : lotPool.entrySet()) {
            Stock stock = entry.getKey();
            Integer value = entry.getValue();
            System.out.printf("%-20s %-10s%n", stock.getSymbol(), value);
        }
//        for (Map.Entry<Stock, Integer> entry : lotpoolDb.entrySet()) {
//            Stock stock = entry.getKey();
//            Integer value = entry.getValue();
//            System.out.printf("%-20s %-10s%n", stock.getSymbol(), value);
//        }
        System.out.println("Orders in sell order list: ");
        for (Order order : sellOrders) {
            System.out.printf("%-20s %-10s%n", order.getStock().getSymbol(), order.getShares());
        }
    }

    private void displayBuyOrders(List<Order> orders) {
        for (Order order : orders) {
            System.out.println("Stock: " + order.getStock().getSymbol());
            System.out.println("Price: " + order.getExpectedBuyingPrice());
            System.out.println("TimeStamp: " + order.getTimestamp());
            System.out.println("-".repeat(30));
        }
    }

}