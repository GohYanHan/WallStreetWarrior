public class Trade {
    private Stock stock;
    private double price;
    private long placementTime;

    public Trade(Stock stock, double price, long placementTime) {
        this.stock = stock;
        this.price = price;
        this.placementTime = placementTime;
    }

    public Stock getStock() {
        return stock;
    }

    public double getPrice() {
        return price;
    }

    public long getPlacementTime() {
        return placementTime;
    }
}