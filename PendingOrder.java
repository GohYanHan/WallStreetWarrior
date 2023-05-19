import java.time.LocalDateTime;
import java.util.Scanner;

public class PendingOrder implements Comparable<PendingOrder> {
    private Stock stock;
    private Order.Type type;
    private int shares;
    private double price;
    private double expectedBuyingPrice;
    private double expectedSellingPrice;

    private LocalDateTime timePlaced;

    public PendingOrder(Stock stock, Order.Type type, int shares, double price, double expectedBuyingPrice, double expectedSellingPrice) {
        this.stock = stock;
        this.type = type;
        this.shares = shares;
        this.price = price;
        this.expectedBuyingPrice = expectedBuyingPrice;
        this.expectedSellingPrice = expectedSellingPrice;
        this.timePlaced = LocalDateTime.now();
    }

    public Stock getStock() {
        return stock;
    }

    public int getShares() {
        return shares;
    }

    public double getPrice() {
        return price;
    }

    public LocalDateTime getTimePlaced() {
        return timePlaced;
    }

    @Override
    public int compareTo(PendingOrder other) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Cancel order based on: \n1. Longest time length \n2. Highest amount of money");
        int choice = sc.nextInt();
        if (choice == 1) {
            // Compare based on the longest time length (the earliest time placed)
            int timeComparison = this.timePlaced.compareTo(other.timePlaced);
            if (timeComparison != 0) {
                return timeComparison;
            }else{
                System.out.println("Time placed is same.");
            }
        } else if(choice == 2){
            // If time is the same, compare based on the highest amount of money (highest price)
            int priceComparison = Double.compare(other.price, this.price);
            if (priceComparison != 0) {
                return priceComparison;
            }else{
                System.out.println("Price offered is same.");
            }
        }
        return choice;
    }
}
