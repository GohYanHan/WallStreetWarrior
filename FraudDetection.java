import java.util.List;

public class FraudDetection {
    private Notification notification;
    private Database database;
    private User user;

    public FraudDetection(Database database) {
        this.database = database;
        this.user = new User();
        this.notification = new Notification();
    }

    List<User> users = database.getUsersList();


    public void displaySuspiciousUsers() {

        System.out.println("Questionable Users:");
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

    public boolean isSuspiciousTransaction(Order transaction, User user) {
        return isShortSelling(user.getKey()) || checkTradeOnMargin(user);
    }

    public boolean isSuspiciousUser(User user) {
        return isShortSelling(user.getKey()) || checkTradeOnMargin(user);
    }

    public boolean isShortSelling(int userKey) {
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

    private void sendNotification(String userEmail, Order order) {
        // Logic to send a notification to the provided userEmail
        // You can implement the notification mechanism here
        // For example:
        System.out.println("Sending notification to admin: " + userEmail);
        System.out.println("Order details: " + order.toString());
        System.out.println("Notification sent.");
    }

    private void displaySuspiciousTransaction(Order transaction) {
        System.out.println("=================================================================================");
        System.out.printf("| Stock     : %-75s |\n", transaction.getStock().getSymbol());
        System.out.printf("| Name      : %-75s |\n", transaction.getStock().getName());
        System.out.printf("| Type      : %-75s |\n", transaction.getType());
        System.out.printf("| Shares    : %-75s |\n", transaction.getShares());

        if (transaction.getType() == Order.Type.BUY)
            System.out.printf("| Price     : RM %-72s |\n", transaction.getExpectedBuyingPrice());
        else
            System.out.printf("| Price     : RM %-72s |\n", transaction.getExpectedSellingPrice());

        System.out.printf("| Timestamp : %-75s |\n", transaction.getTimestamp());
        System.out.println("=================================================================================");
    }
}