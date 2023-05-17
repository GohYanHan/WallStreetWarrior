import java.io.*;
import java.util.*;
import org.mindrot.jbcrypt.BCrypt;

public class UserAuthentication {

    private Map<String, User> users;
    private static final String USERS_DATA = "data.txt";

    public UserAuthentication() {
        users = new HashMap<>();
        read();
    }

    public void register(String email, String password, String name) {
        User user = new User(email, hashPassword(password), name);
        users.put(email, user);
        write();
        System.out.println("Registration successful!");
    }

    public void login(String email, String password) {
        User user = users.get(email);
        if (user != null && BCrypt.checkpw(password,hashPassword(password))) {
            System.out.println("Login successful!");
            System.out.println("Welcome, " + user.getName() + "!");
        }
        System.out.println("Invalid email or password. Please try again.");
    }

    private void write(){
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_DATA))) {
            for (User user : users.values()) {
                writer.write(user.getEmail() + "," + user.getPassword() + "," + user.getName() + "\n");
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
                User user = new User(email, password, name);
                users.put(email, user);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
    public static void main(String[] args) {
        UserAuthentication authentication = new UserAuthentication();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to the Application!");
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.print("Enter your choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume the newline character after reading the choice

        switch (choice) {
            case 1 -> {
                System.out.print("Registration\nEmail: ");
                String email = scanner.nextLine();
                System.out.print("Password: ");
                String password = scanner.nextLine();
                System.out.print("Name: ");
                String name = scanner.nextLine();
                authentication.register(email, password, name);
            }
            case 2 -> {
                System.out.print("Login\nEmail: ");
                String email = scanner.nextLine();
                System.out.print("Password: ");
                String password = scanner.nextLine();
                authentication.login(email, password);
            }
            default -> System.out.println("Invalid choice. Please try again.");
        }
    }
}