import java.util.*;

public class UserDashboard {
    private User user;
    private Database db;


    public UserDashboard(User user) {
        this.user = user;
        db = new Database();
    }

    public void displayAccountBalance() {
        double accountBalance = user.getPortfolio().getAccBalance();
        System.out.println("Account Balance: RM" + accountBalance);
    }


    public void calculatePLPoints(User user) {
        List<Order> tradeHistory = db.loadTransactionHistory(user.getKey());

        double totalProfitAndLoss = 0.0;

        // Create a map to keep track of the remaining shares for each stock
        Map<String, Integer> remainingShares = new HashMap<>();

        // Create a copy of the tradeHistory list to remove sell orders from
        List<Order> remainingOrders = new ArrayList<>(tradeHistory);

        for (Order order : tradeHistory) {
            if (order.getType() == Order.Type.BUY) {
                // Update the remaining shares for the stock being bought
                remainingShares.put(order.getStock().getSymbol(), order.getShares());
            }
        }

        for (Order order : tradeHistory) {
            if (order.getType() == Order.Type.SELL) {
                // Get the remaining shares for the stock being sold
                int remainingQuantity = remainingShares.getOrDefault(order.getStock().getSymbol(), 0);

                // Check if the remaining quantity is greater than or equal to the sell order quantity
                while (remainingQuantity >= order.getShares()) {
                    // Calculate profit or loss based on the sell order
                    double profitOrLoss = (order.getExpectedSellingPrice() - order.getExpectedBuyingPrice());
                    totalProfitAndLoss += profitOrLoss;

                    // Update the remaining shares for the stock being sold
                    remainingShares.put(order.getStock().getSymbol(), remainingQuantity - order.getShares());

                    // Remove the sell order from the remaining orders list
                    remainingOrders.remove(order);

                    // Check if the remaining quantity is less than 100, remove the stock from the searching list
                    if (remainingQuantity - order.getShares() < 100) {
                        remainingShares.remove(order.getStock().getSymbol());
                    }

                    // Find the next matching sell order, if any
                    Order nextSellOrder = findMatchingSellOrder(order, remainingOrders);
                    if (nextSellOrder != null) {
                        order = nextSellOrder;
                        remainingQuantity = remainingShares.getOrDefault(order.getStock().getSymbol(), 0);
                    } else {
                        break; // No more matching sell orders, exit the loop
                    }
                }
            }
        }

        double startingBalance = 50000.0; // Assuming a fixed starting balance
        Map<Integer, Double> plPoints = db.loadPLpoint();
        Double points = plPoints.get(user.getKey());
        points += ((totalProfitAndLoss / startingBalance) * 100);
        db.updateUserPLpoint(user.getKey(), points);

    }

    private Order findMatchingSellOrder(Order buyOrder, List<Order> remainingOrders) {
        for (Order order : remainingOrders) {
            if (order.getType() == Order.Type.SELL && order.getStock().getSymbol().equals(buyOrder.getStock().getSymbol())) {
                return order;
            }
        }
        return null; // No matching sell order found
    }

    public void displayCurrentPoints() {
        System.out.println("Current Points: " + db.loadPLpoint().get(user.getKey()));
    }


    public void displayOpenPositions() {
        user.getPortfolio().displayHoldings();
    }

    public void displayTradeHistory(List<Order> tradeHistory) {
        if (!tradeHistory.isEmpty()) {
            //   tradeHistory.sort(Comparator.comparing(Order::getExpectedBuyingPrice).thenComparing(Order::getTimestamp));
            //tradeHistory list will be sorted in ascending order first by expectedBuyingPrice, and if there are elements with the same expectedBuyingPrice, those will be further sorted by timestamp.
            System.out.println("===========================================================================================");
            System.out.println("|                                Trade History                                            |");
            System.out.println("===========================================================================================");
            int tradeHistorySize = tradeHistory.size(); // Get the size of the tradeHistory list

            // Iterate through the tradeHistory list and print each order
            for (int i = 0; i < tradeHistorySize; i++) {
                Order order = tradeHistory.get(i);

                System.out.println("| Stock     : " + padRight(order.getStock().getSymbol(), 75) + " |");
                System.out.println("| Name      : " + padRight(order.getStock().getName(), 75) + " |");
                System.out.println("| Type      : " + padRight(order.getType().toString(), 75) + " |");
                System.out.println("| Shares    : " + padRight(String.valueOf(order.getShares()), 75) + " |");

                if (order.getType() == Order.Type.BUY)
                    System.out.println("| Price     : RM " + padRight(String.valueOf(order.getExpectedBuyingPrice()), 72) + " |");
                else
                    System.out.println("| Price     : RM " + padRight(String.valueOf(order.getExpectedSellingPrice()), 72) + " |");

                System.out.println("| Timestamp : " + padRight(order.getTimestamp().toString(), 75) + " |");

                if (i != tradeHistorySize - 1)
                    System.out.println("|-----------------------------------------------------------------------------------------|");
            }
            // Print the closing line
            System.out.println("===========================================================================================");
        }
        else System.out.println("No trade history");
    }

    private static String padRight(String s, int length) {
        return String.format("%-" + length + "s", s);
    }

    //lowest price to highest price
    public void sortTradeHistoryByPrice() {
        List<Order> tradeHistoryByPrice = db.loadTransactionHistory(user.getKey());
        tradeHistoryByPrice.sort(Comparator.comparingDouble(order -> {
            if (order.getType() == Order.Type.BUY) {
                return order.getExpectedBuyingPrice();
            } else {
                return order.getExpectedSellingPrice();
            }
        }));
        displayTradeHistory(tradeHistoryByPrice); // Pass the sorted list to the display method
    }


    //oldest to newest
    public void sortTradeHistoryByPlacementTime() {
        List<Order> tradeHistoryByPlacementTime = db.loadTransactionHistory(user.getKey());
        tradeHistoryByPlacementTime.sort(Comparator.comparing(Order::getTimestamp));
        displayTradeHistory(tradeHistoryByPlacementTime); // Pass the sorted list to the display method
    }

    public void chooseSort() {
        System.out.println("Trade History: ");
        int i;
        Scanner k = new Scanner(System.in);
        System.out.println("1: Sort by price");
        System.out.println("2: Sort by placement time");

        i = k.nextInt();

        if (i == 1) {
            sortTradeHistoryByPrice();
        } else if (i == 2) {
            sortTradeHistoryByPlacementTime();
        } else {
            System.out.println("invalid choice, try again");
            chooseSort();
        }
    }

}
