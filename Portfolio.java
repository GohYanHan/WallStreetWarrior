import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Portfolio {
    private Map<Order, Integer> holdings;
    private double value;
    private double accBalance;
    private int userKey;
    private Database db;

    public int getUserKey() {
        return userKey;
    }


    public Portfolio(int userKey, double balance) {
        db = new Database();
        this.userKey = userKey;
        this.holdings = db.loadHolding(userKey);
        this.accBalance = balance;
//        holdingList = db.loadHolding(userKey);
//        for (Order holding : holdingList) {
//            this.holdings.put(holding, holding.getShares());
//        }

    }

    public double addValue(double expectedBuyingPrice) {
        value += expectedBuyingPrice;
        return value;
    }

    public double removeValue(double expectedSellingPrice) {
        value -= expectedSellingPrice;
        return value;
    }

    double getValue() {
        return value;
    }

    public List<Integer> getHoldingsValues() {
        return new ArrayList<>(holdings.values());
    }

    public Map<Order, Integer> getHoldings() {
        return holdings;
    }

    void setAccBalance(double accBalance) {
        if (db.updateUserBalance(userKey, accBalance)) {
            this.accBalance = accBalance;
            System.out.println("New account balance: " + this.accBalance);
        } else
            System.out.println("Account balance is not updated.\nCurrent account balance: " + this.accBalance);
    }

    double getAccBalance() {
        return accBalance;
    }


    public void addStock(Order order, int buyShares) {
        boolean found = false;

        for (Map.Entry<Order, Integer> entry : holdings.entrySet()) {
            Order existingOrder = entry.getKey();
            int shares = entry.getValue();
            if (existingOrder.getStock().getSymbol().equalsIgnoreCase(order.getStock().getSymbol())) {
                int updatedShares = shares + buyShares;
                holdings.replace(existingOrder, shares, updatedShares);
                db.updateHolding(userKey, existingOrder.getStock(), updatedShares);
                found = true;
                break;
            }
        }
        if (!found) {
            holdings.put(order, buyShares);
            db.addHoldings(userKey, order.getStock(), buyShares);
        }
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
                        db.removeHolding(userKey, existingOrder.getStock());
                    } else {
                        holdings.replace(existingOrder, shares, updatedShares);
                        db.updateHolding(userKey, existingOrder.getStock(), updatedShares);
                    }
                    found = true;
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

    void displayHoldings() {
        System.out.println("Holdings:");
//        if (!this.holdingList.isEmpty()) {
//            for (Order holding : holdingList) {
//                System.out.println("Stock: " + holding.getSymbol());
//                System.out.println("Shares: " + holding.getShares());
//                System.out.println("-".repeat(30));
//            }
//        } else {
//            System.out.println("No holdings");
//        }
        if (holdings.isEmpty()) {
            System.out.println("No holdings");
        } else {
            for (Map.Entry<Order, Integer> entry : this.holdings.entrySet()) {
                Order order = entry.getKey();
                int shares = entry.getValue();
                System.out.println("Stock: " + order.getStock().getSymbol());
                System.out.println("Shares: " + shares);
                System.out.println("-".repeat(30));
            }
        }
    }


    void displayBuyOrders() {
        System.out.println("Orders to sell: ");
//        List<Order> orders = db.loadOrder(userKey, Order.Type.BUY);
//        for(Order order: orders){
//            System.out.println("Stock: " + order.getSymbol());
//            System.out.println("Shares: " + order.getShares());
//            System.out.println("-".repeat(30));
//        }
        for (Map.Entry<Order, Integer> entry : this.holdings.entrySet()) {
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

    public List<Order> getTradeHistory() {
        return db.loadTransactionHistory(userKey);
    }

    public void addToTradeHistory(Order order) {
        db.addTransactionHistory(userKey, order);
    }


}