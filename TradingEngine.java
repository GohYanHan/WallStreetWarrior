import java.io.IOException;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TradingEngine {
    private List<Stock> stocks;
    private Map<Stock, List<Order>> buyOrders; // api de stock
    private Map<Stock, List<Order>> sellOrders;
    private Map<Stock, Integer> lotPool;
    API api = new API();

    private final Database db = new Database();
    Notification notification = new Notification();
    FraudDetection fd = new FraudDetection();

    public TradingEngine() throws IOException {
        this.stocks = api.extractStocks();
        this.buyOrders = new HashMap<>();
        this.sellOrders = new HashMap<>();
        for (Stock stock : stocks) {
            buyOrders.put(stock, new ArrayList<>());
            sellOrders.put(stock, new ArrayList<>());
        }
        this.lotPool = new HashMap<>();
        for (Stock stock : stocks) {
            lotPool.put(stock, 50000); // Initialize the lotpool with 50000 shares for each stock
        }
    }

    public boolean executeOrder(Order order, Portfolio portfolio) throws IOException { // for execute immediately orders
        replenishLotPoolDaily();
        if (order.getType() == Order.Type.BUY) {
            if (!findMatch(order, portfolio)) System.out.println("Buy unsuccessful");
        } else { // order type is sell
            boolean found = false;

            for (Map.Entry<Order, Integer> entry : portfolio.getHoldings().entrySet()) { // sell stock in holdings only
                Order orders = entry.getKey();
                String stockSymbol = orders.getStock().getSymbol();
                Integer holdingShares = entry.getValue();

                if (stockSymbol.equalsIgnoreCase(order.getStock().getSymbol()) && order.getShares() <= holdingShares) {
                    double currentPrice = api.getRealTimePrice(order.getStock().getSymbol()) * order.getShares();
                    double expectedBuyingPrice = order.getExpectedSellingPrice();

                    if (isPriceWithinRange(expectedBuyingPrice, currentPrice, 1)) {
                        System.out.println("Sell order placed successfully.");
                        notification.sendNotification(4, order.getUser().getEmail(), order);

                    } else {
                        System.out.println("The expected selling price is not within the acceptable range.\nOrder not placed.");
                        return false;
                    }
                    found = true;
                    break;

                }
            }
            if (!found) {
                System.out.println("Stock cannot be sold.");
                return false;
            }
        }
        return true;
    }


    private boolean findMatch(Order order, Portfolio portfolio) throws IOException {
        double currentPrice = api.getRealTimePrice(order.getStock().getSymbol()) * order.getShares();

        // Condition 1: Find order in the sell order list
        for (Order orderDb : db.loadOrders(order.getUserKey(), Order.Type.SELL)) {
            String symbolDb = orderDb.getStock().getSymbol();
            int shareDb = orderDb.getShares();
            double priceDb = orderDb.getExpectedSellingPrice();

            if (symbolDb.equalsIgnoreCase(order.getStock().getSymbol()) && shareDb == order.getShares() && priceDb == order.getExpectedBuyingPrice()) {
                tryExecuteBuyOrder(order, portfolio);
                tryExecuteSellOrder(orderDb);
                //System.out.println("Sell order executed successfully.");
                db.removeOrder(orderDb.getUserKey(), orderDb); // Remove from sell order list
                return true;
            }
        }

        // Condition 2: If not in the sell order list, check the lotpool
        for (Map.Entry<Stock, Integer> entry : lotPool.entrySet()) {
            Stock stock = entry.getKey();
            int shares = entry.getValue();
            String stockSymbol = stock.getSymbol();

            if (isPriceWithinRange(order.getExpectedBuyingPrice(), currentPrice, 1)) {
                if (!isWithinInitialTradingPeriod() && stockSymbol.equalsIgnoreCase(order.getStock().getSymbol()) && order.getShares() <= shares) {
                    int updatedShares = shares - order.getShares();
                    if (updatedShares >= 0) {
                        tryExecuteBuyOrder(order, portfolio);

                        for (Map.Entry<Stock, Integer> entries : db.getLotPool().entrySet()) {
                            Stock stockDb = entries.getKey();
                            String stockSymbolDb = stockDb.getSymbol();
                            if (stockSymbolDb.equalsIgnoreCase(order.getStock().getSymbol())) {
                                db.updateLotPool(order.getStock(), updatedShares);
                            } else {
                                db.storeLotPool(order.getStock(), updatedShares);
                                lotPool.remove(order.getStock(), 50000); // Store in database, remove from lotpool
                            }
                        }
                        return true;
                    }
                } else if (isWithinInitialTradingPeriod() && stockSymbol.equalsIgnoreCase(order.getStock().getSymbol())) { // First 3 days buy whatever in lotpool
                    tryExecuteBuyOrder(order, portfolio);
                    return true;
                }
            }
        }
        return false; // No match found
    }

    private void tryExecuteBuyOrder(Order order, Portfolio portfolio) { // enough money jiu buy
        double price = order.getExpectedBuyingPrice();
        int shares = order.getShares();

        if (portfolio.getAccBalance() >= price) {
            double temp = portfolio.getAccBalance();
            temp -= price;
            portfolio.setAccBalance(temp);
            portfolio.addStock(order, shares);
            portfolio.addValue(order.getExpectedBuyingPrice());
            portfolio.addToTradeHistory(order);
            System.out.println("Buy order executed successfully.");
            notification.sendNotification(3, order.getUser().getEmail(), order);
            fd.sendNotification();
        } else {
            System.out.println("Not enough money");
        }
    }

    private void tryExecuteSellOrder(Order order) {
        double price = order.getExpectedSellingPrice();
        int shares = order.getShares();
        User user = db.loadUserByKey(order.getUserKey());
        Portfolio portfolio = user.getPortfolio();

        double temp = portfolio.getAccBalance();
        temp += price;
        db.updateUserBalance(user.getKey(), Math.round(temp * 100.0) / 100.0);
        portfolio.removeStock(order, shares); // remove share num
        portfolio.removeValue(price);
        portfolio.addToTradeHistory(order);
        UserDashboard dashboard = new UserDashboard(user);
        dashboard.calculatePLPoints();
        notification.sendNotification(5, user.getEmail(), order);
        fd.sendNotification();

    }

    public void runAutoMatchingInBackground(List<Order> orders, Portfolio portfolio) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            try {
                autoMatching(orders, portfolio);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        executor.shutdown();
    }

    public boolean autoMatching(List<Order> orders, Portfolio portfolio) throws IOException {
        boolean allBuyOrdersMatched = false;

        while (!allBuyOrdersMatched) { //within trading hours
            for (Order order : orders) {
                if (findMatch(order, portfolio)) {
                    db.removeOrder(order.getUserKey(), order); // Remove from buy order list
                    System.out.println("Buy order removed from buy order list.");
                }
            }

            // Check if all buy orders are matched
            allBuyOrdersMatched = true;

            if (orders.isEmpty()) {
                allBuyOrdersMatched = false;
                break;
            }
        }

        return allBuyOrdersMatched;
    }


    public boolean isWithinInitialTradingPeriod() {
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime endTime = LocalDateTime.of(currentTime.getYear(), currentTime.getMonth(), currentTime.getDayOfMonth(), 0, 0)
//                .plusDays(3); // Add 3 days to the current date
                .plusMinutes(1);
        return currentTime.isBefore(endTime);
    }

    public void replenishLotPoolDaily() {
        // Check if it's the start of a new trading day
        if (isStartOfTradingDay()) {
            // Reset the lotpool shares to 500 for each stock
            lotPool.clear();
            db.refreshLotPool();
            for (Stock stock : stocks) {
                lotPool.put(stock, 50000);
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

    public void cancelOrder(List<Order> orders, Order.Type type) {
        Database db = new Database();
        if (!orders.isEmpty()) {
            displayOrders(orders, type);
            System.out.println("Choose the cancel option: ");
            System.out.println("1. Cancel based on longest time");
            System.out.println("2. Cancel based on highest price");

            Scanner scanner = new Scanner(System.in);
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            switch (choice) {
                case 1:
                    Order orderToCancelByTime = getOrderWithLongestTime(orders);
                    db.removeOrder(orderToCancelByTime.getUserKey(), orderToCancelByTime);
                    //portfolio.removeStock(orderToCancelByTime, orderToCancelByTime.getShares()); // no need bcs not in holdings
                    System.out.println("Order canceled based on longest time successfully.");
                    break;
                case 2:
                    Order orderToCancelByPrice = getOrderWithHighestPrice(orders);
                    db.removeOrder(orderToCancelByPrice.getUserKey(), orderToCancelByPrice);
                    //portfolio.removeStock(orderToCancelByPrice, orderToCancelByPrice.getShares());
                    System.out.println("Order canceled based on highest price successfully.");
                    break;
                default:
                    System.out.println("Invalid choice. Order cancellation canceled.");
                    break;
            }
        } else {
            System.out.println("No Orders available.");
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


    public void closeMarket(User user) { // argument = portfolio.getAccBalance()
        if (user.getBalance() >= 25000) {
            db.disqualifyUser(user.getEmail());
            System.out.println("You have been disqualified. Your account balance is exceed 50% of initial balance.");
        }
        // Check if the account balance is non-negative
        if (user.getBalance() >= 0) {
            // Perform any necessary actions to finalize the market closing

            // Reset the buy and sell orders values in api
            buyOrders.clear();
            sellOrders.clear();
            for (Stock stock : stocks) {
                buyOrders.put(stock, new ArrayList<>());
                sellOrders.put(stock, new ArrayList<>());
            }
        } else {
            System.out.println("User balance is negative.");
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

    public void displayLotpoolSellOrders(List<Order> sellOrders) { // sellOrderList
        Map<Stock, Integer> lotpoolDb = db.getLotPool();
        for (Map.Entry<Stock, Integer> entry : lotpoolDb.entrySet()) {
            Stock stockDb = entry.getKey();
            int sharesDb = entry.getValue();
            lotPool.put(stockDb, sharesDb);
        }
        System.out.println("=".repeat(47));
        System.out.println("Orders available: ");
        System.out.println("=".repeat(47));
        System.out.printf("%-1s %-20s %-1s %-20s %-1s%n", "|", "Stock", "|", "Shares", "|");
        System.out.println("-".repeat(47));

        Map<Stock, Integer> lotPools = new TreeMap<>(Comparator.comparing(Stock::getSymbol));
        lotPools.putAll(lotPool);

        if (!isWithinInitialTradingPeriod()) {
            System.out.println("Orders available: ");
            System.out.printf("%-20s %-10s\n", "Stock", "Shares");

            for (Map.Entry<Stock, Integer> entry : lotPools.entrySet()) {
                Stock stock = entry.getKey();
                Integer value = entry.getValue();
                System.out.printf("%-1s %-20s %-1s %-20s %-10s%n", "|", stock.getSymbol(), "|", value, "|");
            }
        } else {
            System.out.println("Orders available: ");
            System.out.printf("%-20s %-10s\n", "Stock", "Shares");

            for (Map.Entry<Stock, Integer> entry : lotPools.entrySet()) {
                Stock stock = entry.getKey();
                Integer value = entry.getValue();
                System.out.printf("%-1s %-20s %-1s %-20s %-1s%n", "|", stock.getSymbol(), "|", "unlimited", "|");
            }
        }
        System.out.println("=".repeat(47));
        System.out.println("Orders in sell order list: ");
        System.out.println("=".repeat(47));
        System.out.printf("%-1s %-12s %-1s %-12s %-1s %-13s %-1s%n", "|", "Stock", "|", "Shares", "|", "Selling Price", "|");
        System.out.println("-".repeat(47));

        if (sellOrders.isEmpty()) {
            System.out.printf("%-1s %-12s %-1s %-12s %-1s %-13s %-1s%n", "|", "-", "|", "-", "|", "-", "|");
        } else {
            for (Order order : sellOrders) {
                System.out.printf("%-1s %-12s %-1s %-12s %-1s %-13s %-1s%n", "|", order.getStock().getSymbol(), "|", order.getShares(), "|", order.getExpectedSellingPrice(), "|");
            }
        }
        System.out.println("=".repeat(47));
    }

    private void displayOrders(List<Order> orders, Order.Type type) {
        if (type == Order.Type.BUY) {
            for (Order order : orders) {
                System.out.println("Stock: " + order.getStock().getSymbol());
                System.out.println("Price: " + order.getExpectedBuyingPrice());
                System.out.println("TimeStamp: " + order.getTimestamp());
                System.out.println("-".repeat(30));
            }
        } else if (type == Order.Type.SELL) {
            for (Order order : orders) {
                System.out.println("Stock: " + order.getStock().getSymbol());
                System.out.println("Price: " + order.getExpectedSellingPrice());
                System.out.println("TimeStamp: " + order.getTimestamp());
                System.out.println("-".repeat(30));
            }
        }
    }
}
