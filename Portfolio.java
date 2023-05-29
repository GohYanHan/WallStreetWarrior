import java.util.HashMap;
import java.util.Map;

public class Portfolio {
    private Map<Stock, Integer> holdings;
    private double accBalance;

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

    public double getValue() {
        double value = 0;
        for (Map.Entry<Stock, Integer> entry : holdings.entrySet()) {
            Stock stock = entry.getKey();
            int shares = entry.getValue();
            value += stock.getPrice() * shares;
        }
        return value;
    }//buy +, sell - each stock budongchan

    public void setAccBalance(double accBalance) {
        this.accBalance = accBalance;
    }

    public double getAccBalance(){
        return accBalance;
    }
}