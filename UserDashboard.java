import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class UserDashboard {
    private User user;
    private Database db = new Database();


    public UserDashboard(User user) {
        this.user = user;
        this.db = db;
    }

    public void displayAccountBalance() {
        double accountBalance = user.getPortfolio().getAccBalance();
        System.out.println("Account Balance: $" + accountBalance);
    }

    public void displayCurrentPoints() {
        double startingBalance = 50000.0; // Assuming a fixed starting balance

        //Overall account profit / loss  (account balance)

        double pAndL = user.getPortfolio().getAccBalance() - startingBalance;
        int points = (int) ((pAndL / startingBalance) * 100);
        db.updateUserPLpoint(user.getKey(), points);
        System.out.println("Current Points: " + points);
    }


    public void displayOpenPositions() {
        user.getPortfolio().displayHoldings();
    }

    public void displayTradeHistory() {

        System.out.println("Trade History:");
        List<Order> tradeHistory = db.loadTransactionHistory(user.getKey());

        if (!tradeHistory.isEmpty()) {


            //   tradeHistory.sort(Comparator.comparing(Order::getExpectedBuyingPrice).thenComparing(Order::getTimestamp));

            //tradeHistory list will be sorted in ascending order first by expectedBuyingPrice, and if there are elements with the same expectedBuyingPrice, those will be further sorted by timestamp.


            for (Order order : tradeHistory) {
                System.out.println("Stock: " + order.getStock().getSymbol());
                System.out.println("Type: " + order.getType());
                System.out.println("Shares: " + order.getShares());

                if (order.getType() == Order.Type.BUY)
                    System.out.println("Price: $" + order.getExpectedBuyingPrice());
                else
                    System.out.println("Price: $" + order.getExpectedSellingPrice());

                System.out.println("Timestamp: " + order.getTimestamp());
                System.out.println("-".repeat(30));
            }

        }
    }


    //lowest price to highest price
    public void sortTradeHistoryByPrice() {
        List<Order> tradeHistorybyprice = db.loadTransactionHistory(user.getKey());
        tradeHistorybyprice.sort(Comparator.comparing(Order::getExpectedBuyingPrice));
        displayTradeHistory();
    }


    //oldest to newest
    public void sortTradeHistoryByPlacementTime() {
        List<Order> tradeHistorybyplacementtime = db.loadTransactionHistory(user.getKey());

        tradeHistorybyplacementtime.sort(Comparator.comparing(Order::getTimestamp));
        displayTradeHistory();
    }

    public void chooseSort() {

        int i;

        Scanner k = new Scanner(System.in);
        System.out.println("1: Sort by price");
        System.out.println("2: Sort by placement time");

        i = k.nextInt();

        if (i == 1) {
            sortTradeHistoryByPrice();
        } else if (i == 2) {
            sortTradeHistoryByPlacementTime();
        } else {
            System.out.println("invalid choice, try again");
            chooseSort();
        }
    }

}

