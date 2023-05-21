import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class UserScore {
        private String name;
        private int marks;

        public UserScore(String name, int marks) {
            this.name = name;
            this.marks = marks;
        }

        public String getName() {
            return name;
        }

        public int getMarks() {
            return marks;
        }
public class Leaderboard {
    public static void main(String[] args) {
        // Retrieve users' names and marks from the dashboard
        List<UserScore> dashboardData = retrieveDataFromDashboard();

        // Sort the data based on marks in descending order
        Collections.sort(dashboardData, Comparator.comparingInt(UserScore::getMarks).reversed());

        // Extract the top ten users
        List<UserScore> topTenUsers = dashboardData.subList(0, Math.min(dashboardData.size(), 10));

        // Display the leaderboard
        System.out.println("Rank | User         | Marks");
        System.out.println("-----|--------------|-------");
        // Add condition if user is disqualified then score cannot be counted
        for (int i = 0; i < topTenUsers.size(); i++) {
            UserScore user = topTenUsers.get(i);
            System.out.printf("%4d | %-12s | %5d%n", i + 1, user.getName(), user.getMarks());
        }
    }
    // Simulated method to retrieve data from the dashboard
    private static List<UserScore> retrieveDataFromDashboard() {

        // Retrieve the data from the dashboard and populate a list of User objects
        List<UserScore> dashboardData = new ArrayList<>();

        // Split dashboard de username n score
        // Use for loop / while loop to add username n score into dashboard
        dashboardData.add(new UserScore("User123", 500));

        return dashboardData;
        }
    }
}
