import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Portfolio {
    private Map<Order, Integer> holdings;
    private double accBalance = 50000;

    public Portfolio() {
        holdings = new HashMap<>();
    }

    public void addStock(Order order, int shares) {
        int currentShares = holdings.getOrDefault(order, 0);
        holdings.put(order, currentShares + shares);
    }

    public void removeStock(Order order, int shares) {
        int currentShares = holdings.getOrDefault(order, 0);
        if (currentShares >= shares) {
            holdings.put(order, currentShares - shares);
        }else{
            System.out.println("Current shares in holding not enough to be sold");
        }
    }

    public boolean getHoldings(String symbol){
        return holdings.containsKey(symbol);
    }

    public void setAccBalance(double accBalance) {
        this.accBalance = accBalance;
    }

    public double getAccBalance(){
        return accBalance;
    }

    public void displayHoldings() {
        System.out.println("Holdings:");

        if (holdings.isEmpty()) {
            System.out.println("No holdings");
        } else {
            for (Map.Entry<Order, Integer> entry : holdings.entrySet()) {
                Order order = entry.getKey();
                int shares = entry.getValue();

                System.out.println("Stock: " + order.getStock().getSymbol());
                System.out.println("Shares: " + shares);
                System.out.println("Value: " + (order.getExpectedBuyingPrice()));
                System.out.println("-".repeat(30));
            }
        }
    }

    public void displayBuyOrders(){
        System.out.println("Orders to sell: ");

        for (Map.Entry<Order, Integer> entry : holdings.entrySet()) {
            Order order = entry.getKey();
            int shares = entry.getValue();

            System.out.println("Stock: " + order.getStock().getSymbol());
            System.out.println("Shares: " + shares);
            System.out.println("-".repeat(30));
        }
    }
    public boolean containsStockSymbol(String symbol) {
        for (Map.Entry<Order, Integer> entry : holdings.entrySet()) {
            Order order = entry.getKey();
            String stockSymbol = order.getStock().getSymbol();

            if (stockSymbol.equals(symbol)) {
                return true; // Symbol found in holdings
            }
        }
        return false; // Symbol not found in holdings
    }
    public Stock findStockBySymbol(String symbol) {
        for (Map.Entry<Order, Integer> entry : holdings.entrySet()) {
            Order order = entry.getKey();

            if (order.getStock().getSymbol().equalsIgnoreCase(symbol)) {
                return order.getStock();
            }
        }
        return null;
    }
}