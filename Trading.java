//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Scanner;
//
//public class Trading {
//    public static void main(String[] args) {
//        // Create a list of stocks
//        List<Stock> stocks = new ArrayList<>();
//        stocks.add(new Stock("AAPL", "Apple Inc.", 1500.0));
//        stocks.add(new Stock("GOOG", "Alphabet Inc.", 2500.0));
//
//        // Create a trading engine with the list of stocks
//        TradingEngine tradingEngine = new TradingEngine(stocks);
//
//        // Create a portfolio for the user
//        Portfolio portfolio = new Portfolio();
//        //Create a user
//
//        // Example usage
//
//        Scanner scanner = new Scanner(System.in);
//
//        if(tradingEngine.isWithinTradingHours()) {
//            // Choose between buying or selling
//            System.out.println("1. Buy or sell stock \n2. Show current stock owned \n3. Cancel pending orders \n4. Close market");
//            int choice = scanner.nextInt();
//
//            if (choice == 1) {
//                System.out.println("1. Buy order \n2. Sell order");
//                choice = scanner.nextInt();
//                if (choice == 1) {
//                    // Place a buy order
//                    System.out.println("Enter stock symbol for buy order: ");
//                    String buyStockSymbol = scanner.next();
//                    // Find the stock by symbol
//                    Stock buyStock = findStockBySymbol(stocks, buyStockSymbol);
//                    while (buyStock == null) {
//                        System.out.println("Stock with symbol " + buyStockSymbol + " not found. Please enter a new stock symbol: ");
//                        buyStockSymbol = scanner.next();
//                        buyStock = findStockBySymbol(stocks, buyStockSymbol);
//                    }
//
//                    System.out.println("Enter quantity for buy order: ");
//                    int buyQuantity = scanner.nextInt();
//                    if (buyQuantity < 100) {
//                        System.out.println("Minimum order quantity is 100 shares (one lot).");
//                        return;
//                    }
//
//                    // Display suggested price for a stock
//                    tradingEngine.displaySuggestedPrice(buyStockSymbol);
//
//                    System.out.println("Enter expected buying price: ");
//                    double buyExpectedPrice = scanner.nextDouble();
//
//                    buyStock = findStockBySymbol(stocks, buyStockSymbol);
//                    if (buyStock != null) {
//                        Order buyOrder = new Order(buyStock, Order.Type.BUY, buyQuantity, 0.0, buyExpectedPrice, 0.0, User user);
//                        tradingEngine.executeOrder(buyOrder, portfolio);
//                    } else {
//                        System.out.println("Stock with symbol " + buyStockSymbol + " not found.");
//                    }
//
//                } else if (choice == 2) {
//                    // Place a sell order
//                    System.out.println("Enter stock symbol for sell order: ");
//                    String sellStockSymbol = scanner.next();
//                    // Find the stock by symbol
//                    Stock sellStock = findStockBySymbol(stocks, sellStockSymbol);
//                    while (sellStock == null) {
//                        System.out.println("Stock with symbol " + sellStockSymbol + " not found. Please enter a new stock symbol: ");
//                        sellStockSymbol = scanner.next();
//                        sellStock = findStockBySymbol(stocks, sellStockSymbol);
//                    }
//
//                    System.out.println("Enter quantity for sell order: ");
//                    int sellQuantity = scanner.nextInt();
//                    System.out.println("Enter expected selling price: ");
//                    double sellExpectedPrice = scanner.nextDouble();
//
//                    // Display suggested price for a stock
//                    tradingEngine.displaySuggestedPrice(sellStockSymbol);
//
//                    sellStock = findStockBySymbol(stocks, sellStockSymbol);
//                    if (sellStock != null) {
//                        Order sellOrder = new Order(sellStock, Order.Type.SELL, sellQuantity, 0.0, 0.0, sellExpectedPrice,User user);
//                        tradingEngine.executeOrder(sellOrder, portfolio);
//                    } else {
//                        System.out.println("Stock with symbol " + sellStockSymbol + " not found.");
//                    }
//                }
//            }else if(choice == 2){
//                //show current stock owned (trading dashboard)
//            }else if(choice == 3){
//                //cancelOrder method in TradingApp class
//            }else if(choice == 4){
//                tradingEngine.closeMarket(portfolio, portfolio.getValue());
//            }
//            else{
//                System.out.println("Execution invalid");
//            }
//        }else{
//            System.out.println("Trading is currently closed. Orders cannot be executed outside trading hours.");
//        }
//
//        scanner.close();
//    }
//
//    private static Stock findStockBySymbol(List<Stock> stocks, String symbol) {
//        for (Stock stock : stocks) {
//            if (stock.getSymbol().equalsIgnoreCase(symbol)) {
//                return stock;
//            }
//        }
//        return null;
//    }
//}
