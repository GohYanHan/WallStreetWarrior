import java.util.List;
import java.util.Scanner;

public class AdminPanel {
    private final Database db;

    public AdminPanel() {
        db = new Database();
    }

    void adminPanel() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("-----------------------------");
            System.out.println("1. List Users");
            System.out.println("2. Disqualify User");
            System.out.println("3. Remove User");
            System.out.println("4. Update User Information");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            System.out.println("-----------------------------");
            scanner.nextLine(); // Consume the newline character after reading the choice

            switch (choice) {
                case 1 -> listUsers();
                case 2 -> {
                    System.out.print("Enter the email of the user: ");
                    String email = scanner.nextLine();
                    if (disqualifyUser(email))
                        System.out.println("User (" + email + ") has been disqualified.");
                    else
                        System.out.println("User (" + email + ") not found, please try again.");
                }
                case 3 -> {
                    System.out.print("Enter the email of the user to be removed: ");
                    String email = scanner.nextLine();
                    if (removeUser(email))
                        System.out.println("User (" + email + ") has been removed.");
                    else
                        System.out.println("User (" + email + ") not found, please try again.");
                }
                case 4 -> {
                    System.out.print("Enter the email of the user: ");
                    String email = scanner.nextLine();
                    System.out.print("Enter the new username: ");
                    String newUsername = scanner.nextLine();
                    if (updateUsername(email, newUsername))
                        System.out.println("User (" + newUsername + ") update successfully!");
                    else
                        System.out.println("User (" + email + ") not found, please try again.");

                }
                case 5 -> {
                    System.out.println("Exiting Admin Panel...");
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // Method to disqualify a user
    public boolean disqualifyUser(String name) {
        return db.disqualifyUser(name);
    }

    // Method to list all users
    public void listUsers() {
        List<User> users = db.getUsersList();
        System.out.printf("%-6s%-30s%-20s%-15s%-10s%-5s%n", "ID", "Email", "Username", "Status", "Balance", "PL Points");
        for (User user : users) {
            System.out.printf("%-6d%-30s%-20s%-15s%-10d%-5d%n", user.getKey(), user.getEmail(), user.getUsername(), user.getStatus(), user.getBalance(), user.getPL_Points());
        }
    }

    public boolean removeUser(String email) {
        return db.removeUser(email);
    }


    public boolean updateUsername(String email, String newUsername) {
        return db.updateUsername(email, newUsername);
    }
}
