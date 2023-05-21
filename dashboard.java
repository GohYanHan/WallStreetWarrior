import java.util.*;

class Stock {
    private String name;
    private String ticker;

    public Stock(String name, String ticker) {
        this.name = name;
        this.ticker = ticker;
    }

    public String getName() {
        return name;
    }

    public String getTicker() {
        return ticker;
    }
}

class Trade {
    private Stock stock;
    private double price;
    private int quantity;
    private Date placementTime;

    public Trade(Stock stock, double price, int quantity) {
        this.stock = stock;
        this.price = price;
        this.quantity = quantity;
        this.placementTime = new Date();
    }

    public Stock getStock() {
        return stock;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public Date getPlacementTime() {
        return placementTime;
    }
}

class UserDashboard {
    private double accountBalance;
    private double pnlOrPoints;
    private List<Trade> openPositions;
    private List<Trade> tradeHistory;

    public UserDashboard() {
        accountBalance = 0.0;
        pnlOrPoints = 0.0;
        openPositions = new ArrayList<>();
        tradeHistory = new ArrayList<>();
    }

    public void setAccountBalance(double accountBalance) {
        this.accountBalance = accountBalance;
    }

    public void setPnlOrPoints(double pnlOrPoints) {
        this.pnlOrPoints = pnlOrPoints;
    }

    public void addOpenPosition(Trade trade) {
        openPositions.add(trade);
    }

    public void addTradeToHistory(Trade trade) {
        tradeHistory.add(trade);
    }

    public void sortTradeHistoryByPrice() {
        Collections.sort(tradeHistory, Comparator.comparingDouble(Trade::getPrice));
    }

    public void sortTradeHistoryByPlacementTime() {
        Collections.sort(tradeHistory, Comparator.comparing(Trade::getPlacementTime));
    }

    public void displayDashboard() {
        System.out.println("Account Balance: $" + accountBalance);
        System.out.println("P&L or Points: " + pnlOrPoints);
        System.out.println("Open Positions: ");
        for (Trade trade : openPositions) {
            System.out.println(trade.getStock().getName() + " (" + trade.getStock().getTicker() + ")");
        }
        System.out.println("Trade History: ");
        for (Trade trade : tradeHistory) {
            System.out.println("Stock: " + trade.getStock().getName() + " (" + trade.getStock().getTicker() + ")");
            System.out.println("Price: $" + trade.getPrice());
            System.out.println("Quantity: " + trade.getQuantity());
            System.out.println("Placement Time: " + trade.getPlacementTime());
            System.out.println("---------------------------------");
        }
    }
}

public class StockTradingApp {
    public static void main(String[] args) {
        StockSearch stockSearch = new StockSearch();

        Portfolio portfolio = new Portfolio();

        // Add some sample stocks
        stockSearch.addStock(new Stock(Portfolio.getHoldings()));


        // Create user dashboard
        UserDashboard user
