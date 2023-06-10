import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class UserAuthentication {
    private final Database db = new Database();
    private final Scanner scanner = new Scanner(System.in);
    private final FinanceNewsAPI financeNewsAPI = new FinanceNewsAPI();
    private final TradingEngine tradingEngine = new TradingEngine();
    private final Notification notification = new Notification();


    public UserAuthentication() throws IOException {

    }

    public boolean register() {
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.println("Your password should contain at least one uppercase letter,\none lowercase letter, one digit and minimum length of 8 characters.");
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Username: ");
        String name = scanner.nextLine();

        //Format of valid email address
        boolean isEmailValid = email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
        //Format of valid password (at least one uppercase letter, one lowercase letter, one digit and minimum length of 8 characters)
        boolean isPwValid = password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

        if (!isPwValid && !isEmailValid) {
            System.out.println("Invalid email and password.");
            return false;
        } else if (!isEmailValid) {
            System.out.println("Invalid email.");
            return false;
        } else if (!isPwValid) {
            System.out.println("Invalid password.");
            return false;
        }
        return db.addUser(email, hashPassword(password), name);
    }

    public boolean login(String email, String password) throws IOException {
        User user = db.loadUserByEmail(email);
        db.setUser(user);
        if (user != null) {
            if (BCrypt.checkpw(password, user.getPassword())) {
                System.out.println("Login successful!");
                System.out.println("Welcome, " + user.getUsername() + "!");
                System.out.println("-".repeat(120));
                if (!Objects.equals(db.getUser().getRole(), "Admin")) {
                    System.out.println("Displaying news today...");
//                    financeNewsAPI.getNews();
                }
                return true;
            }
        }
        System.out.println("Invalid email or password, please try again.");
        return false;
    }

    public void forgetPassword() {
        System.out.print("Please enter your email address: ");
        String email = scanner.nextLine();
        System.out.print("Please enter your username: ");
        String username = scanner.nextLine();
        System.out.println("Your new password should contain at least one uppercase letter, one lowercase letter, one digit and minimum length of 8 characters.");
        System.out.print("Please enter your new password: ");
        String newPassword = scanner.nextLine();
        boolean isPwValid = newPassword.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");
        if (isPwValid) if (db.resetPassword(email, username, hashPassword(newPassword)))
            System.out.println("Password is reset successfully!");
        else System.out.println("Invalid email or username, please try again.");
        else System.out.println("Invalid password format, please try again.");
    }

    String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    private static Stock findStockBySymbol(List<Stock> stocks, String symbol) {
        for (Stock stock : stocks) {
            if (stock.getSymbol().equalsIgnoreCase(symbol)) {
                return stock;
            }
        }
        return null;
    }

    private boolean isValidQuantity(int quantity) {
        if (!tradingEngine.isWithinInitialTradingPeriod()) {
            return quantity >= 100 && quantity <= 500;
        } else {
            return quantity >= 100;
        }
    }

    public void loopTrade(List<Stock> stocks, Portfolio portfolio, User user, TradingEngine tradingEngine, Report report) throws IOException {
        while (true) {
            boolean running = true;

            while (running) {
                // Display menu and prompt for choice
                System.out.println();
                System.out.println("=".repeat(40));
                System.out.printf("%-15s%-24s%s%n", "|", "Main Menu", "|");
                System.out.println("=".repeat(40));
                System.out.printf("%-39s%s%n", "| 1. Buy or sell stock", "|");
                System.out.printf("%-39s%s%n", "| 2. Search stock", "|");
                System.out.printf("%-39s%s%n", "| 3. Show current stock owned", "|");
                System.out.printf("%-39s%s%n", "| 4. Cancel pending orders", "|");
                System.out.printf("%-39s%s%n", "| 5. Display dashboard", "|");
                System.out.printf("%-39s%s%n", "| 6. Display Leaderboard", "|");
                System.out.printf("%-39s%s%n", "| 7. Generate Report", "|");
                System.out.printf("%-39s%s%n", "| 8. Notification Settings", "|");
                System.out.printf("%-39s%s%n", "| 9. Set Threshold ", "|");
                System.out.printf("%-39s%s%n", "| 10. Log Out", "|");

                System.out.println("=".repeat(40));
                System.out.print("Enter your choice: ");

                try {
                    int choice = scanner.nextInt();
                    scanner.nextLine();

                    switch (choice) {
                        case 1:
                            if (!user.getStatus().equalsIgnoreCase("disqualified")) {
                                System.out.println("1. Buy stock \n2. Sell stock");
                                System.out.print("Enter your choice: ");
                                choice = scanner.nextInt();
                                scanner.nextLine();
                                if (choice == 1) {
                                    // Display stock in sellOrder list & api
                                    tradingEngine.displayLotpoolSellOrders(db.loadOrders(user.getKey(), Order.Type.SELL));
                                    // Place a buy order
                                    System.out.print("Enter stock symbol for buy order: ");
                                    String buyStockSymbol = scanner.nextLine();

                                    // Find the stock by symbol
                                    Stock buyStock = findStockBySymbol(stocks, buyStockSymbol);
                                    while (buyStock == null) {
                                        System.out.print("Stock with symbol " + buyStockSymbol + " not found. Please enter a new stock symbol: ");
                                        buyStockSymbol = scanner.nextLine();
                                        buyStock = findStockBySymbol(stocks, buyStockSymbol);
                                    }

                                    System.out.print("Enter quantity for buy order: ");
                                    int buyQuantity = scanner.nextInt();
                                    while (!isValidQuantity(buyQuantity)) {
                                        System.out.println("Invalid quantity. Minimum buy order quantity is 100 shares (one lot), and maximum is 500 shares.");
                                        System.out.print("Enter quantity for buy order: ");
                                        buyQuantity = scanner.nextInt();
                                    }

                                    // Display suggested price for a stock
                                    tradingEngine.displaySuggestedPrice(buyStockSymbol, buyQuantity);

                                    System.out.print("Enter expected buying price: "); // if add into pending order list then no condition
                                    double buyExpectedPrice = scanner.nextDouble();

                                    // Format the user input to two decimal points
                                    DecimalFormat decimalFormat = new DecimalFormat("#0.00");
                                    double formattedBuyExpectedPrice = Double.parseDouble(decimalFormat.format(buyExpectedPrice));

                                    buyStock = findStockBySymbol(stocks, buyStockSymbol);

                                    if (buyStock != null) {
                                        LocalDateTime timestamp = LocalDateTime.now();
                                        System.out.print("Add to pending order? [y/n] ");
                                        String choose = scanner.next();
                                        char character = choose.charAt(0);
                                        Order buyOrder = new Order(buyStock, Order.Type.BUY, buyQuantity, formattedBuyExpectedPrice, 0.0, user, timestamp);

                                        if (character == 'y') {
                                            db.addOrder(user.getKey(), buyOrder);
                                            System.out.println("Buy order added into pending buy order list.");
                                            if (tradingEngine.autoMatching(db.loadOrders(user.getKey(), Order.Type.BUY), portfolio)) { // how to make it keep check
                                                db.removeOrder(user.getKey(), buyOrder); // if successfully execute buy order remove from pending buy order
                                            }
                                        } else {
                                            tradingEngine.executeOrder(buyOrder, portfolio);
                                            System.out.println("Buy order executed successfully.");
                                        }
                                    } else {
                                        System.out.println("Stock with symbol " + buyStockSymbol + " not found.");
                                    }
                                } else if (choice == 2) {
                                    // display buyOrders
                                    portfolio.displayBuyOrders();
                                    // Place a sell order
                                    System.out.print("Enter stock symbol for sell order: ");
                                    String sellStockSymbol = scanner.nextLine();
                                    // Find the stock by symbol
                                    Stock sellStock = portfolio.findStockBySymbol(sellStockSymbol);
                                    while (sellStock == null) {
                                        System.out.println("Stock with symbol " + sellStockSymbol + " not found. Please enter a new stock symbol: ");
                                        sellStockSymbol = scanner.nextLine();
                                        sellStock = portfolio.findStockBySymbol(sellStockSymbol);
                                    }

                                    System.out.print("Enter quantity for sell order: ");
                                    int sellQuantity = scanner.nextInt();
                                    while (sellQuantity < 100) {
                                        System.out.println("Invalid quantity. Minimum sell order quantity is 100 shares (one lot).");
                                        System.out.println("Enter quantity for sell order: ");
                                        sellQuantity = scanner.nextInt();
                                    }

                                    // Display suggested price for a stock
                                    tradingEngine.displaySuggestedPrice(sellStockSymbol, sellQuantity);

                                    System.out.print("Enter expected selling price: ");
                                    double sellExpectedPrice = scanner.nextDouble();

                                    // Format the user input to two decimal points
                                    DecimalFormat decimalFormat = new DecimalFormat("#0.00");
                                    double formattedSellingPrice = Double.parseDouble(decimalFormat.format(sellExpectedPrice));

                                    sellStock = portfolio.findStockBySymbol(sellStockSymbol);
                                    if (sellStock != null) {
                                        LocalDateTime timestamp = LocalDateTime.now();
                                        Order sellOrder = new Order(sellStock, Order.Type.SELL, sellQuantity, 0.0, formattedSellingPrice, user, timestamp);
                                        if (tradingEngine.executeOrder(sellOrder, portfolio)) {
                                            db.addOrder(user.getKey(), sellOrder);
                                        }

                                    } else {
                                        System.out.println("Stock with symbol " + sellStockSymbol + " not found.");
                                    }
                                }
                            } else {
                                System.out.println("User is disqualified. Cannot buy or sell orders");
                            }
                            break;


                        case 2:
                            Scanner k = new Scanner(System.in);
                            System.out.print("Enter name/symbol to search: ");
                            String searchstring = k.next();

                            search stocksearch = new search();
                            stocksearch.searchStocks(searchstring);
                            break;

                        case 3:
                            portfolio.displayHoldings();
                            break;

                        case 4:
                            if (!user.getStatus().equalsIgnoreCase("disqualified")) {
                                tradingEngine.cancelBuyOrder(db.loadOrders(user.getKey(), Order.Type.BUY));
                            } else {
                                System.out.println("User is disqualified. Cannot buy or sell orders");
                            }
                            break;

                        case 5:
                            UserDashboard dashboard = new UserDashboard(user);
                            dashboard.displayAccountBalance();
                            dashboard.displayCurrentPoints();
                            dashboard.displayOpenPositions();
                            dashboard.displayTradeHistory();
                            dashboard.chooseSort();
                            break;

                        case 6:
                            report.generateReport();
//                            notification.sendNotification(5, stocks.get(2));
                            break;

                        case 8:
                            System.out.println("Notification\nCurrent notification setting is set to " + ((Notification.notificationSendSetting)? "ON":"OFF") + "\n1.turn ON \n2.turn OFF");
                            System.out.print("Enter your choice: ");
                            choice = scanner.nextInt();
                            if (choice == 1) {
                                notification.setNotificationSendSettingTrue();
                                Notification.saveNotificationSettings();
                                break;
                            } else if (choice == 2) {
                                notification.setNotificationSendSettingFalse();
                                Notification.saveNotificationSettings();
                                break;
                            } else {
                                System.out.println("Execution invalid");
                                break;
                            }
                        case 9:
                            user = db.loadUserByKey(user.getKey());
                            System.out.println("Current threshold value is set to RM" + (user.getThresholds()) + "\nThreshold range has to be within 10 - 10000\nEnter an amount to set as your threshold: ");
                            while (true) {
                                if (scanner.hasNextDouble()) {
                                    double thresholds = scanner.nextDouble();
                                    if (thresholds > 10000 || thresholds < 10) {
                                        System.out.println("Threshold amount not within range.\nPlease enter a different amount.");
                                    } else {
                                        db.updateUserThresholds(user.getKey(), thresholds);
                                        System.out.println("Threshold set successfully!");
                                        break;
                                    }
                                } else {
                                    System.out.println("Threshold has to be a number. Please enter a valid amount.");
                                    scanner.next();
                                }
                            }
                            break;
                        case 10:
                            System.out.println("Logged out successfully!");
                            running = false; // Set running to false to exit the loop
                            return;

                        default:
                            System.out.println("Invalid choice. Please enter a number from 1 to 9.");
                            break;
                    }
                } catch (InputMismatchException e) {
                    System.out.println("Invalid input. Please enter a number.");
                    scanner.nextLine(); // Consume the invalid input
                }
            }
        }
    }
}