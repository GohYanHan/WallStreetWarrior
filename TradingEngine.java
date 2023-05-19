import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TradingEngine {
    private List<Stock> stocks;
    private Map<Stock, List<Order>> buyOrders;
    private Map<Stock, List<Order>> sellOrders;

    public TradingEngine(List<Stock> stocks) {
        this.stocks = stocks;
        this.buyOrders = new HashMap<>();
        this.sellOrders = new HashMap<>();
        for (Stock stock : stocks) {
            buyOrders.put(stock, new ArrayList<>());
            sellOrders.put(stock, new ArrayList<>());
        }
    }

    public void executeOrder(Order order, Portfolio portfolio) {
            if (order.getType() == Order.Type.BUY) {
                double currentPrice = order.getStock().getPrice();
                double expectedBuyingPrice = order.getExpectedBuyingPrice();

                if (isPriceWithinRange(expectedBuyingPrice, currentPrice, 1)) {
                    buyOrders.get(order.getStock()).add(order);
                    tryExecuteBuyOrders(order.getStock(), portfolio);
                    System.out.println("Order placed successfully!");
                } else {
                    System.out.println("The expected buying price is not within the acceptable range.");
                }
            } else {
                sellOrders.get(order.getStock()).add(order);
                tryExecuteSellOrders(order.getStock(), portfolio);
            }
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
    public void displaySuggestedPrice(String stockSymbol) {
        Stock stock = null;

        for (Stock s : stocks) {
            if (s.getSymbol().equals(stockSymbol)) {
                stock = s;
                if (stock != null) {
                    double currentPrice = stock.getPrice();
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

    public void updatePrices() {
        for (Stock stock : stocks) {
            // Update the stock price based on some market data source
            double newPrice = 1; // Get the new price from some market data source
            stock.setPrice(newPrice);
            tryExecuteBuyOrders(stock, new Portfolio());
            tryExecuteSellOrders(stock, new Portfolio());
        }
    }
}
