import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class UserScore {
    private String name;
    private double marks;

    public UserScore(String name, double marks) {
        this.name = name;
        this.marks = marks;
    }

    public String getName() {
        return name;
    }

    public double getMarks() {
        return marks;
    }
}

public class Leaderboard {
    public void printLeaderboard() {
        Database db = new Database();

        // Create a list of UserScore objects from the dashboard data
        List<UserScore> userScores = new ArrayList<>();
        for (User userScore : db.getUsersList()) {
            String username = userScore.getUsername();
            double PLpoints = userScore.getPL_Points();
            String status = userScore.getStatus();
            if (!status.equalsIgnoreCase("disqualified"))
                userScores.add(new UserScore(username, PLpoints));
        }

        // Sort the data based on marks (highest to lowest)
        Collections.sort(userScores, Comparator.comparingDouble(UserScore::getMarks).reversed());

        // Extract the top ten users
        List<UserScore> topTenUsers = userScores.subList(0, Math.min(userScores.size(), 10));

        // Display the leaderboard

        System.out.println("\n     Top 10 Leaderboard      ");
        System.out.println("=============================");
        System.out.println("Rank |     User     |  Point ");
        System.out.println("=====|==============|========");
        for (int i = 0; i < topTenUsers.size(); i++) {
            UserScore users = topTenUsers.get(i);
            System.out.printf("|%4d | %-12s | %5.2f|%n", i + 1, users.getName(), users.getMarks());
        }
        System.out.println("=============================");
    }
}