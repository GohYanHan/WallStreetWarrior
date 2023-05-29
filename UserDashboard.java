import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class UserDashboard {
    private Portfolio portfolio;
    private double startingAccountBalance;

    public UserDashboard(double startingAccountBalance) {
        portfolio = new Portfolio();
        this.startingAccountBalance = 50000;
    }

    public void addStock(Stock stock, int shares) {
        portfolio.addStock(stock, shares);
    }

    public void removeStock(Stock stock, int shares) {
        portfolio.removeStock(stock, shares);
    }

    public double getAccountBalance() {
        return startingAccountBalance + portfolio.getValue();
    }

    public double getCurrentPoints() {
        double pAndL = portfolio.getValue() - startingAccountBalance;
        return (pAndL / startingAccountBalance) * 100;
    }

    public List<Stock> getOpenPositions() {
        return portfolio.getHoldings().keySet().stream()
                .sorted(Comparator.comparing(Stock::getSymbol))
                .collect(Collectors.toList());
    }

    public List<Trade> getTradeHistorySortedByPrice() {
        return portfolio.getTradeHistory().stream()
                .sorted(Comparator.comparing(Trade::getPrice))
                .collect(Collectors.toList());
    }

    public List<Trade> getTradeHistorySortedByPlacementTime() {
        return portfolio.getTradeHistory().stream()
                .sorted(Comparator.comparing(Trade::getPlacementTime))
                .collect(Collectors.toList());
    }

    public int getStocksLeft() {
        int totalShares = portfolio.getHoldings().values().stream()
                .mapToInt(Integer::intValue)
                .sum();
        return totalShares;
    }
}
