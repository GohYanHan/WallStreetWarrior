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
        double roundedBalance = Math.round(accBalance * 100.0) / 100.0; // Round to two decimal places

        if (db.updateUserBalance(userKey, roundedBalance)) {
            this.accBalance = roundedBalance;
            System.out.println("New account balance: " + this.accBalance);
        } else {
            System.out.println("Account balance is not updated.\nCurrent account balance: " + this.accBalance);
        }
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
        Map<Order,Integer> holdings = db.loadHolding(order.getUserKey());
        for (Map.Entry<Order, Integer> entry : holdings.entrySet()) {
            Order existingOrder = entry.getKey();
            int shares = entry.getValue();

            if (existingOrder.getStock().getSymbol().equalsIgnoreCase(order.getStock().getSymbol())) {
                if (shares >= soldShares) {
                    int updatedShares = shares - soldShares;
                    if (updatedShares == 0) {
                        holdings.remove(existingOrder);
                        db.removeHolding(order.getUserKey(), existingOrder.getStock());
                    } else {
                        holdings.replace(existingOrder, shares, updatedShares);
                        db.updateHolding(order.getUserKey(), existingOrder.getStock(), updatedShares);
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
            System.out.println("           Holdings           ");
            System.out.println("------------------------------");
            System.out.println("|    Stock     |    Shares   |");
            System.out.println("------------------------------");
            for (Map.Entry<Order, Integer> entry : this.holdings.entrySet()) {
                Order order = entry.getKey();
                int shares = entry.getValue();
                System.out.printf("|%10s    |   %5d     |\n", order.getStock().getSymbol(), shares);
                System.out.println("------------------------------");
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

    public boolean isHoldingsEmpty() {
        return holdings.isEmpty();
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