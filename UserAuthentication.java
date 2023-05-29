import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
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

    public static void main(String[] args) throws IOException {
        UserAuthentication userAuth = new UserAuthentication();
        AdminPanel admin = new AdminPanel();
        Database db = new Database();
        API api = new API();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to the Application!");
        while (true) {
            System.out.println("-----------------------------");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Forget Password");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();
            System.out.println("-----------------------------");
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
                        if (db.getUser().getRole().equals("Admin")) {
                            System.out.println("\nWelcome to Admin Panel");
                            admin.adminPanel();
                        } else {
                            // Create a list of stocks

                            TradingEngine tradingEngine = new TradingEngine();
                            // Create a portfolio for the user
                            Portfolio portfolio = new Portfolio();

                            if (tradingEngine.isWithinTradingHours()) {
                                userAuth.loopTrade(API.extractStocks(), portfolio, db.getUser(), tradingEngine);
                            } else {
                                System.out.println("Trading is currently closed. Orders cannot be executed outside trading hours.");
                            }
                        }
                    }

                }
                case 3 -> userAuth.forgetPassword();
                case 4 -> {
                    System.out.println("Exiting...");
                    System.out.println("-----------------------------");
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }

    }


    private static Stock findStockBySymbol(List<Stock> stocks, String symbol) {
        for (Stock stock : stocks) {
            if (stock.getSymbol().equalsIgnoreCase(symbol)) {
                return stock;
            }
        }
        return null;
    }

    private void loopTrade(List<Stock> stocks, Portfolio portfolio, User user, TradingEngine tradingEngine) throws IOException {
        while (true) {
            // Choose between buying or selling
            System.out.println("1. Buy or sell stock \n2. Show current stock owned \n3. Cancel pending orders");
            int choice = scanner.nextInt();

            Order buyOrder = null;
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
                    }

                    // Display suggested price for a stock
                    tradingEngine.displaySuggestedPrice(buyStockSymbol, buyQuantity);

                    System.out.println("Enter expected buying price: ");
                    double buyExpectedPrice = scanner.nextDouble();

                    buyStock = findStockBySymbol(stocks, buyStockSymbol);
                    //can implement placeOrder??
                    if (buyStock != null) {
                        buyOrder = new Order(buyStock, Order.Type.BUY, buyQuantity, buyExpectedPrice, 0.0, user);
                        tradingEngine.executeOrder(buyOrder, portfolio);
                        System.out.println("Stock bought successfully!");
                    } else {
                        System.out.println("Stock with symbol " + buyStockSymbol + " not found.");
                    }

                } else if (choice == 2) {
                    // Place a sell order
                    System.out.println("Enter stock symbol for sell order: ");
                    String sellStockSymbol = scanner.nextLine();
                    // Find the stock by symbol
                    Stock sellStock = findStockBySymbol(stocks, sellStockSymbol);
                    while (sellStock == null) {
                        System.out.println("Stock with symbol " + sellStockSymbol + " not found. Please enter a new stock symbol: ");
                        sellStockSymbol = scanner.nextLine();
                        sellStock = findStockBySymbol(stocks, sellStockSymbol);
                    }


                    System.out.println("Enter quantity for sell order: ");
                    int sellQuantity = scanner.nextInt();

                    // Display suggested price for a stock
                    tradingEngine.displaySuggestedPrice(sellStockSymbol, sellQuantity);

                    System.out.println("Enter expected selling price: ");
                    double sellExpectedPrice = scanner.nextDouble();


                    sellStock = findStockBySymbol(stocks, sellStockSymbol);
                    if (sellStock != null) {
                        Order sellOrder = new Order(sellStock, Order.Type.SELL, sellQuantity, 0.0, sellExpectedPrice, user);
                        tradingEngine.executeOrder(sellOrder, portfolio);
                        System.out.println("Stock successfully bought!");
                    } else {
                        System.out.println("Stock with symbol " + sellStockSymbol + " not found.");
                    }
                }
            } else if (choice == 2) {
                //show current stock owned (trading dashboard)
            } else if (choice == 3) {
                List<Order> buyOrders = new ArrayList<>();
                while (tradingEngine.getBuyOrders() != null) {
                    buyOrders.add((Order) tradingEngine.getBuyOrders().values());
                }
                tradingEngine.cancelBuyOrder(buyOrders); // remove string
            } else {
                System.out.println("Execution invalid");
                return;
            }

        }
    }
}