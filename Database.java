//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    public boolean addUser(String email, String hashedPassword, String username) {
        String sql = "INSERT INTO users (userEmail, userPassword, userName) VALUES (?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
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
            String selectQuery = "SELECT userKey, userEmail, userName, userPassword, userStatus, userBalance, PL_Points, role FROM users " +
                    "WHERE userEmail = ?";

            PreparedStatement statement = connection.prepareStatement(selectQuery);
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

    boolean updateUserPLpoint(int userKey, int pl_points) {
        String sql = "UPDATE users SET PL_Points = ? WHERE userKey = ?";
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {
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

    boolean updateUserBalance(int userKey, double balance) {
        String sql = "UPDATE users SET userBalance = ? WHERE userKey = ?";
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {
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
        String sql = "UPDATE users SET userPassword = ? WHERE userEmail = ? AND userName = ?";
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {
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
        String sql = "UPDATE users SET userName = ? WHERE userEmail = ?";
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {
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
        String sql = "UPDATE users SET userStatus = ? WHERE userEmail = ?";
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {
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

    boolean removeUser(String email) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String deleteQuery = "DELETE FROM users WHERE userEmail = ?";
            PreparedStatement statement = connection.prepareStatement(deleteQuery);
            statement.setString(1, email);
            int rowsAffected = statement.executeUpdate();
            statement.close();

            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // User not found or failed to removed
    }


    public List<User> getUsersList() {
        List<User> list = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String selectQuery = "SELECT userKey, userName, userEmail, userStatus, userBalance, PL_Points, role FROM users WHERE role = \"User\"";
            PreparedStatement statement = connection.prepareStatement(selectQuery);
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