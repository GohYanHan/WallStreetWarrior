import java.io.*;
import java.util.*;

import org.mindrot.jbcrypt.BCrypt;

public class UserAuthentication {
    //Admin account and password
    private final String ADMIN_EMAIL = "22004848@siswa.um.edu.my";
    private final String ADMIN_PASSWORD = "Wa11Street";
    private Map<String, User> users;
    private static final String USERS_DATA = "data.txt";
    private final Scanner scanner = new Scanner(System.in);

    public Map<String, User> getUsers() {
        return users;
    }

    public UserAuthentication() {
        users = new HashMap<>();
        read();
    }

    public boolean register(String email, String password, String name) {
        //Format of valid email address
        boolean isEmailValid = email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
        //Format of valid password (at least one uppercase letter, one lowercase letter, one digit and minimum length of 8 characters)
        boolean isPwValid = password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");
        if (!isPwValid && !isEmailValid) {
            System.out.println("Invalid email and password. Please enter again.");
            return false;
        } else if (!isEmailValid) {
            System.out.println("Invalid email. Please enter again.");
            return false;
        } else if (!isPwValid) {
            System.out.println("Invalid password. Please enter again.");
            System.out.println("Your password should contain at least one uppercase letter, one lowercase letter, one digit and minimum length of 8 characters.");
            return false;
        }
        User user = new User(email, hashPassword(password), name);
        users.put(email, user);
        write();
        System.out.println("Registration successful!");
        return true;
    }

    public boolean login(String email, String password) {
        User user = users.get(email);
        if (user != null && BCrypt.checkpw(password, hashPassword(password))) {
            System.out.println("Login successful!");
            System.out.println("Welcome, " + user.getName() + "!");
            System.out.println("-----------------------------");
            return true;
        }
        System.out.println("Invalid email or password. Please try again.");

        return false;
    }

    void write() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_DATA))) {
            for (User user : users.values()) {
                writer.write(user.getEmail() + "," + user.getPassword() + "," + user.getName() + "," + user.isDisqualified() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void read() {
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_DATA))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                String email = data[0];
                String password = data[1];
                String name = data[2];
                boolean isDisqualified = Boolean.parseBoolean(data[3]);
                User user = new User(email, password, name);
                user.setDisqualified(isDisqualified);
                users.put(email, user);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public void disqualifyUser(String email) {
        User user = users.get(email);
        if (user != null) {
            user.setDisqualified(true);
            System.out.println("User (" + email + ") has been disqualified.");
        } else {
            System.out.println("User (" + email + ") not found.");
        }
    }

    // Method to list all users
    public void listUsers() {
        System.out.printf("%-30s%-20s%-15s%n", "Email", "Name", "Status");
        for (User user : users.values()) {
            System.out.printf("%-30s%-20s%-15s%n", user.getEmail(), user.getName(), (user.isDisqualified() ? "Disqualified" : "Qualified"));
        }
    }

    // Method to add a user to the system
    public void addUser(User user) {
        users.put(user.getEmail(), user);
        write();
        System.out.println("User (" + user.getEmail() + ") has been added.");
    }

    public void removeUser(String email) {
        User user = users.remove(email);
        if (user != null) {
            write();
            System.out.println("User (" + email + ") has been removed.");
        } else {
            System.out.println("User (" + email + ") not found.");
        }
    }

    public void updateUser(String email) {
        User user = users.get(email);
        if (user != null) {
            System.out.println("Enter updated information for user (" + email + "): ");
            System.out.print("New Password: ");
            String newPassword = scanner.nextLine();
            System.out.print("New Name: ");
            String newName = scanner.nextLine();

            user.setPassword(hashPassword(newPassword));
            user.setName(newName);

            write();
            System.out.println("User (" + email + ") has been updated.");
        } else {
            System.out.println("User (" + email + ") not found.");
        }
    }

    public void adminPanel(UserAuthentication userAuth) {
        AdminPanel admin = new AdminPanel(userAuth);
        while (true) {
            System.out.println("-----------------------------");
            System.out.println("1. List Users");
            System.out.println("2. Disqualify User");
            System.out.println("3. Add User");
            System.out.println("4. Remove User");
            System.out.println("5. Update User Information");
            System.out.println("6. Exit");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            System.out.println("-----------------------------");
            scanner.nextLine(); // Consume the newline character after reading the choice

            switch (choice) {
                case 1 -> admin.listUsers();
                case 2 -> {
                    System.out.print("Enter the email of the user to disqualify: ");
                    String userEmail = scanner.nextLine();
                    admin.disqualifyUser(userEmail);
                }
                case 3 -> {
                    System.out.print("Enter the email of the new user: ");
                    String email = scanner.nextLine();
                    System.out.print("Enter the password of the new user: ");
                    String password = hashPassword(scanner.nextLine());
                    System.out.print("Enter the name of the new user: ");
                    String name = scanner.nextLine();
                    register(email,password,name);
                }case 4 -> {
                    System.out.print("Enter the email of the user to be removed: ");
                    String userEmail = scanner.nextLine();
                    admin.removeUser(userEmail);
                }
                case 5 -> {
                    System.out.print("Enter the email of the user to be updated: ");
                    String userEmail = scanner.nextLine();
                    admin.updateUser(userEmail);
                }
                case 6 -> {
                    System.out.println("Exiting Admin Panel...");
                    return;
                }
                default -> {
                    System.out.println("Invalid choice. Please try again.");
                }
            }
        }
    }

    public static void main(String[] args) {
        UserAuthentication userAuth = new UserAuthentication();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to the Application!");
        while (true) {
            System.out.println("-----------------------------");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();
            System.out.println("-----------------------------");
            switch (choice) {
                case 1 -> {
                    System.out.print("Registration\nEmail: ");
                    String email = scanner.nextLine();
                    System.out.println("Your password should contain at least one uppercase letter, one lowercase letter, one digit and minimum length of 8 characters.");
                    System.out.print("Password: ");
                    String password = scanner.nextLine();
                    System.out.print("Name: ");
                    String name = scanner.nextLine();
                    userAuth.register(email, password, name);
                }
                case 2 -> {
                    System.out.print("Login\nEmail: ");
                    String email = scanner.nextLine();
                    System.out.print("Password: ");
                    String password = scanner.nextLine();
                    if (email.equals(userAuth.ADMIN_EMAIL) && password.equals(userAuth.ADMIN_PASSWORD)) {
                        System.out.println("\nWelcome to Admin Panel");
                        userAuth.adminPanel(userAuth);
                    } else if (userAuth.login(email, password))
                        return;
                }
                case 3 -> {
                    System.out.println("Exiting...");
                    System.out.println("-----------------------------");
                    return;
                }
                default -> {
                    System.out.println("Invalid choice. Please try again.");
                }

            }
        }
    }
}