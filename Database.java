//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDateTime;
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

    public boolean storeLotPool(Stock stock, int share) {
        Map<String, Integer> dbAPI = new HashMap<>();
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

    public boolean addHoldings(int userKey, Stock stock, int share) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "INSERT INTO holdings (userKey, symbol, share) VALUES (?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userKey);
            statement.setString(2, stock.getSymbol());
            statement.setInt(3, share);

            int rowsInserted = statement.executeUpdate();
            statement.close();

            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Map<Order, Integer> loadHolding(int userKey) {
        Map<Order, Integer> holding = new HashMap<>();
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "SELECT userKey, symbol, share FROM holdings WHERE userKey = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userKey);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Order stock = new Order(userKey, new Stock(resultSet.getString("symbol")));
                holding.put(stock, resultSet.getInt("share"));
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return holding;
    }

    boolean removeHolding(int userKey, String symbol) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "DELETE FROM holdings WHERE userKey = ? AND symbol = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userKey);
            statement.setString(2, symbol);

            int rowsAffected = statement.executeUpdate();
            statement.close();

            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateHolding(int userKey, String symbol, int updateShares) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "UPDATE holdings SET share = ? WHERE userKey = ? AND symbol = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, updateShares);
            statement.setInt(2, userKey);
            statement.setString(3, symbol);
            int rowsUpdated = statement.executeUpdate();
            statement.close();

            // Check if any rows were updated
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addOrder(int userKey, String symbol, int share, double price, LocalDateTime time, Order.Type type) {
        String sql = "INSERT INTO `order` (userKey, symbol, share, price, time, type) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userKey);
            statement.setString(2, symbol);
            statement.setInt(3, share);
            statement.setDouble(4, price);
            statement.setTimestamp(5, java.sql.Timestamp.valueOf(time));
            statement.setString(6, type.name());

            int rowsInserted = statement.executeUpdate();
            statement.close();

            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Order> loadOrders(int userKey, Order.Type type) {
        List<Order> list = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "";
            PreparedStatement statement;
            ResultSet resultSet;
            //load buy orders of current user
            if (type.equals(Order.Type.BUY)) {
                sql = "SELECT userKey, symbol, share, price, time FROM `order` WHERE userKey = ? AND type = ?";
                statement = connection.prepareStatement(sql);
                statement.setInt(1, userKey);
                statement.setString(2, type.name());
                resultSet = statement.executeQuery();

            } else {
                //load all sell orders by all users
                sql = "SELECT userKey, symbol, share, price, time FROM `order` WHERE type = ?";
                statement = connection.prepareStatement(sql);
                statement.setString(1, type.name());
                resultSet = statement.executeQuery();
            }

            while (resultSet.next()) {
                list.add(new Order(resultSet.getInt("userKey"), new Stock(resultSet.getString("symbol")),
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

    boolean removeOrder(int userKey, String symbol, int share, Order.Type type) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "DELETE FROM `order` WHERE userKey = ? AND symbol = ? AND share = ? AND type = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userKey);
            statement.setString(2, symbol);
            statement.setInt(3, share);
            statement.setString(4, type.name());

            int rowsAffected = statement.executeUpdate();
            statement.close();

            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addTransactionHistory(int userKey, String symbol, int share, double price, LocalDateTime time, Order.Type type) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "INSERT INTO history (userKey, symbol, share, price, time, type) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userKey);
            statement.setString(2, symbol);
            statement.setInt(3, share);
            statement.setDouble(4, price);
            statement.setTimestamp(5, java.sql.Timestamp.valueOf(time));
            statement.setString(6, type.name());

            int rowsInserted = statement.executeUpdate();
            statement.close();

            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Order> loadTransactionHistory(int userKey) {
        List<Order> list = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String selectQuery = "SELECT userKey, symbol, share, price, time, type FROM history WHERE userKey = ?";

            PreparedStatement statement = connection.prepareStatement(selectQuery);
            statement.setInt(1, userKey);
            ResultSet resultSet = statement.executeQuery();

//            while (resultSet.next()) {
//                list.add(new Order(resultSet.getInt("userKey"), resultSet.getString("symbol"),
//                        resultSet.getInt("share"), resultSet.getDouble("price"),
//                        resultSet.getTimestamp("time").toLocalDateTime()), Order.Type.valueOf(resultSet.getString("type")));
//            }
            while (resultSet.next()) {
                String typeStr = resultSet.getString("type");
                // Convert the type string to the enum type
                Order.Type type = Order.Type.valueOf(typeStr);

                list.add(new Order(resultSet.getInt("userKey"), new Stock(resultSet.getString("symbol")), resultSet.getInt("share"),
                        resultSet.getDouble("price"), resultSet.getTimestamp("time").toLocalDateTime(), type));
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

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
            System.out.println("This email has already registered.");
            return false;
        }
    }

    public User loadUser(String inputEmail) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "SELECT userKey, userEmail, userName, userPassword, userStatus, userBalance, PL_Points, role FROM users WHERE userEmail = ?";

            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, inputEmail);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                user = (new User(resultSet.getInt("userKey"), resultSet.getString("userEmail"), resultSet.getString("userName"),
                        resultSet.getString("userPassword"), resultSet.getString("userStatus"), resultSet.getDouble("userBalance"),
                        resultSet.getInt("PL_Points"), resultSet.getString("role")));
//                if (user.getRole().equals("Admin")) {
////                    dk wht to do
//                } else if (user.getRole().equals("User")) {
//                    user.setBalance(resultSet.getDouble("userBalance"));
//                    user.setPL_Points(resultSet.getInt("PL_Points"));
//                }
                return user;
            }

            resultSet.close();
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

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

    boolean updateUserPLpoint(int userKey, int pl_points) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "UPDATE users SET PL_Points = ? WHERE userKey = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, pl_points);
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

    boolean updateUserThresholds(int userKey, int thresholds) {
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

    public List<User> getUsersList() {
        List<User> list = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "SELECT userKey, userName, userEmail, userStatus, userBalance, PL_Points, role FROM users WHERE role = \"User\"";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                list.add(new User(resultSet.getString("userEmail"), resultSet.getString("userName"),
                        resultSet.getString("userStatus"), resultSet.getInt("userBalance"),
                        resultSet.getInt("PL_Points"), resultSet.getInt("userKey")));
            }
            resultSet.close();
            statement.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

//    public ObservableList<User> displayUsers() {
//        ObservableList<User> list = FXCollections.observableArrayList();
//
//        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
//
//            String selectQuery = "SELECT userKey, userName, userEmail, userPassword, userStatus, userBalance, PL_Points, role FROM users WHERE role = User";
//
//            PreparedStatement statement = connection.prepareStatement(selectQuery);
//            ResultSet resultSet = statement.executeQuery();
//
//            while (resultSet.next()) {
//                list.add(new User(resultSet.getString("userEmail"), resultSet.getString("userName"),
//                        resultSet.getString("userStatus"), resultSet.getInt("userBalance"),
//                        resultSet.getInt("PL_Points"), resultSet.getInt("userKey")));
//            }
//            resultSet.close();
//            statement.close();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return list;
//    }
}