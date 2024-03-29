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
    private int orderID;
    private double expectedBuyingPrice;
    private double expectedSellingPrice;
    private int userKey;
    //add time
    public Order(int userKey, Stock stock) {
        this.userKey = userKey;
        this.stock = stock;
    }

    public Order(int orderID, User user, Stock stock, int shares, double price, LocalDateTime timestamp, Type type) {
        this.orderID = orderID;
        this.stock = stock;
        this.user = user;
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

    public double getExpectedBuyingPrice() {
        return expectedBuyingPrice;
    }

    public double getExpectedSellingPrice() {
        return expectedSellingPrice;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public int getOrderID() {
        return orderID;
    }
}