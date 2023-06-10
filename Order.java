import java.time.LocalDateTime;

public class Order {
    public enum Type {
        BUY,
        SELL
    }
    private Stock stock;
    private Type type;
    private int shares;
    private LocalDateTime timestamp;
    private User user;

    private double price;
    private double expectedBuyingPrice;
    private double expectedSellingPrice;
    private int userKey;
    public Order() {
    }

    //add time
    public Order(int userKey, Stock stock) {
        this.userKey = userKey;
        this.stock = stock;
    }

    public Order(Stock stock, Type type, int shares, double expectedBuyingPrice, double expectedSellingPrice, User user, LocalDateTime timestamp) {
        this.stock = stock;
        this.type = type;
        this.shares = shares;
        this.expectedBuyingPrice = expectedBuyingPrice;
        this.expectedSellingPrice = expectedSellingPrice;
        this.user = user;
        this.timestamp = timestamp;
    }

    public Order(int userKey, Stock stock, int shares, double price, LocalDateTime timestamp, Type type) {
        this.stock = stock;
        this.userKey = userKey;
        this.shares = shares;
        this.timestamp = timestamp;
        this.type = type;
        if (type == Type.BUY)
            this.expectedBuyingPrice = price;
        else this.expectedSellingPrice = price;
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

    public double getExpectedSellingPrice() {
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

    public User getUser() {
        return user;
    }

    public int getUserKey() {
        return userKey;
    }
}