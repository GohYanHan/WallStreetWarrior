import javax.swing.*;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class UserDashboard {
    private User user;

    public UserDashboard(User user) {
        this.user = user;
    }

    public void displayAccountBalance() {
        double accountBalance = user.getBalance();
        System.out.println("Account Balance: $" + accountBalance);
    }

    public void displayCurrentPoints() {
        double startingBalance = 50000.0; // Assuming a fixed starting balance

       //Overall(cumulative) account profit / loss  (account balance)

        //positive means profit, negative means loss

        double pAndL = user.getBalance() - startingBalance;
        double points = (pAndL / startingBalance) * 100;
        System.out.println("Current Points: " + points);
    }

    public void displayOpenPositions() {
        Portfolio portfolio = user.getPortfolio();
        portfolio.displayHoldings();
    }

    public void displayTradeHistory() {

        System.out.println("Trade History:");
        List<Order> tradeHistory = user.getPortfolio().getTradeHistory();

        if(!tradeHistory.isEmpty()){


        //   tradeHistory.sort(Comparator.comparing(Order::getExpectedBuyingPrice).thenComparing(Order::getTimestamp));

          //tradeHistory list will be sorted in ascending order first by expectedBuyingPrice,
            // and if there are elements with the same expectedBuyingPrice, those will be further sorted by timestamp.



        for (Order order : tradeHistory) {
            System.out.println("Stock: " + order.getStock().getSymbol());
            System.out.println("Type: " + order.getType());
            System.out.println("Shares: " + order.getShares());
            System.out.println("Price: $" + order.getExpectedBuyingPrice());
            System.out.println("Timestamp: " + order.getTimestamp());
            System.out.println("-".repeat(30));
             }
        }
        else
            System.out.println("No trade history");


    }

    public void displayStocksLeft() {
        Portfolio portfolio = user.getPortfolio();
        List<Order> tradeHistory = portfolio.getTradeHistory();

        if(!tradeHistory.isEmpty()) {
            System.out.println("Stocks Left:");

            for (Order order : tradeHistory) {
                if (!portfolio.containsStockSymbol(order.getStock().getSymbol())) {
                    System.out.println("Stock: " + order.getStock().getSymbol());
                    System.out.println("Name: " + order.getStock().getName());
                    System.out.println("Price: $" + order.getStock().getPrice());
                    System.out.println("-".repeat(30));
                }
            }
        }

        System.out.println("No stocks left.");


    }

    //lowest price to highest price
    public void sortTradeHistoryByPrice() {
        List<Order> tradeHistory = user.getPortfolio().getTradeHistory();
        tradeHistory.sort(Comparator.comparing(Order::getExpectedBuyingPrice));
    }


    //oldest to newest
    public void sortTradeHistoryByPlacementTime() {
        List<Order> tradeHistory = user.getPortfolio().getTradeHistory();

        tradeHistory.sort(Comparator.comparing(Order::getTimestamp));
    }

    public void chooseSort(){

        int i;

        Scanner k = new Scanner(System.in);
        System.out.println("1: Sort by price");
        System.out.println("2: Sort by placement time");

        i = k.nextInt();

        if(i == 1){
            sortTradeHistoryByPrice();
        }
        else if(i == 2){
            sortTradeHistoryByPlacementTime();
        }
        else{
            System.out.println("invalid choice, try again");
            chooseSort();
        }
    }

}


