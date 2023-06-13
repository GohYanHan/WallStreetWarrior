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
            System.out.println("-".repeat(120));
            System.out.println("1. List Users");
            System.out.println("2. Display Potential Frauds");
            System.out.println("3. Disqualify User");
            System.out.println("4. Remove User");
            System.out.println("5. Update User Information");
            System.out.println("6. Exit");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            System.out.println("-".repeat(120));
            scanner.nextLine(); // Consume the newline character after reading the choice

            switch (choice) {
                case 1 -> listUsers();
                case 2 -> {
                    FraudDetection fd = new FraudDetection();
                    fd.displaySuspiciousUsers();
                }

                case 3 -> {
                    System.out.print("Enter the email of the user: ");
                    String email = scanner.nextLine();
                    if (disqualifyUser(email))
                        System.out.println("User (" + email + ") has been disqualified.");
                    else
                        System.out.println("User (" + email + ") not found, please try again.");
                }
                case 4 -> {
                    System.out.print("Enter the email of the user to be removed: ");
                    String email = scanner.nextLine();
                    if (removeUser(email))
                        System.out.println("User (" + email + ") has been removed.");
                    else
                        System.out.println("User (" + email + ") not found, please try again.");
                }
                case 5 -> {
                    System.out.print("Enter the email of the user: ");
                    String email = scanner.nextLine();
                    System.out.print("Enter the new username: ");
                    String newUsername = scanner.nextLine();
                    if (updateUsername(email, newUsername))
                        System.out.println("User (" + newUsername + ") update successfully!");
                    else
                        System.out.println("User (" + email + ") not found, please try again.");

                }
                case 6 -> {
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
        System.out.printf("%-6s%-30s%-20s%-15s%-15s%-12s%s%n", "ID", "Email", "Username", "Status", "Balance", "PL Points", "Thresholds");
        for (User user : users) {
            if (user.getRole().equals("User"))
                System.out.printf("%-6d%-30s%-20s%-15s%-15.2f%-12.3f%.3f%n", user.getKey(), user.getEmail(), user.getUsername(), user.getStatus(), user.getBalance(), user.getPL_Points(), user.getThresholds());
            else continue;
        }
    }

    public boolean removeUser(String email) {
        return db.removeUser(email);
    }


    public boolean updateUsername(String email, String newUsername) {
        return db.updateUsername(email, newUsername);
    }
}
