import java.util.*;

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

class Leaderboard {
    public void printLeaderboard() {
        Database db = new Database(); // Assuming you have the Database class available

        // Create a list of UserScore objects from the dashboard data
        List<UserScore> userScores = new ArrayList<>();
        for (Map.Entry<Integer, Double> entry : db.loadPLpoint().entrySet()) {
            int userKey = entry.getKey();
            double points = entry.getValue();
            userScores.add(new UserScore("User" + userKey, points));
        }

        // Sort the data based on marks in descending order
        Collections.sort(userScores, Comparator.comparingDouble(UserScore::getMarks).reversed());

        // Extract the top ten users
        List<UserScore> topTenUsers = userScores.subList(0, Math.min(userScores.size(), 10));

        // Display the leaderboard
        System.out.println("Rank | User         | Marks");
        System.out.println("-----|--------------|-------");
        for (int i = 0; i < topTenUsers.size(); i++) {
            UserScore user = topTenUsers.get(i);
            System.out.printf("%4d | %-12s | %5.2f%n", i + 1, user.getName(), user.getMarks());
        }
    }
}


