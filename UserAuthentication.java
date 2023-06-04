import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class UserAuthentication {
    private final Database db;
    private final Scanner scanner = new Scanner(System.in);

    public UserAuthentication() {
        db = new Database();
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

    public boolean login(String email, String password) {
        User user = db.loadUser(email);
        db.setUser(user);
        if (user != null) {
            if (BCrypt.checkpw(password, user.getPassword())) {
                System.out.println("Login successful!");
                System.out.println("Welcome, " + user.getUsername() + "!");
                System.out.println("-----------------------------");
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

    public void loopTrade(List<Stock> stocks, Portfolio portfolio, User user, TradingEngine tradingEngine) throws IOException {
        while (true) {
            List<Order> buyOrderList = db.loadOrders(user.getKey(), Order.Type.BUY);
            List<Order> sellOrderList = db.loadOrders(user.getKey(), Order.Type.SELL);

            // Choose between buying or selling
            System.out.println("1. Buy or sell stock \n2. Show current stock owned \n3. Cancel pending orders");
            int choice = scanner.nextInt();

            if (choice == 1) {
                System.out.println("1. Buy stock \n2. Sell stock");
                choice = scanner.nextInt();
                scanner.nextLine();
                if (choice == 1) {
                    // Display stock in sellOrder list
                    tradingEngine.displaySellOrders();
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
                    if (buyQuantity < 100) {
                        System.out.println("Minimum order quantity is 100 shares (one lot).");
                        return;
                    } else if (!tradingEngine.isStartOfTradingDay() && buyQuantity > 500) {
                        System.out.println("Maximum order quantity is 500 shares");
                        return;
                    }

                    // Display suggested price for a stock
                    tradingEngine.displaySuggestedPrice(buyStockSymbol, buyQuantity);

                    System.out.println("Enter expected buying price: ");
                    double buyExpectedPrice = scanner.nextDouble();

                    // Format the user input to two decimal points
                    DecimalFormat decimalFormat = new DecimalFormat("#0.00");
                    double formattedBuyExpectedPrice = Double.parseDouble(decimalFormat.format(buyExpectedPrice));

                    buyStock = findStockBySymbol(stocks, buyStockSymbol);

                    if (buyStock != null) {
                        Order buyOrder = new Order(buyStock, Order.Type.BUY, buyQuantity, formattedBuyExpectedPrice, 0.0, user);
                        tradingEngine.executeOrder(buyOrder, portfolio);
                        LocalDateTime timestamp = LocalDateTime.now();

                        // if executeOrder success, add buyOrderList into a list, link list to cancelOrder() or move cancelOrder here
//                        Order buyOrderListElement = new Order(user.getKey(), buyStockSymbol, buyQuantity, formattedBuyExpectedPrice, timestamp);
                        db.addOrder(user.getKey(), buyStockSymbol, buyQuantity, formattedBuyExpectedPrice, timestamp, Order.Type.BUY);

                    } else {
                        System.out.println("Stock with symbol " + buyStockSymbol + " not found.");
                    }

                } else if (choice == 2) {
                    // display buyOrders
                    portfolio.displayBuyOrders();
                    // Place a sell order
                    System.out.println("Enter stock symbol for sell order: ");
                    String sellStockSymbol = scanner.nextLine();
                    // Find the stock by symbol
                    Stock sellStock = portfolio.findStockBySymbol(sellStockSymbol);
                    while (sellStock == null) {
                        System.out.println("Stock with symbol " + sellStockSymbol + " not found. Please enter a new stock symbol: ");
                        sellStockSymbol = scanner.nextLine();
                        sellStock = portfolio.findStockBySymbol(sellStockSymbol);
                    }


                    System.out.println("Enter quantity for sell order: ");
                    int sellQuantity = scanner.nextInt();

                    // Display suggested price for a stock
                    tradingEngine.displaySuggestedPrice(sellStockSymbol, sellQuantity);

                    System.out.println("Enter expected selling price: ");
                    double sellExpectedPrice = scanner.nextDouble();

                    // Format the user input to two decimal points
                    DecimalFormat decimalFormat = new DecimalFormat("#0.00");
                    double formattedSellingPrice = Double.parseDouble(decimalFormat.format(sellExpectedPrice));

                    sellStock = portfolio.findStockBySymbol(sellStockSymbol);
                    if (sellStock != null) {
                        Order sellOrder = new Order(sellStock, Order.Type.SELL, sellQuantity, 0.0, sellExpectedPrice, user);
                        tradingEngine.executeOrder(sellOrder, portfolio);
                        LocalDateTime timestamp = LocalDateTime.now();

//                        Order sellOrderListElement = new Order(user.getKey(), sellStockSymbol, sellQuantity, formattedSellingPrice, timestamp);
//                        sellOrderList.add(sellOrderListElement);
                        db.addOrder(user.getKey(), sellStockSymbol, sellQuantity, formattedSellingPrice, timestamp, Order.Type.SELL);

                    } else {
                        System.out.println("Stock with symbol " + sellStockSymbol + " not found.");
                    }
                }
            } else if (choice == 2) {
                portfolio.displayHoldings();
            } else if (choice == 3) {
                tradingEngine.cancelBuyOrder(buyOrderList, portfolio);
            } else {
                System.out.println("Execution invalid");
                return;
            }

        }
    }

    public void displayOrderList(List<Order> orders) {
        for (Order order : orders) {
            System.out.println("Stock: " + order.getStock().getSymbol());
            System.out.println("Price: " + order.getExpectedBuyingPrice());
            System.out.println("TimeStamp: " + order.getTimestamp());
            System.out.println("-".repeat(30));
        }
    }
}