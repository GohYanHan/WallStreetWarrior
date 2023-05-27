import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.mindrot.jbcrypt.BCrypt;

import javax.swing.*;
import java.sql.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Database {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/wsw1";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "abc123";
    private static User user;

    public Database() {

    }

    public boolean addUser(String email, String password, String username) {
        String sql = "INSERT INTO users (userEmail, userPassword, userName) VALUES (?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);
            statement.setString(2, password);
            statement.setString(3, username);

            int rowsInserted = statement.executeUpdate();

            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean loadUser(String inputUsername, String inputPassword) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {


            String selectQuery = "SELECT userKey, userName, userEmail, userPassword, userStatus, userBalance, PL_Points, role FROM users " +
                    "WHERE userName = ? AND userPassword = ?";

            PreparedStatement statement = connection.prepareStatement(selectQuery);
            statement.setString(1, inputUsername);
            statement.setString(2, inputPassword);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                user = new User();
                user.setKey(resultSet.getInt("userKey"));
                user.setUsername(resultSet.getString("userName"));
                user.setEmail(resultSet.getString("userEmail"));
                user.setPassword(resultSet.getString("userPassword"));
                user.setRole(resultSet.getString("role"));
                if (user.getRole().equals("Admin")) {
                } else if (user.getRole().equals("user")) {
                    user.setBalance(resultSet.getInt("userBalance"));
                    user.setPL_Points(resultSet.getInt("PL_Points"));
                }
                return true;
            }

            resultSet.close();
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateUserBalance(String username, int balance, int pl_points) {
        String sql = "UPDATE users SET userBalance = ? PL_Points = ? WHERE userName = ?";
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, balance);
            statement.setInt(2, pl_points);
            statement.setString(3, username);
            statement.executeUpdate();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean resetPassword(String username, String newPassword) {
        String sql = "UPDATE users SET userPassword = ? WHERE userName = ?";
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, hashPassword(newPassword));
            statement.setString(2, username);
            statement.executeUpdate();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUsername(String oldUsername, String newUsername) {
        String sql = "UPDATE users SET userName = ? WHERE userName = ?";
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newUsername);
            statement.setString(1, oldUsername);
            statement.executeUpdate();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean disqualifyUser(String username) {
        String sql = "UPDATE users SET userStatus = ? WHERE userName = ?";
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "Disqualified");
            statement.setString(1, username);
            statement.executeUpdate();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeUser(String username) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String deleteQuery = "DELETE FROM users WHERE userName = ?";
            PreparedStatement statement = connection.prepareStatement(deleteQuery);
            statement.setString(1, username);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                return true; // User removed successfully
            }

            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // User not found or failed to removed
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
//
//    public String encryptPassword(String password) {
//        try {
//            // Create a SHA-256 MessageDigest instance
//            MessageDigest digest = MessageDigest.getInstance("SHA-256");
//
//            // Convert the password string to bytes
//            byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
//
//            // Apply the digest calculation to the password bytes
//            byte[] hashedBytes = digest.digest(passwordBytes);
//
//            // Convert the hashed bytes to a hexadecimal representation
//            StringBuilder hexString = new StringBuilder();
//            for (byte b : hashedBytes) {
//                String hex = Integer.toHexString(0xff & b);
//                if (hex.length() == 1) {
//                    hexString.append('0');
//                }
//                hexString.append(hex);
//            }
//
//            return hexString.toString();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

    public ObservableList<User> displayUsers() {
        ObservableList<User> list = FXCollections.observableArrayList();

        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {

            String selectQuery = "SELECT userKey, userName, userEmail, userPassword, userStatus, userBalance, PL_Points, role FROM users ";

            PreparedStatement statement = connection.prepareStatement(selectQuery);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                list.add(new User(resultSet.getString("userName"), resultSet.getString("userEmail"), resultSet.getInt("userBalance"), resultSet.getInt("PL_Points")));
            }
            resultSet.close();
            statement.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public User getUser() {
        return user;
    }

    public static void setUser(User user) {
        Database.user = user;
    }

}