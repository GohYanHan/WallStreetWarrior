import java.time.LocalDateTime;
import java.util.*;

public class TradingApp {
    private List<User> users;
    private TradingEngine tradingEngine;
    private List<Order> pendingOrders;
    private PriorityQueue<Order> pendingOrdersByTime;
    private PriorityQueue<Order> pendingOrdersByPrice;

    public TradingApp(List<User> users, TradingEngine tradingEngine) {
        this.users = users;
        this.tradingEngine = tradingEngine;
        this.pendingOrders = new ArrayList<>();
        this.pendingOrdersByTime = new PriorityQueue<>(Comparator.comparing(Order::getTimestamp));
        this.pendingOrdersByPrice = new PriorityQueue<>(Comparator.comparingDouble(Order::getPrice).reversed());
    }


    public User login(String email, String password) {
        for (User user : users) {
            if (user.getEmail().equals(email) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    public void placeOrder(User user, Order order) {
        order.setTimestamp(LocalDateTime.now());
        order.setUser(user); // Set the user who placed the order
        tradingEngine.executeOrder(order, user.getPortfolio());
        pendingOrders.add(order); // if placed order fulfill the conditions, order is executed
        System.out.println("Order added into pending list");
        autoMatching(order.getStock(), order.getShares(), order.getUser());
        pendingOrdersByTime.offer(order);
        pendingOrdersByPrice.offer(order);
    }

    public void cancelOrder(User user) {
        System.out.println("Choose cancellation option:\n1. Cancel by longest time\n2. Cancel by highest price");
        Scanner scanner = new Scanner(System.in);
        int option = scanner.nextInt();

        switch (option) {
            case 1:
                cancelOrderByTime(user);
                break;
            case 2:
                cancelOrderByPrice(user);
                break;
            default:
                System.out.println("Invalid option. Please try again.");
        }
    }

    private void cancelOrderByTime(User user) {
        if (pendingOrdersByTime.isEmpty()) {
            System.out.println("No pending orders to cancel.");
            return;
        }

        Order order = pendingOrdersByTime.poll();
        if (order != null && order.getUser() == user) {
            boolean removed = pendingOrders.remove(order);
            if (removed) {
                System.out.println("Order canceled successfully: " + order);
            } else {
                System.out.println("Failed to cancel the order: " + order);
            }
        } else {
            System.out.println("You don't have any pending orders to cancel.");
        }
    }

    private void cancelOrderByPrice(User user) {
        if (pendingOrdersByPrice.isEmpty()) {
            System.out.println("No pending orders to cancel.");
            return;
        }

        Order order = pendingOrdersByPrice.poll();
        if (order != null && order.getUser() == user) {
            boolean removed = pendingOrders.remove(order);
            if (removed) {
                System.out.println("Order canceled successfully: " + order);
            } else {
                System.out.println("Failed to cancel the order: " + order);
            }
        } else {
            System.out.println("You don't have any pending orders to cancel.");
        }
    }

    public void autoMatching(Stock buyStock, int buyQuantity, User buyer) {
        List<Order> sellOrders = new ArrayList<>();

        // Find sell orders that match the buy stock
        for (Order order : pendingOrders) {
            if (order.getType() == Order.Type.SELL && order.getStock().equals(buyStock)) {
                sellOrders.add(order);
            }
        }

        // Find a matching sell order with sufficient quantity
        for (Order sellOrder : sellOrders) {
            if (sellOrder.getShares() >= buyQuantity) {
                // Create a new order for the matched trade
                double price = sellOrder.getPrice();
                double expectedBuyingPrice = buyStock.getPrice();
                double expectedSellingPrice = sellOrder.getExpectedSellingPrice();
                User seller = sellOrder.getUser();

                Order matchedOrder = new Order(buyStock, Order.Type.BUY, buyQuantity, price, expectedBuyingPrice, expectedSellingPrice, buyer);
                matchedOrder.setTimestamp(LocalDateTime.now());

                // Execute the matched trade and update portfolios
                tradingEngine.executeOrder(matchedOrder, buyer.getPortfolio());
                tradingEngine.executeOrder(sellOrder, seller.getPortfolio());

                // Remove the matched sell order from the pending orders
                pendingOrders.remove(sellOrder);
                pendingOrdersByTime.remove(sellOrder);
                pendingOrdersByPrice.remove(sellOrder);

                System.out.println("Trade executed successfully: " + matchedOrder);
                return;
            }
        }

        System.out.println("No matching sell orders found for the buy order: " + buyStock + ", Quantity: " + buyQuantity);
    }
}

