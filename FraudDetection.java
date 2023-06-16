import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FraudDetection {
    private final Database database = new Database();
    private final User user = new User();
    private Notification notification = new Notification();

    private List<User> notifiedUsers = new ArrayList<>();

    private boolean sentAlready;

    public void sendNotification() {
        List<User> users = database.getUsersList();

        for (User user : users) {

            sentAlready = database.getUserFDNotificationStatus(user.getKey());

            if (isSuspiciousUser(user) && sentAlready == false) {
                List<String> adminEmails = database.getAllAdminEmails();
                for (String adminEmail : adminEmails) {
                    notification.sendNotificationToAdmin(adminEmail, user);
                    database.setUserFDNotificationStatus(user.getKey());
                }
            }
        }
    }


    public void displaySuspiciousUsers() {

        List<User> users = database.getUsersList();

        System.out.println("Suspicious users: ");

        for (User user : users) {
            if (isSuspiciousUser(user)) {
                List<Order> transactions = database.loadTransactionHistory(user.getKey());

                System.out.println("Name: " + user.getUsername());
                System.out.println("Email: " + user.getEmail());

                if (!transactions.isEmpty()) {


                    //   tradeHistory.sort(Comparator.comparing(Order::getExpectedBuyingPrice).thenComparing(Order::getTimestamp));

                    //tradeHistory list will be sorted in ascending order first by expectedBuyingPrice, and if there are elements with the same expectedBuyingPrice, those will be further sorted by timestamp.

                    System.out.println("===========================================================================================");
                    System.out.println("|                                Trade History                                            |");
                    System.out.println("===========================================================================================");


                    int tradeHistorySize = transactions.size(); // Get the size of the tradeHistory list

                    // Iterate through the tradeHistory list and print each order
                    for (int i = 0; i < tradeHistorySize; i++) {
                        Order order = transactions.get(i);

                        System.out.println("| Stock     : " + padRight(order.getStock().getSymbol(), 75) + " |");
                        System.out.println("| Name      : " + padRight(order.getStock().getName(), 75) + " |");
                        System.out.println("| Type      : " + padRight(order.getType().toString(), 75) + " |");
                        System.out.println("| Shares    : " + padRight(String.valueOf(order.getShares()), 75) + " |");

                        if (order.getType() == Order.Type.BUY)
                            System.out.println("| Price     : RM " + padRight(String.valueOf(order.getExpectedBuyingPrice()), 72) + " |");
                        else
                            System.out.println("| Price     : RM " + padRight(String.valueOf(order.getExpectedSellingPrice()), 72) + " |");

                        System.out.println("| Timestamp : " + padRight(order.getTimestamp().toString(), 75) + " |");

                        if (i == tradeHistorySize - 1) {
                        } else {
                            System.out.println("|-----------------------------------------------------------------------------------------|");
                        }
                    }

                    // Print the closing line
                    System.out.println("===========================================================================================");
                }
            }
        }
    }

    private static String padRight(String s, int length) {
        return String.format("%-" + length + "s", s);
    }

    public boolean isSuspiciousUser(User user) {
        return isShortSelling() || checkTradeOnMargin(user);
    }

    public boolean isShortSelling() {
        List<Order> transactions = database.loadTransactionHistory(user.getKey());
        Map<String, Integer> stockShares = new HashMap<>();

        // Calculate the total shares for each stock from trade history
        for (Order order : transactions) {
            String stockSymbol = order.getStock().getSymbol();
            int shares = order.getShares();

            if (order.getType() == Order.Type.BUY) {
                stockShares.put(stockSymbol, stockShares.getOrDefault(stockSymbol, 0) + shares);
            } else if (order.getType() == Order.Type.SELL) {
                stockShares.put(stockSymbol, stockShares.getOrDefault(stockSymbol, 0) - shares);
            }
        }
        // Compare the calculated shares with the user's holdings
        Map<Order, Integer> userHoldings = user.getPortfolio().getHoldings();
        for (Map.Entry<Order, Integer> entry : userHoldings.entrySet()) {
            Order order = entry.getKey();
            int userShares = entry.getValue();
            int calculatedShares = stockShares.getOrDefault(order.getStock().getSymbol(), 0);
            if (userShares < calculatedShares) {
                return true; // User is short selling
            }
        }
        return false; // User is not short selling
    }

    private boolean checkTradeOnMargin(User user) {
        return user.getPortfolio().getAccBalance() > 50000;
    }

}