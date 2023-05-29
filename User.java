public class User {
    private String username;
    private String email;
    private String password;
    private String status;
    private int key;
    private int balance;
    private int PL_Points;
    private String role;
    private Portfolio portfolio;

    public User() {
    }

    public User(String email, String password, String username) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.portfolio = new Portfolio();
    }

    public User(String email, String username, String status, int balance, int PL_Points, int key) {
        this.username = username;
        this.email = email;
        this.status = status;
        this.balance = balance;
        this.PL_Points = PL_Points;
        this.key = key;
        this.portfolio = new Portfolio();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String name) {
        this.username = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public int getPL_Points() {
        return PL_Points;
    }

    public void setPL_Points(int PL_Points) {
        this.PL_Points = PL_Points;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

