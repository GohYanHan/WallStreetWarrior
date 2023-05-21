import java.time.LocalDateTime;

public class Order {
    public enum Type {
        BUY,
        SELL
    }

    private Stock stock;
    private Type type;
    private int shares;
    private double price;
    private LocalDateTime timestamp;
    private User user;
    private double expectedBuyingPrice;
    private static double expectedSellingPrice;
    //add time

    public Order(Stock stock, Type type, int shares, double price, double expectedBuyingPrice, double expectedSellingPrice, User user) {
        this.stock = stock;
        this.type = type;
        this.shares = shares;
        this.price = price;
        this.expectedBuyingPrice = expectedBuyingPrice;
        this.expectedSellingPrice = expectedSellingPrice;
        this.user = user;
    }

    public Stock getStock() {
        return stock;
    }

    public Type getType() {
        return type;
    }

    public int getShares() {
        return shares;
    }

    public double getPrice() {
        return price;
    }

    public double getExpectedBuyingPrice() {
        return expectedBuyingPrice;
    }

    public static double getExpectedSellingPrice() {
        return expectedSellingPrice;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    public void setUser(User user) {
        this.user = user;
    }

    public void setShares(int shares) {
        this.shares = shares;
    }

    public User getUser() {
        return user;
    }
}
