import java.util.Comparator;
import java.util.List;

public class UserDashboard {
    private User user;

    public UserDashboard(User user) {
        this.user = user;
    }

    public void displayAccountBalance() {
        double accountBalance = user.getBalance();
        System.out.println("Account Balance: $" + accountBalance);
    }

    public void displayCurrentPoints() {
        double startingBalance = 50000.0; // Assuming a fixed starting balance
        double pAndL = user.getBalance() - startingBalance;
        double points = (pAndL / startingBalance) * 100;
        System.out.println("Current Points: " + points);
    }

    public void displayOpenPositions() {
        System.out.println("Open Positions:");
        Portfolio portfolio = user.getPortfolio();
        portfolio.displayHoldings();
    }

    public void displayTradeHistory() {
        System.out.println("Trade History:");
        List<Order> tradeHistory = user.getPortfolio().getTradeHistory();
        tradeHistory.sort(Comparator.comparing(Order::getPrice).thenComparing(Order::getTimestamp));

        for (Order order : tradeHistory) {
            System.out.println("Stock: " + order.getStock().getSymbol());
            System.out.println("Type: " + order.getType());
            System.out.println("Shares: " + order.getShares());
            System.out.println("Price: $" + order.getPrice());
            System.out.println("Timestamp: " + order.getTimestamp());
            System.out.println("-".repeat(30));
        }
    }

    public void displayStocksLeft() {
        System.out.println("Stocks Left:");
        Portfolio portfolio = user.getPortfolio();
        List<Order> tradeHistory = portfolio.getTradeHistory();

        for (Order order : tradeHistory) {
            if (!portfolio.containsStockSymbol(order.getSymbol())) {
                System.out.println("Stock: " + order.getSymbol());
                System.out.println("Name: " + order.getStock().getName());
                System.out.println("Price: $" + order.getPrice());
                System.out.println("-".repeat(30));
            }
        }
    }

    public void sortTradeHistoryByPrice() {
        List<Order> tradeHistory = user.getPortfolio().getTradeHistory();
        tradeHistory.sort(Comparator.comparing(Order::getPrice));
    }

    public void sortTradeHistoryByPlacementTime() {
        List<Order> tradeHistory = user.getPortfolio().getTradeHistory();
        tradeHistory.sort(Comparator.comparing(Order::getTimestamp));
    }

}