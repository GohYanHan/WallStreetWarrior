public class User {
    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private String name;
    private String email;
    private String password;
    private Portfolio portfolio;
    private boolean disqualified;

    public User(String email, String password, String name) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.portfolio = new Portfolio();
    }

    public String getName() {
        return name;
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

    public boolean isDisqualified() {return disqualified;}

    public void setDisqualified(boolean disqualified) {this.disqualified = disqualified;}

}

