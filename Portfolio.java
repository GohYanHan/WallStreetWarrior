import java.util.HashMap;
import java.util.Map;

public class Portfolio {
    private Map<Stock, Integer> holdings;

    public Portfolio() {
        holdings = new HashMap<>();
    }

    public void addStock(Stock stock, int shares) {
        int currentShares = holdings.getOrDefault(stock, 0);
        holdings.put(stock, currentShares + shares);
    }

    public void removeStock(Stock stock, int shares) {
        int currentShares = holdings.getOrDefault(stock, 0);
        if (currentShares >= shares) {
            holdings.put(stock, currentShares - shares);
        }
    }

    public Map<Stock, Integer> getHoldings() {
        return holdings;
    }

    public double getValue() { // stock de value only no 本钱
        double value = 50000; // initial fund = RM50000
        for (Map.Entry<Stock, Integer> entry : holdings.entrySet()) {
            Stock stock = entry.getKey();
            int shares = entry.getValue();
            value += stock.getPrice() * shares;
        }
        return value;
    }
}