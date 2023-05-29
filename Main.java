//import java.util.ArrayList;
//import java.util.List;
//import java.util.Scanner;
//
//public class Main {
//    public static void main(String[] args) {
//        UserAuthentication userAuth = new UserAuthentication();
//        List<User> test = userAuth.getUser();
//        System.out.println(test);
//        Scanner scanner = new Scanner(System.in);
//
//        System.out.println("Welcome to the Application!");
//        while (true) {
//            System.out.println("-----------------------------");
//            System.out.println("1. Register");
//            System.out.println("2. Login");
//            System.out.println("3. Exit");
//            System.out.print("Enter your choice: ");
//            int choice = scanner.nextInt();
//            scanner.nextLine();
//            System.out.println("-----------------------------");
//            switch (choice) {
//                case 1 -> {
//                    System.out.print("Registration\nEmail: ");
//                    String email = scanner.nextLine();
//                    System.out.println("Your password should contain at least one uppercase letter, one lowercase letter, one digit and minimum length of 8 characters.");
//                    System.out.print("Password: ");
//                    String password = scanner.nextLine();
//                    System.out.print("Name: ");
//                    String name = scanner.nextLine();
//                    userAuth.register(email, password, name);
//                }
//                case 2 -> {
//                    System.out.print("Login\nEmail: ");
//                    String email = scanner.nextLine();
//                    System.out.print("Password: ");
//                    String password = scanner.nextLine();
//                    if (email.equals(userAuth.getADMIN_EMAIL()) && password.equals(userAuth.getADMIN_PASSWORD())) {
//                        System.out.println("\nWelcome to Admin Panel");
//                        userAuth.adminPanel(userAuth);
//                    } else if (userAuth.login(email, password)) {
//                        User user = userAuth.getUsers().get(email);
//                        // Create a list of stocks
//                        List<Stock> stocks = new ArrayList<>();
//                        stocks.add(new Stock("AAPL", "Apple Inc.", 1500.0));
//                        stocks.add(new Stock("GOOG", "Alphabet Inc.", 2500.0));
//                        // Create a trading engine with the list of stocks
//                        TradingEngine tradingEngine = new TradingEngine(stocks);
//                        //updatePrice here??
//                        // Create a portfolio for the user
//                        Portfolio portfolio = new Portfolio();
//
////                        if (tradingEngine.isWithinTradingHours()) {
//                        userAuth.loopTrade(stocks,portfolio,user,tradingEngine);
//                    } else {
//                        System.out.println("Trading is currently closed. Orders cannot be executed outside trading hours.");
//                    }
//                }
////                }
//                case 3 -> {
//                    System.out.println("Exiting...");
//                    System.out.println("-----------------------------");
//                    return;
//                }
//                default -> {
//                    System.out.println("Invalid choice. Please try again.");
//                }
//
//            }
//        }
//    }
//}
