import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class TradingEngine {
    private List<Stock> stocks;
    private Map<Stock, List<Order>> buyOrders;
    private Map<Stock, List<Order>> sellOrders;
    private Map<Stock, Integer> lotPool;


    public TradingEngine(List<Stock> stocks) {
        this.stocks = stocks;
        this.buyOrders = new HashMap<>();
        this.sellOrders = new HashMap<>();
        for (Stock stock : stocks) {
            buyOrders.put(stock, new ArrayList<>());
            sellOrders.put(stock, new ArrayList<>());
        }
        this.lotPool = new HashMap<>();
        for (Stock stock : stocks) {
            lotPool.put(stock, 500); // Initialize the lot pool with 500 shares for each stock
        }
    }

    public void executeOrder(Order order, Portfolio portfolio) {
        if (order.getType() == Order.Type.BUY) {
            // Check if it's within the initial trading period (first three days)
            if (isWithinInitialTradingPeriod()) {
                double currentPrice = order.getStock().getPrice();
                double expectedBuyingPrice = order.getExpectedBuyingPrice();

                if (isPriceWithinRange(expectedBuyingPrice, currentPrice, 1)) {
                    buyOrders.get(order.getStock()).add(order);
                    tryExecuteBuyOrders(order.getStock(), portfolio);
                    System.out.println("Order is available");
                } else {
                    System.out.println("The expected buying price is not within the acceptable range.");
                }
            } else {
                // After the initial trading period, enforce the 500-lot rule
                int remainingLotShares = lotPool.getOrDefault(order.getStock(), 0);
                int sharesToBuy = Math.min(order.getShares(), remainingLotShares);

                if (sharesToBuy > 0) {
                    // Deduct the shares from the lot pool
                    lotPool.put(order.getStock(), remainingLotShares - sharesToBuy);

                    // Update the order with the adjusted number of shares
                    order.setShares(sharesToBuy);

                    // Place the order
                    buyOrders.get(order.getStock()).add(order);
                    tryExecuteBuyOrders(order.getStock(), portfolio);
                    System.out.println("Order is available");
                } else {
                    System.out.println("No shares available in the lot pool for the specified stock.");
                }
            }
                }else {
                sellOrders.get(order.getStock()).add(order);
                tryExecuteSellOrders(order.getStock(), portfolio);
            }
        }
    private boolean isWithinInitialTradingPeriod() {
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime endTime = LocalDateTime.of(currentTime.getYear(), currentTime.getMonth(), currentTime.getDayOfMonth(), 0, 0)
                .plusDays(3); // Add three days to the current date
        return currentTime.isBefore(endTime);
    }
    public void replenishLotPoolDaily() { // call each day when wanna start new pool
        // Check if it's the start of a new trading day
        if (isStartOfTradingDay()) {
            // Reset the lot pool shares to 500 for each stock
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
    private boolean isPriceWithinRange(double price, double currentPrice, double rangePercentage) { // Execute trades if the price falls within 1% range
        double range = currentPrice * (rangePercentage / 100);
        double lowerLimit = currentPrice - range;
        double upperLimit = currentPrice + range;
        return price >= lowerLimit && price <= upperLimit;
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

    private void tryExecuteBuyOrders(Stock stock, Portfolio portfolio) {
        List<Order> orders = buyOrders.get(stock);
        double price = stock.getPrice();
        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            if (order.getPrice() >= price) {
                int currentShares = portfolio.getHoldings().getOrDefault(stock, 0);
                double totalPrice = order.getPrice() * order.getShares();
                if (portfolio.getValue() >= totalPrice) {
                    portfolio.addStock(stock, order.getShares());
                    orders.remove(i);
                    i--;
                }
            }
        }
    }
    public void closeMarket(Portfolio portfolio, double accountBalance) {
        if (isWithinTradingHours()) {
            System.out.println("The market is still open. Cannot close the market now.");
            return;
        }

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
    public void displaySuggestedPrice(String stockSymbol, int quantity) {
        Stock stock;

        for (Stock s : stocks) {
            if (s.getSymbol().equalsIgnoreCase(stockSymbol)) {
                stock = s;
                if (stock != null) {
                    double currentPrice = stock.getPrice()*quantity ;
                    double lowerLimit = currentPrice * 0.99; // 1% below the current price
                    double upperLimit = currentPrice * 1.01;

                    System.out.println("Suggested price range for " + stockSymbol + ": " + lowerLimit + " - " + upperLimit);
                } else {
                    System.out.println("Stock with symbol " + stockSymbol + " not found.");
                }
            }
        }
    }

    private void tryExecuteSellOrders(Stock stock, Portfolio portfolio) {
        List<Order> orders = sellOrders.get(stock);
        double price = stock.getPrice();
        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            if (order.getPrice() <= price) {
                int currentShares = portfolio.getHoldings().getOrDefault(stock, 0);
                if (currentShares >= order.getShares()) {
                    portfolio.removeStock(stock, order.getShares());
                    orders.remove(i);
                    i--;
                }
            }
        }
    }

    public void updatePrices(API api) {
        for (Stock stock : stocks) {
            try {
                // Retrieve the stock symbol from the Stock object
                String stockSymbol = stock.getSymbol();

                // Get the current date and time
                Date currentDate = new Date();

                // Set the desired start and end dates for retrieving stock prices
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                String startDate = dateFormat.format(currentDate);
                String endDate = startDate;

                // Set the desired interval for retrieving stock prices (e.g., "daily")
                String interval = "daily";

                // Retrieve the stock prices using the API object
                String jsonResponse = api.getStockPrice(new String[] { stockSymbol }, Long.parseLong(startDate), Long.parseLong(endDate), interval);

                // Process the JSON response and update the stock price
                processStockPrice(jsonResponse, stock);

                // Try executing buy and sell orders for the updated stock
                tryExecuteBuyOrders(stock, new Portfolio());
                tryExecuteSellOrders(stock, new Portfolio());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void processStockPrice(String jsonResponse, Stock stock) {
        try {
            JSONObject json = new JSONObject(jsonResponse);
            JSONObject symbolData = json.getJSONObject(stock.getSymbol());
            JSONObject closeData = symbolData.getJSONObject("Close");

            // Get the latest closing price from the JSON data
            Iterator<String> timestampIterator = closeData.keys();
            String latestTimestamp = null;
            double latestPrice = 0.0;
            while (timestampIterator.hasNext()) {
                String timestamp = timestampIterator.next();
                double price = closeData.getDouble(timestamp);
                if (latestTimestamp == null || timestamp.compareTo(latestTimestamp) > 0) {
                    latestTimestamp = timestamp;
                    latestPrice = price;
                }
            }

            // Update the stock price with the latest price
            stock.setPrice(latestPrice);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
