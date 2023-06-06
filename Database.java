//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;

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

    public boolean addOrder(int userKey, Order order) {
        String sql = "INSERT INTO `order` (userKey, symbol, share, price, time, type) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userKey);
            statement.setString(2, order.getStock().getSymbol());
            statement.setInt(3, order.getShares());
            statement.setDouble(4, order.getPrice());
            statement.setTimestamp(5, java.sql.Timestamp.valueOf(order.getTimestamp()));
            statement.setString(6, order.getType().name());

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

    boolean removeOrder(int userKey, Order order) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "DELETE FROM `order` WHERE userKey = ? AND symbol = ? AND share = ? AND type = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userKey);
            statement.setString(2, order.getStock().getSymbol());
            statement.setInt(3, order.getShares());
            statement.setString(4, order.getType().name());

            int rowsAffected = statement.executeUpdate();
            statement.close();

            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addTransactionHistory(int userKey, Order order) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "INSERT INTO history (userKey, symbol, name, share, price, time, type) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userKey);
            statement.setString(2, order.getStock().getSymbol());
            statement.setString(3, order.getStock().getName());
            statement.setInt(4, order.getShares());
            statement.setDouble(5, order.getPrice());
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

    public List<Order> loadTransactionHistory(int userKey) {
        List<Order> list = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String selectQuery = "SELECT * FROM history WHERE userKey = ?";

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


                list.add(new Order(resultSet.getInt("userKey"), new Stock(resultSet.getString("symbol"),
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

    public User loadUser(String inputEmail) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "SELECT * FROM users WHERE userEmail = ?";

            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, inputEmail);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                user = (new User(resultSet.getInt("userKey"), resultSet.getString("userEmail"), resultSet.getString("userName"),
                        resultSet.getString("userPassword"), resultSet.getString("userStatus"), resultSet.getDouble("userBalance"),
                        resultSet.getInt("PL_Points"), resultSet.getString("role"), resultSet.getDouble("thresholds")));
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

    public Map<Integer, Double> loadPLpoint() {
        Map<Integer, Double> plPoints = new HashMap<>();
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "SELECT PL_Points, userKey FROM users";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                plPoints.put(resultSet.getInt("userKey"),resultSet.getDouble("PL_Points"));
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return plPoints;
    }

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
            String sql = "SELECT userKey, userName, userEmail, userStatus, userBalance, PL_Points, thresholds FROM users WHERE role = \"User\"";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                list.add(new User(resultSet.getString("userEmail"), resultSet.getString("userName"),
                        resultSet.getString("userStatus"), resultSet.getInt("userBalance"),
                        resultSet.getInt("PL_Points"), resultSet.getInt("userKey"), resultSet.getDouble("thresholds")));
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