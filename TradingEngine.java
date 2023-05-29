import java.io.IOException;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class TradingEngine {
    private List<Stock> stocks;
    private Map<Stock, List<Order>> buyOrders;
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
            if (isWithinInitialTradingPeriod()) {
                double currentPrice = api.getRealTimePrice(order.getStock().getSymbol()) * order.getShares();
                double expectedBuyingPrice = order.getExpectedBuyingPrice();

                if (isPriceWithinRange(expectedBuyingPrice, currentPrice, 1)) {
                    List<Order> buyOrdersList = buyOrders.computeIfAbsent(order.getStock(), k -> new ArrayList<>());
                    buyOrdersList.add(order);
                    tryExecuteBuyOrder(order, portfolio);
                    lotPool.remove(order.getStock(), order.getShares());
                } else {
                    System.out.println("The expected buying price is not within the acceptable range.\nOrder not placed.");
                    return;
                }
            } else {
                autoMatching(portfolio);
            }
        } else {
            // if sell order is in buy order list
            if (portfolio.containsStockSymbol(order.getStock().getSymbol())) {
                double currentPrice = api.getRealTimePrice(order.getStock().getSymbol()) * order.getShares();
                double expectedBuyingPrice = order.getExpectedSellingPrice();

                if (isPriceWithinRange(expectedBuyingPrice, currentPrice, 1)) {
                    List<Order> sellOrdersList = sellOrders.computeIfAbsent(order.getStock(), k -> new ArrayList<>());
                    sellOrdersList.add(order);
                    tryExecuteSellOrder(order, portfolio);
                } else {
                    System.out.println("The expected buying price is not within the acceptable range.\nOrder not placed.");
                    return;
                }
            }else{
                System.out.println("Stock is not in list");
            }
        }
    }


    private boolean isWithinInitialTradingPeriod() {
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
            for (Stock stock : stocks) {
                lotPool.put(stock, 500);
            }
            System.out.println("Lot pool replenished for the day.");
        }
    }

    private boolean isStartOfTradingDay() {
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

    private void tryExecuteBuyOrder(Order order, Portfolio portfolio) { //need ziji remove from sellOrders/lotpool after this method is called
        List<Order> orders = buyOrders.get(order.getStock()); //loop buy order,if enough money, add buy order into portfolio
        double price = order.getExpectedBuyingPrice();
        int shares = order.getShares();

        if (portfolio.getAccBalance() >= price) {
            double temp = portfolio.getAccBalance();
            temp -= price;
            portfolio.setAccBalance(temp);
        //    portfolio.addValue(order.getExpectedBuyingPrice());
            portfolio.addStock(order, shares);
            orders.remove(order);
        } else {
            System.out.println("Not enough money");
        }
    }

    public void cancelBuyOrder(List<Order> orders) {
        if (!orders.isEmpty()) {
            System.out.println("Choose the cancel option: ");
            System.out.println("1. Cancel based on longest time");
            System.out.println("2. Cancel based on highest price");

            Scanner scanner = new Scanner(System.in);
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            switch (choice) {
                case 1:
                    Order orderToCancelByTime = getOrderWithLongestTime(orders);
                    orders.remove(orderToCancelByTime);
                    System.out.println("Buy order canceled based on longest time successfully.");
                    break;
                case 2:
                    Order orderToCancelByPrice = getOrderWithHighestPrice(orders);
                    orders.remove(orderToCancelByPrice);
                    System.out.println("Buy order canceled based on highest price successfully.");
                    break;
                default:
                    System.out.println("Invalid choice. Buy order cancellation canceled.");
                    break;
            }
        } else {
            System.out.println("No buy orders available for the specified stock.");
        }
    }

    private Order getOrderWithLongestTime(List<Order> orders) {
        Order orderWithLongestTime = null;
        LocalDateTime longestTime = LocalDateTime.MIN;

        for (Order order : orders) {
            LocalDateTime orderTime = order.getTimestamp();
            if (orderTime.compareTo(longestTime) > 0) {
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
    public void displaySellOrders() { // prob
        System.out.println("Sell Orders:");

        for (Stock stock : stocks) {
            List<Order> sellOrderList = sellOrders.get(stock);
            if (sellOrderList != null) {
                System.out.println("Stock: " + stock.getSymbol());
                for (Order order : sellOrderList) {
                    System.out.println("  Price: " + order.getExpectedSellingPrice());
                    System.out.println("  Shares: " + order.getShares());
                    System.out.println("  Timestamp: " + order.getTimestamp());
                    System.out.println("-".repeat(30));
                }
            }
        }
    }



    public Stock getStockBySymbol(String symbol) {
        for (Stock stock : stocks) {
            if (stock.getSymbol().equalsIgnoreCase(symbol)) {
                return stock;
            }
        }
        return null; // Stock with the specified symbol not found
    }

    private void tryExecuteSellOrder(Order order, Portfolio portfolio) {
        List<Order> orders = sellOrders.get(order.getStock());
        double price = order.getExpectedSellingPrice();
        int shares = order.getShares();
        double totalPrice = price * shares;

        double temp = portfolio.getAccBalance();
        temp += totalPrice;
        portfolio.setAccBalance(temp);
        portfolio.removeStock(order, shares); // remove share num
        orders.remove(order);
    }
    public void autoMatching(Portfolio portfolio) {
        for (Stock stock : stocks) {
            List<Order> buyOrderList = buyOrders.get(stock);
            List<Order> sellOrderList = sellOrders.get(stock);

            // Iterate over the buy orders
            for (Order buyOrder : buyOrderList) {
                // Check if the buy order symbol and price match with any sell order
                for (Order sellOrder : sellOrderList) {
                    if (isInSellOrder(buyOrder, sellOrder)) {
                        tryExecuteBuyOrder(buyOrder, portfolio);
                        sellOrders.remove(stock);
                    }
                    // Check if the buy order symbol and price match with the lot pool
                    else if ((!isInSellOrder(buyOrder, sellOrder)) && lotPool.containsKey(buyOrder.getStock()) && lotPool.containsValue(buyOrder.getShares())) {
                        tryExecuteBuyOrder(buyOrder, portfolio);
                        lotPool.remove(buyOrder.getStock(), buyOrder.getShares()); // deduct shares
                    } else {
                        System.out.println("Stock is added into pending order");
                    }
                }
            }
        }
    }
    private boolean isInSellOrder(Order buyOrder, Order sellOrder){
        return buyOrder.getStock().getSymbol().equals(sellOrder.getStock().getSymbol()) && buyOrder.getExpectedBuyingPrice() == sellOrder.getExpectedSellingPrice();
    }

    public Map<Stock, List<Order>> getBuyOrders() {
        return buyOrders;
    }

}
