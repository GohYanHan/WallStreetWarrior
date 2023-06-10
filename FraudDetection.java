import java.util.List;

public class FraudDetection {
    private Database database;
    private User user;

    public FraudDetection(Database database) {
        this.database = database;
        this.user = user;
    }

    List<User> users = database.getUsersList();


    public void displaySuspiciousUsers() {

        System.out.println("Questionable Users:");
        for (User user : users) {
            if (isSuspiciousUser(user)) {
                List<Order> transactions = database.loadTransactionHistory(user.getKey());

                System.out.println("----------------------------------------");
                System.out.println("User: " + user.getUsername());
                System.out.println("Email: " + user.getEmail());
                System.out.println("Questionable Transactions:");

                boolean suspiciousTransactionsFound = false;
                for (Order transaction : transactions) {
                    if (isSuspiciousTransaction(transaction, user)) {
                        suspiciousTransactionsFound = true;
                        displaySuspiciousTransaction(transaction);
                    }
                }

                if (!suspiciousTransactionsFound) {
                    System.out.println("No questionable transactions found.");
                }

                System.out.println("----------------------------------------");
            }
        }
    }

    public boolean isSuspiciousTransaction(Order transaction, User user) {
        return isShortSelling(user.getKey()) && checkDuplicateOrders(transaction, user);
    }

    public boolean isSuspiciousUser(User user) {
        return isShortSelling(user.getKey());
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

    private boolean checkDuplicateOrders(Order order, User user) {
        List<Order> orders = database.loadOrders(user.getKey(), order.getType());

        for (Order existingOrder : orders) {
            if (existingOrder.getStock().equals(order.getStock()) && existingOrder.getShares() == order.getShares()) {
                return true; // Duplicate order found
            }
        }

        return false; // No duplicate orders found
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