import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        UserAuthentication userAuth = new UserAuthentication();
        AdminPanel admin = new AdminPanel();
        Database db = new Database();
        API api = new API();
        Report report = new Report();
        Scanner scanner = new Scanner(System.in);
        Leaderboard leaderboard = new Leaderboard();

        System.out.println("Welcome to the Application!");
        while (true) {
            System.out.println("-".repeat(120));
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Forget Password");
            System.out.println("4. Leaderboard");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();
            System.out.println("-".repeat(120));
            switch (choice) {
                case 1 -> {
                    if (userAuth.register())
                        System.out.println("Registration successful!");
                    else System.out.println("Registration failed, please try again.");
                }
                case 2 -> {
                    System.out.print("Email: ");
                    String email = scanner.nextLine();
                    System.out.print("Password: ");
                    String password = scanner.nextLine();
                    if (userAuth.login(email, password)) {
                        User user = db.getUser();

                        if (user.getRole().equals("Admin")) {
                            System.out.println("Welcome to Admin Panel");
                            admin.adminPanel();
                        } else {
                            // Create a list of stocks
                            TradingEngine tradingEngine = new TradingEngine();
//                            tradingEngine.runAutoMatchingInBackground(db.loadOrders(user.getKey(), Order.Type.BUY), user.getPortfolio());
//                            if (tradingEngine.isWithinTradingHours()) {
                            userAuth.loopTrade(api.extractStocks(), user.getPortfolio(), user, tradingEngine, report);
//
//                            } else {
//                                tradingEngine.closeMarket(db.getUser());
//                                System.out.println("Trading is currently closed. Orders cannot be executed outside trading hours.");
//                            }
                        }
                    }

                }
                case 3 -> userAuth.forgetPassword();
                case 4 -> leaderboard.printLeaderboard();
                case 5 -> {
                    System.out.println("Exiting...");
                    System.out.println("-".repeat(120));
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }

    }
}
