import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class UserAuthentication {
    private final Database db = new Database();
    private final Scanner scanner = new Scanner(System.in);
    private final FinanceNewsAPI financeNewsAPI = new FinanceNewsAPI();

    public UserAuthentication() {

    }

    public boolean register() {
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.println("Your password should contain at least one uppercase letter, one lowercase letter, one digit and minimum length of 8 characters.");
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Username: ");
        String name = scanner.nextLine();

        //Format of valid email address
        boolean isEmailValid = email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
        //Format of valid password (at least one uppercase letter, one lowercase letter, one digit and minimum length of 8 characters)
        boolean isPwValid = password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

        if (!isPwValid && !isEmailValid) {
            System.out.println("Invalid email and password. Please enter again.");
            return false;
        } else if (!isEmailValid) {
            System.out.println("Invalid email. Please enter again.");
            return false;
        } else if (!isPwValid) {
            System.out.println("Invalid password. Please enter again.");
            System.out.println("Your password should contain at least one uppercase letter, one lowercase letter, one digit and minimum length of 8 characters.");
            return false;
        }
        return db.addUser(email, hashPassword(password), name);
    }

    public boolean login(String email, String password) throws IOException {
        User user = db.loadUser(email);
        db.setUser(user);
        if (user != null) {
            if (BCrypt.checkpw(password, user.getPassword())) {
                System.out.println("Login successful!");
                System.out.println("Welcome, " + user.getUsername() + "!");
                System.out.println("-".repeat(90));
                System.out.println("Displaying news today...");
                financeNewsAPI.getNews();
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
        if (isPwValid)
            if (db.resetPassword(email, username, hashPassword(newPassword)))
                System.out.println("Password is reset successfully!");
            else
                System.out.println("Invalid email or username, please try again.");
        else
            System.out.println("Invalid password format, please try again.");
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

    private boolean isValidBuyQuantity(int quantity) {
        return quantity >= 100 && quantity <= 500;
    }

    public void loopTrade(List<Stock> stocks, Portfolio portfolio, User user, TradingEngine tradingEngine, Report report) throws IOException {
        while (true) {
            List<Order> buyOrderList = db.loadOrders(user.getKey(), Order.Type.BUY);
            List<Order> sellOrderList = db.loadOrders(user.getKey(), Order.Type.SELL);

            // Choose between buying or selling
            System.out.println("1. Buy or sell stock");
            System.out.println("2. Search stock");
            System.out.println("3. Show current stock owned");
            System.out.println("4. Cancel pending orders");
            System.out.println("5. Display dashboard");
            System.out.println("6. Generate Report");
            System.out.println("7. Log out");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    System.out.println("1. Buy stock \n2. Sell stock");
                    choice = scanner.nextInt();
                    scanner.nextLine();
                    if (choice == 1) {
                        // Display stock in sellOrder list
                        tradingEngine.displayLotpoolSellOrders(db.getLotPool(), sellOrderList);
                        // Place a buy order
                        System.out.println("Enter stock symbol for buy order: ");
                        String buyStockSymbol = scanner.nextLine();

                        // Find the stock by symbol
                        Stock buyStock = findStockBySymbol(stocks, buyStockSymbol);
                        while (buyStock == null) {
                            System.out.println("Stock with symbol " + buyStockSymbol + " not found. Please enter a new stock symbol: ");
                            buyStockSymbol = scanner.nextLine();
                            buyStock = findStockBySymbol(stocks, buyStockSymbol);
                        }

                        System.out.println("Enter quantity for buy order: ");
                        int buyQuantity = scanner.nextInt();
                        while (!isValidBuyQuantity(buyQuantity)) {
                            System.out.println("Invalid quantity. Minimum order quantity is 100 shares (one lot), and maximum is 500 shares.");
                            System.out.println("Enter quantity for buy order: ");
                            buyQuantity = scanner.nextInt();
                        }

                        // Display suggested price for a stock
                        tradingEngine.displaySuggestedPrice(buyStockSymbol, buyQuantity);

                        System.out.println("Enter expected buying price: "); // if add into pending order list then no condition
                        double buyExpectedPrice = scanner.nextDouble();

                        // Format the user input to two decimal points
                        DecimalFormat decimalFormat = new DecimalFormat("#0.00");
                        double formattedBuyExpectedPrice = Double.parseDouble(decimalFormat.format(buyExpectedPrice));

                        buyStock = findStockBySymbol(stocks, buyStockSymbol);

                        if (buyStock != null) {
                            LocalDateTime timestamp = LocalDateTime.now();
                            System.out.println("Add to pending order? [y/n]");
                            String choose = scanner.next();
                            char character = choose.charAt(0);
                            Order buyOrder = new Order(buyStock, Order.Type.BUY, buyQuantity, formattedBuyExpectedPrice, 0.0, user,timestamp);

                            if (character == 'y') {
                                db.addOrder(user.getKey(), buyOrder);
                                System.out.println("Buy order added into pending buy order list.");
                                if (tradingEngine.executeBuyOrdersMatch(buyOrder, portfolio)) {
                                    tradingEngine.executeOrder(buyOrder, portfolio);
                                    db.removeOrder(user.getKey(), buyOrder); // if successfully execute buy order remove from pending buy order
                                }
                            } else {
                                tradingEngine.executeOrder(buyOrder, portfolio);
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
                            Order sellOrder = new Order(sellStock, Order.Type.SELL, sellQuantity, 0.0, sellExpectedPrice, user,timestamp);
                            db.addOrder(user.getKey(), sellOrder);
                            tradingEngine.executeOrder(sellOrder, portfolio);

                        } else {
                            System.out.println("Stock with symbol " + sellStockSymbol + " not found.");
                        }
                    }
                    break;


                case 2:
                    Scanner k = new Scanner(System.in);
                    System.out.println("Search stock using name or symbol. ");
                    String searchstring = k.nextLine();

                    search stocksearch = new search();
                    stocksearch.searchStocks(searchstring);
                    break;

                case 3:
                    portfolio.displayHoldings();
                    break;

                case 4:
                    tradingEngine.cancelBuyOrder(buyOrderList, portfolio);
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
                    break;

                case 7:
                    System.out.println("Logged out successfully!");
                    System.out.println("-".repeat(90));
                    return;

                default:
                    System.out.println("Execution invalid");
                    return;
            }
        }
    }
}