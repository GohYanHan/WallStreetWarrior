import java.util.List;

public class FraudDetection {
    private final Database database = new Database();
    private final User user = new User();

    private Notification notification = new Notification();


    public void sendNotification() {

        List<User> users = database.getUsersList();

        for (User user : users) {
            if (isSuspiciousUser(user)) {
                List<Order> transactions = database.loadTransactionHistory(user.getKey());


                // Send notifications to admin users
                List<String> adminEmails = database.getAllAdminEmails();
                for (String adminEmail : adminEmails) {
                    notification.sendNotificationToAdmin(adminEmail, transactions, user);
                }
            }
        }
    }


    public void displaySuspiciousUsers() {

        List<User> users = database.getUsersList();

        System.out.println("Suspicous users: ");

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

        int totalSharesBought = 0;
        int totalSharesSold = 0;

        for (Order transaction : transactions) {
            if (transaction.getStock().equals(Order.Type.BUY)) {
                totalSharesBought += transaction.getShares();
            } else if (transaction.getStock().equals(Order.Type.SELL)) {
                totalSharesSold += transaction.getShares();
            }
        }

        return totalSharesSold > totalSharesBought;
    }

    private boolean checkTradeOnMargin(User user) {
        return user.getPortfolio().getAccBalance() > 50000;
    }

}
