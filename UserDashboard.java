import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class UserDashboard {
    private User user;

    public UserDashboard(User user) {
        this.user = user;
    }

    public void displayAccountBalance() {
        System.out.println("Account Balance: " + user.getPortfolio().getValue());
    }

    public void displayCurrentPoints() {
        double startingAccountBalance = 50000; // Initial fund
        double pAndL = user.getPortfolio().getValue() - startingAccountBalance;
        double points = (pAndL / startingAccountBalance) * 100;
        System.out.println("Current Points: " + points);
    }

    public void displayOpenPositions() {
        System.out.println("Open Positions:");
        user.getPortfolio().getHoldings().forEach((stock, shares) -> {
            System.out.println(stock.getSymbol() + " - " + stock.getName() + ": " + shares + " shares");
        });
    }

    public void displayTradeHistory() {
        System.out.println("Trade History:");
        List<Order> sortedTradeHistory = user.getPortfolio().getTradeHistory().stream()
                .sorted(Comparator.comparing(Order::getTimestamp))
                .collect(Collectors.toList());

        sortedTradeHistory.forEach(order -> {
            String action = order.getType() == Order.Type.BUY ? "Bought" : "Sold";
            String symbol = order.getStock().getSymbol();
            String name = order.getStock().getName();
            int shares = order.getShares();
            double price = order.getPrice();
            System.out.println(order.getTimestamp() + " - " + action + " " + shares + " shares of " +
                    symbol + " - " + name + " at price " + price);
        });
    }

    public void displayStocksLeft() {
        System.out.println("Stocks Left:");
        user.getPortfolio().getHoldings().forEach((stock, shares) -> {
            System.out.println(stock.getSymbol() + " - " + stock.getName() + ": " + shares + " shares");
        });
    }

    public void sortTradeHistoryByPrice() {
        List<Order> sortedTradeHistory = user.getPortfolio().getTradeHistory().stream()
                .sorted(Comparator.comparingDouble(Order::getPrice))
                .collect(Collectors.toList());
        displaySortedTradeHistory(sortedTradeHistory);
    }

    public void sortTradeHistoryByPlacementTime() {
        List<Order> sortedTradeHistory = user.getPortfolio().getTradeHistory().stream()
                .sorted(Comparator.comparing(Order::getTimestamp))
                .collect(Collectors.toList());
        displaySortedTradeHistory(sortedTradeHistory);
    }

    private void displaySortedTradeHistory(List<Order> sortedTradeHistory) {
        System.out.println("Sorted Trade History:");
        sortedTradeHistory.forEach(order -> {
            String action = order.getType() == Order.Type.BUY ? "Bought" : "Sold";
            String symbol = order.getStock().getSymbol();
            String name = order.getStock().getName();
            int shares = order.getShares();
            double price = order.getPrice();
            System.out.println(order.getTimestamp() + " - " + action + " " + shares + " shares of " +
                    symbol + " - " + name + " at price " + price);
        });
    }

}
