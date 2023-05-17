import java.util.HashMap;
import java.util.Map;

public class AdminPanel {
    //    private Map<String, User> users;
    private final UserAuthentication userAuth;

    public AdminPanel(UserAuthentication userAuth) {
//        users = new HashMap<>();
        this.userAuth = userAuth;
//        loadUsers();
    }

//    // Load users from UserAuthentication
//    private void loadUsers() {
//        userAuth.read();
//        users = userAuth.getUsers();
//    }


    // Method to disqualify a user
    public void disqualifyUser(String email) {
        userAuth.disqualifyUser(email);
    }

    // Method to list all users
    public void listUsers() {
        userAuth.listUsers();
    }

    public void addUser(User user) {
        userAuth.addUser(user);
    }

    public void removeUser(String email) {
        userAuth.removeUser(email);
    }

    public void updateUser(String email) {
        userAuth.updateUser(email);
    }

    public User getUser(String email) {
        Map<String, User> users = userAuth.getUsers();
        return users.get(email);
    }
}
