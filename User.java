public class User {
    private String username;
    private String email;
    private String password;
    private String status;
    private int key;
    private double balance;
    private double PL_Points;
    private String role;
    private double thresholds;
    private Portfolio portfolio;

    private boolean isNotified;

    public User() {
        this.portfolio = new Portfolio(key, balance);
    }

    public User(int key, String email, String username, String password, String status, double balance, double PL_Points, String role, double thresholds, boolean isNotified) {
        this.key = key;
        this.email = email;
        this.username = username;
        this.password = password;
        this.status = status;
        this.balance = balance;
        this.PL_Points = PL_Points;
        this.role = role;
        this.thresholds = thresholds;
        this.portfolio = new Portfolio(key, balance);
        this.isNotified = isNotified;
    }

    public double getThresholds() {
        return thresholds;
    }
    public String getUsername() {
        return username;
    }
    public String getEmail() {
        return email;
    }
    public String getPassword() {
        return password;
    }
    public Portfolio getPortfolio() {
        return portfolio;
    }
    public String getStatus() {
        return status;
    }
    public int getKey() {
        return key;
    }
    public double getBalance() {
        return balance;
    }

    public double getPL_Points() {
        return PL_Points;
    }

    void setPL_Points(int PL_Points) {
        this.PL_Points = PL_Points;
    }

    public String getRole() {
        return role;
    }

    public boolean getIsNotified() {
        return isNotified;
    }
}

