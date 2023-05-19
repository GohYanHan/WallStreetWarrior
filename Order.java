public class Order {
    public enum Type {
        BUY,
        SELL
    }

    private Stock stock;
    private Type type;
    private int shares;
    private double price;
    private double expectedBuyingPrice;
    private static double expectedSellingPrice;
    //add time

    public Order(Stock stock, Type type, int shares, double price, double expectedBuyingPrice, double expectedSellingPrice) {
        this.stock = stock;
        this.type = type;
        this.shares = shares;
        this.price = price;
        this.expectedBuyingPrice = expectedBuyingPrice;
        this.expectedSellingPrice = expectedSellingPrice;
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
}
