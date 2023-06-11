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

    public User() {
        this.portfolio = new Portfolio(key, balance);

    }

//    public User(String email, String password, String username) {
//        this.username = username;
//        this.email = email;
//        this.password = password;
//        this.portfolio = new Portfolio();
//    }

    public User(int key, String email, String username, String password, String status, double balance, double PL_Points, String role, double thresholds) {
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
    }

    public double getThresholds() {
        return thresholds;
    }


    public String getUsername() {
        return username;
    }

    void setUsername(String name) {
        this.username = name;
    }

    public String getEmail() {
        return email;
    }

    void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    void setPassword(String password) {
        this.password = password;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public String getStatus() {
        return status;
    }

    void setStatus(String status) {
        this.status = status;
    }

    public int getKey() {
        return key;
    }

    void setKey(int key) {
        this.key = key;
    }

    public double getBalance() {
        return balance;
    }

    void setBalance(double balance) {
        this.balance = balance;
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

    void setRole(String role) {
        this.role = role;
    }
}

