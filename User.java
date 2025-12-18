import java.util.HashMap;
import java.util.Map;

public class User {
    String name;
    String email;
    String password;
    int id;
    int seatsbooked;


    private static Map<String, User> userDatabase = new HashMap<>();
    private static int idCounter = 1;

    User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.id = idCounter++;
        this.seatsbooked = 0;
    }


    public static String register(String name, String email, String password) {
        if (userDatabase.containsKey(email)) {
            return "Error: Email already exists";
        }
        User newUser = new User(name, email, password);
        userDatabase.put(email, newUser);
        return "Success: User registered successfully";
    }


    public static User login(String email, String password) {
        User user = userDatabase.get(email);
        if (user != null && user.password.equals(password)) {
            return user;
        }
        return null;
    }


    public static User getUserByEmail(String email) {
        return userDatabase.get(email);
    }


    public static boolean userExists(String email) {
        return userDatabase.containsKey(email);
    }

    // Getters
    public String getName() { return name; }
    public String getEmail() { return email; }
    public int getId() { return id; }
    public int getSeatsbooked() { return seatsbooked; }
}