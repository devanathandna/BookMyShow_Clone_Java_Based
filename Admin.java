import java.util.*;


public class Admin {
    String adminname;
    String email;
    String password;
    int adminId;
    List<Theatre> theatres;

 
    private static Map<String, Admin> adminDatabase = new HashMap<>();
    private static int idCounter = 1;

    Admin(String adminname, String email, String password) {
        this.adminname = adminname;
        this.email = email;
        this.password = password;
        this.adminId = idCounter++;
        this.theatres = new ArrayList<>();
    }

    // Register a new admin
    public static String register(String adminname, String email, String password) {
        if (adminDatabase.containsKey(email)) {
            return "Error: Email already exists";
        }
        Admin newAdmin = new Admin(adminname, email, password);
        adminDatabase.put(email, newAdmin);
        return "Success: Admin registered successfully";
    }


    public static Admin login(String email, String password) {
        Admin admin = adminDatabase.get(email);
        if (admin != null && admin.password.equals(password)) {
            return admin;
        }
        return null;
    }

 
    public static Admin getAdminByEmail(String email) {
        return adminDatabase.get(email);
    }


    public static boolean adminExists(String email) {
        return adminDatabase.containsKey(email);
    }

 
    public String getAdminname() { return adminname; }
    public String getEmail() { return email; }
    public int getAdminId() { return adminId; }

    void addTheatre(Theatre theatre) {
        theatres.add(theatre);
        System.out.println("Theatre '" + theatre.theatrename + "' added successfully at " + theatre.location);
    }


    Theatre addTheatre(int theatreid, String location, String theatrename, int totalseats) {
        Theatre theatre = new Theatre(theatreid, location, theatrename, totalseats, 10); // Default tax of 10 added
        theatres.add(theatre);
        System.out.println("Theatre '" + theatrename + "' added successfully at " + location);
        return theatre;
    }


    void addShowToTheatre(Theatre theatre, Show show) {
        if (theatre.shows == null) {
            theatre.shows = new ArrayList<>();
        }
        theatre.shows.add(show);
        System.out.println("Show '" + show.showname + "' added to theatre '" + theatre.theatrename + "'");
    }

    Show addShowToTheatre(Theatre theatre, int showid, String showname, int showprice) {
        Show show = new Show();
        show.showid = showid;
        show.showname = showname;
        show.showprice = showprice;
        
        if (theatre.shows == null) {
            theatre.shows = new ArrayList<>();
        }
        theatre.shows.add(show);
        System.out.println("Show '" + showname + "' added to theatre '" + theatre.theatrename + "'");
        return show;
    }


    List<Theatre> getAllTheatres() {
        return theatres;
    }

  
    Theatre getTheatreById(int theatreid) {
        for (Theatre theatre : theatres) {
            if (theatre.theatreid == theatreid) {
                return theatre;
            }
        }
        return null;
    }
}
