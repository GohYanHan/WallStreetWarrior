import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class Report {
    Database database = new Database();

    public void generateReport() {
        User user = database.getUser();
        if (user != null) {
            String username = user.getUsername();
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = System.getProperty("user.home") + "/Downloads/" + username + "_" + currentTime + ".txt";

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
                // Center alignment for the title
                String title = "User Report";
                int totalWidth = 50;
                int titleWidth = title.length();
                int padding = (totalWidth - titleWidth) / 2;
                writer.write(String.format("%" + padding + "s%s%" + padding + "s", "", title, "")); // Centered title
                writer.newLine();
                writer.newLine();

                // User details
                writer.write("Username: " + username);
                writer.newLine();
                writer.write("Email: " + user.getEmail());
                writer.newLine();
                writer.write("Status: " + user.getStatus());
                writer.newLine();
                writer.write("Balance: " + user.getBalance());
                writer.newLine();
                writer.write("PL Points: " + user.getPL_Points());
                writer.newLine();
                writer.newLine();

                // Holdings
                Map<Order, Integer> holdings = database.loadHolding(user.getKey());
                writer.write("Holdings:");
                writer.newLine();
                if (!holdings.isEmpty()) {
                    for (Map.Entry<Order, Integer> entry : holdings.entrySet()) {
                        Order order = entry.getKey();
                        int shares = entry.getValue();
                        writer.write("Stock: " + order.getStock().getSymbol());
                        writer.newLine();
                        writer.write("Shares: " + shares);
                        writer.newLine();
                        writer.write("-".repeat(30));
                        writer.newLine();
                    }
                } else {
                    writer.write("No holdings");
                    writer.newLine();
                }

                // Trade history
                List<Order> tradeHistory = database.loadTransactionHistory(user.getKey());
                writer.write("Trade History:");
                writer.newLine();
                if (!tradeHistory.isEmpty()) {
                    for (Order order : tradeHistory) {
                        writer.write("Stock: " + order.getStock().getSymbol());
                        writer.newLine();
                        writer.write("Type: " + order.getType());
                        writer.newLine();
                        writer.write("Shares: " + order.getShares());
                        writer.newLine();
                        writer.write("Price: $" + (order.getType() == Order.Type.BUY ? order.getExpectedBuyingPrice() : order.getExpectedSellingPrice()));
                        writer.newLine();
                        writer.write("Timestamp: " + order.getTimestamp());
                        writer.newLine();
                        writer.write("-".repeat(30));
                        writer.newLine();
                    }
                } else {
                    writer.write("No trade history.");
                }

                System.out.println("User report generated successfully.");
            } catch (IOException e) {
                System.out.println("An error occurred while generating the user report.");
                e.printStackTrace();
            }
        } else {
            System.out.println("User data not found in the database.");
        }
    }
}
