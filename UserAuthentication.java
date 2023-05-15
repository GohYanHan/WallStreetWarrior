import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class UserAuthentication {
    public static void main(String[] args) {
        UserAuthentication u1 = new UserAuthentication();
        Scanner sc = new Scanner(System.in);
        System.out.print("Do you want to Register or Login? (R/L): ");
        char e = sc.next().charAt(0);
        System.out.print("Please Enter your username: ");
        String u = sc.next();
        System.out.print("Please Enter your password: ");
        String p = sc.next();
        u1.input(u,p,e);
    }
    Map<String, String> d = new HashMap<>();


    public void input(String u, String p, char e) {
        read();
        if (e=='L'||e=='l') {
            boolean authenticated = login(u, p);
            if (authenticated) {
                System.out.println("Login successful!");
            } else {
                System.out.println("Invalid username or password");
            }
        } else if (e=='R'||e=='r') {
            if (register(u, p)) {
                System.out.println("Registration successful!");
            } else {
                System.out.println("Username already exists");
            }
        }else {
            System.out.println("Invalid choice.");
        }
        write();
    }

    public void read() {
        d = new HashMap<>();
        String s = "";
        try {
            s = new String(Files.readAllBytes(Paths.get("data.txt")));
            if (s.length() == 0)
                return;
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] pairs = s.split("\n");
        for (String pair : pairs) {
            d.put(pair.split(",")[0], pair.split(",")[1]);
        }
    }

    public void write() {
        try (FileWriter m = new FileWriter("data.txt")) {
            for (Map.Entry<String, String> entry : d.entrySet()) {
                m.write(entry.getKey() + "," + entry.getValue() + "\n");
            }
            m.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    boolean login(String u, String p) {
        if (d.containsKey(u)) {
            String storedHash = d.get(u);
            String inputHash = hashPassword(p);
            return storedHash.equals(inputHash);
        }
        return false;
    }

    boolean register(String u, String p) {
        if (d.containsKey(u)) return false;
        d.put(u, hashPassword(p));
        return true;
    }
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}