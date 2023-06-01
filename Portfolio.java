import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Portfolio {
    private Map<Order, Integer> holdings;
    private double accBalance = 50000;
    private double value = 0;

    public Portfolio() {
        holdings = new HashMap<>();
    }

    public void addStock(Order order, int buyShares) {
        boolean found = false;

        for (Map.Entry<Order, Integer> entry : holdings.entrySet()) {
            Order existingOrder = entry.getKey();
            int shares = entry.getValue();

            if (existingOrder.getStock().getSymbol().equalsIgnoreCase(order.getStock().getSymbol())) {
                int updatedShares = shares + buyShares;
                holdings.replace(existingOrder, shares, updatedShares);
                value += order.getExpectedSellingPrice();
                found = true;
                break;
            }
        }
        if (!found) {
            holdings.put(order, buyShares);
        }
        System.out.println("Buy order executed successfully.");
    }


    public void removeStock(Order order, int soldShares) {
        boolean found = false;

        for (Map.Entry<Order, Integer> entry : holdings.entrySet()) {
            Order existingOrder = entry.getKey();
            int shares = entry.getValue();

            if (existingOrder.getStock().getSymbol().equalsIgnoreCase(order.getStock().getSymbol())) {
                if (shares >= soldShares) {
                    int updatedShares = shares - soldShares;
                    if (updatedShares == 0) {
                        holdings.remove(existingOrder);
                    } else {
                        holdings.replace(existingOrder, shares, updatedShares);
                    }
                    value -= order.getExpectedBuyingPrice() * soldShares;
                    found = true;
                    System.out.println("Sell order executed successfully.");
                } else {
                    System.out.println("Not enough shares to sell.");
                }
                break;
            }
        }
        if (!found) {
            System.out.println("Stock not found in holdings.");
        }
    }

    public double getValue() {
        return value;
    }

    public List<Integer> getHoldingsValues() {
        return new ArrayList<>(holdings.values());
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
                System.out.println("Value: " + getValue());
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