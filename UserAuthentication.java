import org.mindrot.jbcrypt.BCrypt;

import java.io.*;
import java.util.*;

public class UserAuthentication {
    //Admin account and password
    private final String ADMIN_EMAIL = "22004848@siswa.um.edu.my";
    private final String ADMIN_PASSWORD = "Wa11Street";
    private Map<String, User> users;
    private static final String USERS_DATA = "data.txt";
    private final Scanner scanner = new Scanner(System.in);
    private List<User> user;

    public List<User> getUser() {
        return user;
    }

    public Map<String, User> getUsers() {
        return users;
    }

    public UserAuthentication() {
        users = new HashMap<>();
        user = new ArrayList<>();
        read();
    }

    public boolean register(String email, String password, String name) {
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
        User user = new User(email, hashPassword(password), name);
        users.put(email, user);
        write();
        System.out.println("Registration successful!");
        return true;
    }

    public boolean login(String email, String password) {
        User user = users.get(email);
        if (user != null && BCrypt.checkpw(password, hashPassword(password))) {
            System.out.println("Login successful!");
            System.out.println("Welcome, " + user.getName() + "!");
            System.out.println("-----------------------------");
            return true;
        }
        System.out.println("Invalid email or password. Please try again.");

        return false;
    }

    void write() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_DATA))) {
            for (User user : users.values()) {
                writer.write(user.getEmail() + "," + user.getPassword() + "," + user.getName() + "," + user.isDisqualified() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void read() {
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_DATA))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                String email = data[0];
                String password = data[1];
                String name = data[2];
                boolean isDisqualified = Boolean.parseBoolean(data[3]);
                User user = new User(email, password, name);
                user.setDisqualified(isDisqualified);
                this.user.add(user);
                users.put(email, user);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public void disqualifyUser(String email) {
        User user = users.get(email);
        if (user != null) {
            user.setDisqualified(true);
            System.out.println("User (" + email + ") has been disqualified.");
        } else {
            System.out.println("User (" + email + ") not found.");
        }
    }

    // Method to list all users
    public void listUsers() {
        System.out.printf("%-30s%-20s%-15s%n", "Email", "Name", "Status");
        for (User user : users.values()) {
            System.out.printf("%-30s%-20s%-15s%n", user.getEmail(), user.getName(), (user.isDisqualified() ? "Disqualified" : "Qualified"));
        }
    }

    // Method to add a user to the system
    public void addUser(User user) {
        users.put(user.getEmail(), user);
        write();
        System.out.println("User (" + user.getEmail() + ") has been added.");
    }

    public void removeUser(String email) {
        User user = users.remove(email);
        if (user != null) {
            write();
            System.out.println("User (" + email + ") has been removed.");
        } else {
            System.out.println("User (" + email + ") not found.");
        }
    }

    public void updateUser(String email) {
        User user = users.get(email);
        if (user != null) {
            System.out.println("Enter updated information for user (" + email + "): ");
            System.out.print("New Password: ");
            String newPassword = scanner.nextLine();
            System.out.print("New Name: ");
            String newName = scanner.nextLine();

            user.setPassword(hashPassword(newPassword));
            user.setName(newName);

            write();
            System.out.println("User (" + email + ") has been updated.");
        } else {
            System.out.println("User (" + email + ") not found.");
        }
    }

    public void adminPanel(UserAuthentication userAuth) {
        AdminPanel admin = new AdminPanel(userAuth);
        while (true) {
            System.out.println("-----------------------------");
            System.out.println("1. List Users");
            System.out.println("2. Disqualify User");
            System.out.println("3. Add User");
            System.out.println("4. Remove User");
            System.out.println("5. Update User Information");
            System.out.println("6. Exit");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            System.out.println("-----------------------------");
            scanner.nextLine(); // Consume the newline character after reading the choice

            switch (choice) {
                case 1 -> admin.listUsers();
                case 2 -> {
                    System.out.print("Enter the email of the user to disqualify: ");
                    String userEmail = scanner.nextLine();
                    admin.disqualifyUser(userEmail);
                }
                case 3 -> {
                    System.out.print("Enter the email of the new user: ");
                    String email = scanner.nextLine();
                    System.out.print("Enter the password of the new user: ");
                    String password = hashPassword(scanner.nextLine());
                    System.out.print("Enter the name of the new user: ");
                    String name = scanner.nextLine();
                    register(email, password, name);
                }
                case 4 -> {
                    System.out.print("Enter the email of the user to be removed: ");
                    String userEmail = scanner.nextLine();
                    admin.removeUser(userEmail);
                }
                case 5 -> {
                    System.out.print("Enter the email of the user to be updated: ");
                    String userEmail = scanner.nextLine();
                    admin.updateUser(userEmail);
                }
                case 6 -> {
                    System.out.println("Exiting Admin Panel...");
                    return;
                }
                default -> {
                    System.out.println("Invalid choice. Please try again.");
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        UserAuthentication userAuth = new UserAuthentication();
        API api = new API();
        List<User> test = userAuth.getUser();
        System.out.println(test);
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to the Application!");
        while (true) {
            System.out.println("-----------------------------");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();
            System.out.println("-----------------------------");
            switch (choice) {
                case 1 -> {
                    System.out.print("Registration\nEmail: ");
                    String email = scanner.nextLine();
                    System.out.println("Your password should contain at least one uppercase letter, one lowercase letter, one digit and minimum length of 8 characters.");
                    System.out.print("Password: ");
                    String password = scanner.nextLine();
                    System.out.print("Name: ");
                    String name = scanner.nextLine();
                    userAuth.register(email, password, name);
                }
                case 2 -> {
                    System.out.print("Login\nEmail: ");
                    String email = scanner.nextLine();
                    System.out.print("Password: ");
                    String password = scanner.nextLine();
                    if (email.equals(userAuth.ADMIN_EMAIL) && password.equals(userAuth.ADMIN_PASSWORD)) {
                        System.out.println("\nWelcome to Admin Panel");
                        userAuth.adminPanel(userAuth);
                    } else if (userAuth.login(email, password)) {
                        User user = userAuth.getUsers().get(email);
                        // Create a list of stocks

                        TradingEngine tradingEngine = new TradingEngine();
                        // Create a portfolio for the user
                        Portfolio portfolio = new Portfolio();

                        if (tradingEngine.isWithinTradingHours()) {
                            userAuth.loopTrade(api.extractStocks() ,portfolio,user,tradingEngine);
                        } else {
                            System.out.println("Trading is currently closed. Orders cannot be executed outside trading hours.");
                        }
                    }
                }
                case 3 -> {
                    System.out.println("Exiting...");
                    System.out.println("-----------------------------");
                    return;
                }
                default -> {
                    System.out.println("Invalid choice. Please try again.");
                }

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