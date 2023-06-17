import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/wsw1";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "abc123";
    private static User user;

    public Database() {

    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        Database.user = user;
    }

    // Store only the remaining stock after bought by user in lot pool to database
    public boolean storeLotPool(Stock stock, int share) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "INSERT INTO lotpool (symbol, name, share) VALUES (?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, stock.getSymbol());
            statement.setString(2, stock.getName());
            statement.setInt(3, share);

            int rowsInserted = statement.executeUpdate();
            statement.close();

            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get the remaining stock in lot pool from database
    public Map<Stock, Integer> getLotPool() {
        Map<Stock, Integer> lotpool = new HashMap<>();
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "SELECT * FROM lotpool";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Stock stock = new Stock(resultSet.getString("symbol"), resultSet.getString("name"));
                lotpool.put(stock, resultSet.getInt("share"));
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lotpool;
    }

    // Replenish the lot pool to initial
    public boolean refreshLotPool() {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "DELETE FROM lotpool";
            PreparedStatement statement = connection.prepareStatement(sql);
            int rowsAffected = statement.executeUpdate();

            statement.close();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Update lot pool (shares) when buying the same stock
    boolean updateLotPool(Stock stock, int updateShares) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "UPDATE lotpool SET share = ? WHERE symbol = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, updateShares);
            statement.setString(2, stock.getSymbol());
            int rowsUpdated = statement.executeUpdate();
            statement.close();

            // Check if any rows were updated
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // Add holdings of users into database
    boolean addHoldings(int userKey, Stock stock, int share) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "INSERT INTO holdings (userKey, symbol, name, share) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userKey);
            statement.setString(2, stock.getSymbol());
            statement.setString(3, stock.getName());
            statement.setInt(4, share);

            int rowsInserted = statement.executeUpdate();
            statement.close();

            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Load users holdings from database
    public Map<Order, Integer> loadHolding(int userKey) {
        Map<Order, Integer> holding = new HashMap<>();
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "SELECT * FROM holdings WHERE userKey = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userKey);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Order stock = new Order(userKey, new Stock(resultSet.getString("symbol"), resultSet.getString("name")));
                holding.put(stock, resultSet.getInt("share"));
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return holding;
    }

    // Remove holdings from users in database
    boolean removeHolding(int userKey, Stock stock) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "DELETE FROM holdings WHERE userKey = ? AND symbol = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userKey);
            statement.setString(2, stock.getSymbol());

            int rowsAffected = statement.executeUpdate();
            statement.close();

            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Update user holding (shares) when buying the same stock
    boolean updateHolding(int userKey, Stock stock, int updateShares) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "UPDATE holdings SET share = ? WHERE userKey = ? AND symbol = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, updateShares);
            statement.setInt(2, userKey);
            statement.setString(3, stock.getSymbol());
            int rowsUpdated = statement.executeUpdate();
            statement.close();

            // Check if any rows were updated
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Add users order into database (Type is BUY or SELL)
    public boolean addOrder(int userKey, Order order) {
        String sql = "INSERT INTO `order` (userKey, symbol, name, share, price, time, type) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userKey);
            statement.setString(2, order.getStock().getSymbol());
            statement.setString(3, order.getStock().getName());
            statement.setInt(4, order.getShares());
            if (order.getType().equals(Order.Type.BUY))
                statement.setDouble(5, order.getExpectedBuyingPrice());
            else
                statement.setDouble(5, order.getExpectedSellingPrice());

            statement.setTimestamp(6, java.sql.Timestamp.valueOf(order.getTimestamp()));
            statement.setString(7, order.getType().name());

            int rowsInserted = statement.executeUpdate();
            statement.close();

            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Load users order from database (Type is BUY or SELL)
    public List<Order> loadOrders(int userKey, Order.Type type) {
        List<Order> list = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "";
            PreparedStatement statement;
            ResultSet resultSet;
            //load buy orders of current user
            if (type.equals(Order.Type.BUY)) {
                sql = "SELECT orderID, userKey, symbol, name, share, price, time FROM `order` WHERE userKey = ? AND type = ?";
                statement = connection.prepareStatement(sql);
                statement.setInt(1, userKey);
                statement.setString(2, type.name());
                resultSet = statement.executeQuery();

            } else {
                //load all sell orders by all users
                sql = "SELECT orderID, userKey, symbol, name, share, price, time FROM `order` WHERE type = ?";
                statement = connection.prepareStatement(sql);
                statement.setString(1, type.name());
                resultSet = statement.executeQuery();
            }

            while (resultSet.next()) {
                list.add(new Order(resultSet.getInt("orderID"),user,
                        new Stock(resultSet.getString("symbol"), resultSet.getString("name")),
                        resultSet.getInt("share"), resultSet.getDouble("price"),
                        resultSet.getTimestamp("time").toLocalDateTime(), type));
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Cancel users order from database (Type is BUY or SELL)
    boolean removeOrder(int orderID) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "DELETE FROM `order` WHERE orderID = ? ";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, orderID);

            int rowsAffected = statement.executeUpdate();
            statement.close();

            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Add users transaction history into database (Type is BUY or SELL)
    public boolean addTransactionHistory(int userKey, Order order) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "INSERT INTO history (userKey, symbol, name, share, price, time, type) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userKey);
            statement.setString(2, order.getStock().getSymbol());
            statement.setString(3, order.getStock().getName());
            statement.setInt(4, order.getShares());
            if (order.getType().equals(Order.Type.BUY))
                statement.setDouble(5, order.getExpectedBuyingPrice());
            else
                statement.setDouble(5, order.getExpectedSellingPrice());
            statement.setTimestamp(6, java.sql.Timestamp.valueOf(order.getTimestamp()));
            statement.setString(7, order.getType().name());

            int rowsInserted = statement.executeUpdate();
            statement.close();

            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Load users transaction history from database (Type is BUY or SELL)
    public List<Order> loadTransactionHistory(int userKey) {
        List<Order> list = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String selectQuery = "SELECT * FROM history WHERE userKey = ?";

            PreparedStatement statement = connection.prepareStatement(selectQuery);
            statement.setInt(1, userKey);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String typeStr = resultSet.getString("type");
                // Convert the type string to the enum type
                Order.Type type = Order.Type.valueOf(typeStr);


                list.add(new Order(-1,user, new Stock(resultSet.getString("symbol"),
                        resultSet.getString("name")), resultSet.getInt("share"),
                        resultSet.getDouble("price"), resultSet.getTimestamp("time").toLocalDateTime(), type));
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Add users into database
    public boolean addUser(String email, String hashedPassword, String username) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "INSERT INTO users (userEmail, userPassword, userName) VALUES (?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setString(1, email);
            statement.setString(2, hashedPassword);
            statement.setString(3, username);

            int rowsInserted = statement.executeUpdate();
            statement.close();

            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("This email has already registered.");
            return false;
        }
    }

    // Get user by email from database
    public User loadUserByEmail(String inputEmail) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "SELECT * FROM users WHERE userEmail = ?";

            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, inputEmail);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                user = (new User(resultSet.getInt("userKey"), resultSet.getString("userEmail"), resultSet.getString("userName"),
                        resultSet.getString("userPassword"), resultSet.getString("userStatus"), resultSet.getDouble("userBalance"),
                        resultSet.getDouble("PL_Points"), resultSet.getString("role"), resultSet.getDouble("thresholds"), resultSet.getBoolean("isNotified")));
                return user;
            }

            resultSet.close();
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Get user by key from database
    public User loadUserByKey(int key) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "SELECT * FROM users WHERE userKey = ?";

            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, key);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                user = (new User(resultSet.getInt("userKey"), resultSet.getString("userEmail"), resultSet.getString("userName"),
                        resultSet.getString("userPassword"), resultSet.getString("userStatus"), resultSet.getDouble("userBalance"),
                        resultSet.getDouble("PL_Points"), resultSet.getString("role"), resultSet.getDouble("thresholds"), resultSet.getBoolean("isNotified")));
                return user;
            }

            resultSet.close();
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Remove users from database
    boolean removeUser(String email) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "DELETE FROM users WHERE userEmail = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, email);
            int rowsAffected = statement.executeUpdate();
            statement.close();

            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // User not found or failed to removed
        }
    }

    // Update users PL points into database
    boolean updateUserPLpoint(int userKey, double pl_points) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "UPDATE users SET PL_Points = ? WHERE userKey = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setDouble(1, pl_points);
            statement.setInt(2, userKey);
            // Execute the update statement
            int rowsUpdated = statement.executeUpdate();
            statement.close();

            // Check if any rows were updated
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Load users PL points from database
    public Map<Integer, Double> loadPLpoint() {
        Map<Integer, Double> plPoints = new HashMap<>();
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "SELECT PL_Points, userKey FROM users";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                plPoints.put(resultSet.getInt("userKey"), resultSet.getDouble("PL_Points"));
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return plPoints;
    }

    // Update users thresholds into database
    boolean updateUserThresholds(int userKey, double thresholds) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "UPDATE users SET thresholds = ? WHERE userKey = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setDouble(1, thresholds);
            statement.setInt(2, userKey);
            // Execute the update statement
            int rowsUpdated = statement.executeUpdate();
            statement.close();

            // Check if any rows were updated
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update users balance into database
    boolean updateUserBalance(int userKey, double balance) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "UPDATE users SET userBalance = ? WHERE userKey = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setDouble(1, balance);
            statement.setInt(2, userKey);
            // Execute the update statement
            int rowsUpdated = statement.executeUpdate();
            statement.close();

            // Check if any rows were updated
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Reset users password into database
    boolean resetPassword(String email, String username, String newPassword) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "UPDATE users SET userPassword = ? WHERE userEmail = ? AND userName = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, newPassword);
            statement.setString(2, email);
            statement.setString(3, username);

            // Execute the update statement
            int rowsUpdated = statement.executeUpdate();
            statement.close();

            // Check if any rows were updated
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update username into database
    public boolean updateUsername(String email, String newUsername) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "UPDATE users SET userName = ? WHERE userEmail = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, newUsername);
            statement.setString(2, email);
            // Execute the update statement
            int rowsUpdated = statement.executeUpdate();
            statement.close();

            // Check if any rows were updated
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Disqualify users from database
    boolean disqualifyUser(String email) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "UPDATE users SET userStatus = ? WHERE userEmail = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, "Disqualified");
            statement.setString(2, email);
            // Execute the update statement
            int rowsUpdated = statement.executeUpdate();
            statement.close();

            // Check if any rows were updated
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get list of users (including admins) from database
    public List<User> getUsersList() {
        List<User> list = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "SELECT * FROM users";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                list.add(new User(resultSet.getInt("userKey"), resultSet.getString("userEmail"),
                        resultSet.getString("userName"), resultSet.getString("userPassword"),
                        resultSet.getString("userStatus"), resultSet.getDouble("userBalance"),
                        resultSet.getDouble("PL_Points"), resultSet.getString("role"),
                        resultSet.getDouble("thresholds"), resultSet.getBoolean("isNotified")));
            }
            resultSet.close();
            statement.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    //get all admin emails for notification. FraudDetection only
    public List<String> getAllAdminEmails() {
        List<String> adminEmails = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "SELECT userEmail FROM users WHERE role = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, "admin");
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                adminEmails.add(resultSet.getString("userEmail"));
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return adminEmails;
    }

    //for FraudDetection, to ensure no duplication of notifications of same suspicious user
    public boolean getUserFDNotificationStatus(int userKey) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "SELECT isNotified FROM users WHERE userKey = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userKey);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                boolean isNotified = resultSet.getBoolean("isNotified");
                resultSet.close();
                statement.close();
                return isNotified;
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Return default value if user is not found or an error occurs
    }

    public boolean setUserFDNotificationStatus(int userKey) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "UPDATE users SET isNotified = ? WHERE userKey = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setBoolean(1, true);
            statement.setInt(2, userKey);

            int rowsUpdated = statement.executeUpdate();
            statement.close();

            // Check if any rows were updated
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}