
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.net.InetSocketAddress;
import java.io.*;
import java.util.*;
import java.security.MessageDigest;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;

public class BackendServer {

    // MongoDB Configuration
    static final String MONGODB_URI = "mongodb+srv://deava_sample:deava_0701_sample@clustermain.d1ldf.mongodb.net/?appName=ClusterMain";
    static final String MONGODB_DB = "BookMyShowDB";

    static MongoClient mongoClient;
    static MongoDatabase database;
    static MongoCollection<Document> usersDB;
    static MongoCollection<Document> adminsDB;
    static MongoCollection<Document> theatresDB;
    static MongoCollection<Document> showsDB;
    static MongoCollection<Document> moviesDB;
    static MongoCollection<Document> bookingsDB;

    public static void main(String[] args) throws Exception {
        // Initialize MongoDB
        System.out.println("Connecting to MongoDB...");
        mongoClient = MongoClients.create(MONGODB_URI);
        database = mongoClient.getDatabase(MONGODB_DB);
        usersDB = database.getCollection("users");
        adminsDB = database.getCollection("admins");
        theatresDB = database.getCollection("theatres");
        showsDB = database.getCollection("shows");
        moviesDB = database.getCollection("movies");
        bookingsDB = database.getCollection("bookings");
        database.runCommand(new Document("ping", 1));
        System.out.println("Connected to MongoDB!");

        int port = 8000;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        
       
        server.createContext("/api/health", new HealthHandler());
        
        // User endpoints
        server.createContext("/api/user/register", new UserRegisterHandler());
        server.createContext("/api/user/login", new UserLoginHandler());
        
        // Admin endpoints
        server.createContext("/api/admin/register", new AdminRegisterHandler());
        server.createContext("/api/admin/login", new AdminLoginHandler());
        
        // Stats endpoint
        server.createContext("/api/stats", new StatsHandler());
        
        // Theatre endpoints
        server.createContext("/api/theatres", new TheatresHandler());
        
        // Shows endpoints
        server.createContext("/api/shows", new ShowsHandler());
        
        // Movies endpoints
        server.createContext("/api/movies", new MoviesHandler());
        
        // Booking endpoints
        server.createContext("/api/bookings", new BookingsHandler());
        server.createContext("/api/show-seats", new ShowSeatsHandler());
        server.createContext("/api/book-seats", new BookSeatsHandler());

        server.setExecutor(null);
        System.out.println("===========================================");
        System.out.println("  BookMyShow Java Backend Server Started!");
        System.out.println("  Running at http://localhost:" + port);
        System.out.println("===========================================");
        server.start();
    }

   

    static class HealthHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            String resp = "{\"status\":\"ok\",\"message\":\"BookMyShow Backend Running\"}";
            sendResponse(exchange, 200, resp);
        }
    }

    static class UserRegisterHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            String body = readBody(exchange);
            Map<String, String> data = parseJson(body);
            
            String name = data.get("name");
            String email = data.get("email");
            String password = data.get("password");

            if (email == null || password == null || name == null) {
                sendResponse(exchange, 400, "{\"success\":false,\"message\":\"Missing required fields\"}");
                return;
            }

            if (usersDB.find(Filters.eq("email", email)).first() != null) {
                sendResponse(exchange, 400, "{\"success\":false,\"message\":\"Email already exists\"}");
                return;
            }

            Document user = new Document()
                .append("name", name)
                .append("email", email)
                .append("password", hashPassword(password));
            usersDB.insertOne(user);

            System.out.println("[USER REGISTERED] " + email);
            sendResponse(exchange, 201, "{\"success\":true,\"message\":\"User registered successfully\"}");
        }
    }

    static class UserLoginHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            String body = readBody(exchange);
            Map<String, String> data = parseJson(body);
            
            String email = data.get("email");
            String password = data.get("password");

            if (email == null || password == null) {
                sendResponse(exchange, 400, "{\"success\":false,\"message\":\"Missing credentials\"}");
                return;
            }

            Document user = usersDB.find(Filters.eq("email", email)).first();
            if (user == null || !user.getString("password").equals(hashPassword(password))) {
                sendResponse(exchange, 401, "{\"success\":false,\"message\":\"Invalid email or password\"}");
                return;
            }

            System.out.println("[USER LOGIN] " + email);
            String resp = String.format(
                "{\"success\":true,\"message\":\"Login successful\",\"user\":{\"name\":%s,\"email\":%s,\"id\":%s}}",
                escape(user.getString("name")), escape(user.getString("email")), escape(user.getObjectId("_id").toHexString())
            );
            sendResponse(exchange, 200, resp);
        }
    }

    static class AdminRegisterHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            String body = readBody(exchange);
            Map<String, String> data = parseJson(body);
            
            String name = data.get("name");
            String email = data.get("email");
            String password = data.get("password");

            if (email == null || password == null || name == null) {
                sendResponse(exchange, 400, "{\"success\":false,\"message\":\"Missing required fields\"}");
                return;
            }

            if (adminsDB.find(Filters.eq("email", email)).first() != null) {
                sendResponse(exchange, 400, "{\"success\":false,\"message\":\"Email already exists\"}");
                return;
            }

            Document admin = new Document()
                .append("name", name)
                .append("email", email)
                .append("password", hashPassword(password));
            adminsDB.insertOne(admin);

            System.out.println("[ADMIN REGISTERED] " + email);
            sendResponse(exchange, 201, "{\"success\":true,\"message\":\"Admin registered successfully\"}");
        }
    }

    static class AdminLoginHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            String body = readBody(exchange);
            Map<String, String> data = parseJson(body);
            
            String email = data.get("email");
            String password = data.get("password");

            if (email == null || password == null) {
                sendResponse(exchange, 400, "{\"success\":false,\"message\":\"Missing credentials\"}");
                return;
            }

            Document admin = adminsDB.find(Filters.eq("email", email)).first();
            if (admin == null || !admin.getString("password").equals(hashPassword(password))) {
                sendResponse(exchange, 401, "{\"success\":false,\"message\":\"Invalid email or password\"}");
                return;
            }

            System.out.println("[ADMIN LOGIN] " + email);
            String resp = String.format(
                "{\"success\":true,\"message\":\"Login successful\",\"admin\":{\"name\":%s,\"email\":%s,\"id\":%s}}",
                escape(admin.getString("name")), escape(admin.getString("email")), escape(admin.getObjectId("_id").toHexString())
            );
            sendResponse(exchange, 200, resp);
        }
    }

    static class StatsHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String resp = String.format(
                "{\"total_users\":%d,\"total_admins\":%d,\"total_theatres\":%d,\"total_shows\":%d}",
                usersDB.countDocuments(), adminsDB.countDocuments(), theatresDB.countDocuments(), showsDB.countDocuments()
            );
            sendResponse(exchange, 200, resp);
        }
    }

    static class TheatresHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            String method = exchange.getRequestMethod();
            
            if (method.equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (method.equalsIgnoreCase("GET")) {
                StringBuilder sb = new StringBuilder("[");
                boolean first = true;
                for (Document t : theatresDB.find()) {
                    if (!first) sb.append(",");
                    first = false;
                    sb.append("{");
                    sb.append("\"id\":").append(escape(t.getObjectId("_id").toHexString())).append(",");
                    sb.append("\"name\":").append(escape(t.getString("name"))).append(",");
                    sb.append("\"location\":").append(escape(t.getString("location"))).append(",");
                    sb.append("\"tax\":").append(t.getInteger("tax", 10)).append(",");
                    sb.append("\"totalseats\":").append(t.getInteger("totalseats", 50));
                    sb.append("}");
                }
                sb.append("]");
                sendResponse(exchange, 200, sb.toString());
            } else if (method.equalsIgnoreCase("POST")) {
                String body = readBody(exchange);
                Map<String, String> data = parseJson(body);
                Document theatre = new Document()
                    .append("name", data.get("name"))
                    .append("location", data.get("location"))
                    .append("totalseats", parseInt(data.get("totalseats"), 50))
                    .append("tax", parseInt(data.get("tax"), 10));
                theatresDB.insertOne(theatre);
                System.out.println("[THEATRE ADDED] " + data.get("name"));
                sendResponse(exchange, 201, "{\"success\":true,\"message\":\"Theatre added successfully\"}");
            } else {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }

    static class ShowsHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            String method = exchange.getRequestMethod();
            
            if (method.equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (method.equalsIgnoreCase("GET")) {
                StringBuilder sb = new StringBuilder("[");
                boolean first = true;
                for (Document s : showsDB.find()) {
                    if (!first) sb.append(",");
                    first = false;
                    sb.append("{");
                    sb.append("\"id\":").append(escape(s.getObjectId("_id").toHexString())).append(",");
                    sb.append("\"movieId\":").append(escape(s.getString("movieId"))).append(",");
                    sb.append("\"theatreId\":").append(escape(s.getString("theatreId"))).append(",");
                    sb.append("\"showtime\":").append(escape(s.getString("showtime"))).append(",");
                    sb.append("\"price\":").append(s.getInteger("price", 200)).append(",");
                    sb.append("\"totalSeats\":").append(s.getInteger("totalSeats", 50));
                    sb.append("}");
                }
                sb.append("]");
                sendResponse(exchange, 200, sb.toString());
            } else if (method.equalsIgnoreCase("POST")) {
                String body = readBody(exchange);
                Map<String, String> data = parseJson(body);
                int totalSeats = 50;
                String theatreId = data.get("theatreId");
                if (theatreId != null) {
                    try {
                        Document theatre = theatresDB.find(Filters.eq("_id", new ObjectId(theatreId))).first();
                        if (theatre != null) totalSeats = theatre.getInteger("totalseats", 50);
                    } catch (Exception e) {}
                }
                Document show = new Document()
                    .append("movieId", data.get("movieId"))
                    .append("theatreId", theatreId)
                    .append("showtime", data.get("showtime"))
                    .append("price", parseInt(data.get("price"), 200))
                    .append("totalSeats", totalSeats)
                    .append("bookedSeats", new ArrayList<Integer>());
                showsDB.insertOne(show);
                System.out.println("[SHOW ADDED] Movie: " + data.get("movieId"));
                sendResponse(exchange, 201, "{\"success\":true,\"message\":\"Show added successfully\"}");
            } else {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }

    // ============ UTILITY METHODS ============

    static void addCorsHeaders(HttpExchange e) {
        var h = e.getResponseHeaders();
        h.add("Access-Control-Allow-Origin", "*");
        h.add("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        h.add("Access-Control-Allow-Headers", "Content-Type,Authorization");
    }

    static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes("UTF-8");
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    static String readBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        return new String(is.readAllBytes(), "UTF-8");
    }

    static String escape(String s) {
        if (s == null) return "\"\"";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
    }

    static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return password; // Fallback to plain text if hashing fails
        }
    }

    static Map<String, String> parseJson(String body) {
        Map<String, String> m = new HashMap<>();
        try {
            String[] keys = {"name", "email", "password", "location", "totalseats", "price", "theatreId", "movieId", "genre", "duration", "language", "description", "showtime", "tax", "showId", "userEmail", "seats"};
            for (String key : keys) {
                String val = extract(body, "\"" + key + "\"");
                if (val != null) m.put(key, val);
            }
        } catch (Exception ex) {
        }
        return m;
    }

    static int parseInt(String s, int def) {
        if (s == null) return def;
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }

    static class MoviesHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            String method = exchange.getRequestMethod();
            
            if (method.equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (method.equalsIgnoreCase("GET")) {
                StringBuilder sb = new StringBuilder("[");
                boolean first = true;
                for (Document m : moviesDB.find()) {
                    if (!first) sb.append(",");
                    first = false;
                    sb.append("{");
                    sb.append("\"id\":").append(escape(m.getObjectId("_id").toHexString())).append(",");
                    sb.append("\"name\":").append(escape(m.getString("name"))).append(",");
                    sb.append("\"genre\":").append(escape(m.getString("genre"))).append(",");
                    sb.append("\"duration\":").append(escape(m.getString("duration"))).append(",");
                    sb.append("\"language\":").append(escape(m.getString("language"))).append(",");
                    sb.append("\"description\":").append(escape(m.getString("description")));
                    sb.append("}");
                }
                sb.append("]");
                sendResponse(exchange, 200, sb.toString());
            } else if (method.equalsIgnoreCase("POST")) {
                String body = readBody(exchange);
                Map<String, String> data = parseJson(body);
                Document movie = new Document()
                    .append("name", data.get("name"))
                    .append("genre", data.get("genre"))
                    .append("duration", data.get("duration"))
                    .append("language", data.get("language"))
                    .append("description", data.get("description"));
                moviesDB.insertOne(movie);
                System.out.println("[MOVIE ADDED] " + data.get("name"));
                sendResponse(exchange, 201, "{\"success\":true,\"message\":\"Movie added successfully\"}");
            } else {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }

    static String extract(String body, String key) {
        int i = body.indexOf(key);
        if (i < 0) return null;
        int col = body.indexOf(":", i);
        if (col < 0) return null;
        
        // Skip whitespace
        int start = col + 1;
        while (start < body.length() && Character.isWhitespace(body.charAt(start))) start++;
        
        if (start >= body.length()) return null;
        
        // Check if it's a string value (starts with quote)
        if (body.charAt(start) == '"') {
            int firstQuote = start;
            int secondQuote = body.indexOf('"', firstQuote + 1);
            if (secondQuote < 0) return null;
            return body.substring(firstQuote + 1, secondQuote);
        } else {
            // It's a number or other value
            int end = start;
            while (end < body.length() && !",}".contains(String.valueOf(body.charAt(end)))) end++;
            return body.substring(start, end).trim();
        }
    }

    // ============ BOOKING HANDLERS ============

    static class ShowSeatsHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String query = exchange.getRequestURI().getQuery();
            String showId = null;
            if (query != null) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length == 2 && pair[0].equals("showId")) {
                        showId = pair[1];
                    }
                }
            }

            if (showId == null) {
                sendResponse(exchange, 400, "{\"success\":false,\"message\":\"showId required\"}");
                return;
            }

            Document show = null;
            try {
                show = showsDB.find(Filters.eq("_id", new ObjectId(showId))).first();
            } catch (Exception e) {
                sendResponse(exchange, 404, "{\"success\":false,\"message\":\"Invalid show ID\"}");
                return;
            }

            if (show == null) {
                sendResponse(exchange, 404, "{\"success\":false,\"message\":\"Show not found\"}");
                return;
            }

            String theatreId = show.getString("theatreId");
            int totalSeats = show.getInteger("totalSeats", 50);
            int price = show.getInteger("price", 200);
            int tax = 10;

            try {
                Document theatre = theatresDB.find(Filters.eq("_id", new ObjectId(theatreId))).first();
                if (theatre != null) tax = theatre.getInteger("tax", 10);
            } catch (Exception e) {}

            List<Integer> bookedSeats = show.getList("bookedSeats", Integer.class);
            if (bookedSeats == null) bookedSeats = new ArrayList<>();

            StringBuilder sb = new StringBuilder("{");
            sb.append("\"showId\":").append(escape(showId)).append(",");
            sb.append("\"totalSeats\":").append(totalSeats).append(",");
            sb.append("\"price\":").append(price).append(",");
            sb.append("\"tax\":").append(tax).append(",");
            sb.append("\"bookedSeats\":[");
            boolean first = true;
            for (Integer seat : bookedSeats) {
                if (!first) sb.append(",");
                first = false;
                sb.append(seat);
            }
            sb.append("]}");
            sendResponse(exchange, 200, sb.toString());
        }
    }

    static class BookSeatsHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            String body = readBody(exchange);
            Map<String, String> data = parseJson(body);
            
            String showId = data.get("showId");
            String userEmail = data.get("userEmail");
            String seatsStr = data.get("seats");

            if (showId == null || seatsStr == null || userEmail == null) {
                sendResponse(exchange, 400, "{\"success\":false,\"message\":\"Missing required fields\"}");
                return;
            }

            String[] seatParts = seatsStr.split(",");
            List<Integer> requestedSeats = new ArrayList<>();
            for (String s : seatParts) {
                try {
                    requestedSeats.add(Integer.parseInt(s.trim()));
                } catch (NumberFormatException e) {}
            }

            Document show = null;
            try {
                show = showsDB.find(Filters.eq("_id", new ObjectId(showId))).first();
            } catch (Exception e) {
                sendResponse(exchange, 404, "{\"success\":false,\"message\":\"Invalid show ID\"}");
                return;
            }

            if (show == null) {
                sendResponse(exchange, 404, "{\"success\":false,\"message\":\"Show not found\"}");
                return;
            }

            List<Integer> bookedSeats = show.getList("bookedSeats", Integer.class);
            if (bookedSeats == null) bookedSeats = new ArrayList<>();

            for (Integer seat : requestedSeats) {
                if (bookedSeats.contains(seat)) {
                    sendResponse(exchange, 400, "{\"success\":false,\"message\":\"Seat " + seat + " is already booked\"}");
                    return;
                }
            }

            List<Integer> allBooked = new ArrayList<>(bookedSeats);
            allBooked.addAll(requestedSeats);
            showsDB.updateOne(Filters.eq("_id", new ObjectId(showId)), Updates.set("bookedSeats", allBooked));

            int price = show.getInteger("price", 200);
            String theatreId = show.getString("theatreId");
            int tax = 10;
            String movieName = "Unknown", theatreName = "Unknown";

            try {
                Document theatre = theatresDB.find(Filters.eq("_id", new ObjectId(theatreId))).first();
                if (theatre != null) {
                    tax = theatre.getInteger("tax", 10);
                    theatreName = theatre.getString("name");
                }
            } catch (Exception e) {}

            try {
                Document movie = moviesDB.find(Filters.eq("_id", new ObjectId(show.getString("movieId")))).first();
                if (movie != null) movieName = movie.getString("name");
            } catch (Exception e) {}

            double subtotal = price * requestedSeats.size();
            double taxAmount = subtotal * tax / 100.0;
            double total = subtotal + taxAmount;

            Document booking = new Document()
                .append("showId", showId)
                .append("userEmail", userEmail)
                .append("seats", requestedSeats)
                .append("movieName", movieName)
                .append("theatreName", theatreName)
                .append("showtime", show.getString("showtime"))
                .append("subtotal", subtotal)
                .append("tax", taxAmount)
                .append("total", total)
                .append("createdAt", new java.util.Date());
            bookingsDB.insertOne(booking);

            System.out.println("[BOOKING] User " + userEmail + " booked seats " + requestedSeats + " for show " + showId);

            String resp = String.format(
                "{\"success\":true,\"message\":\"Booking successful\",\"booking\":{\"seats\":%d,\"subtotal\":%.2f,\"tax\":%.2f,\"total\":%.2f}}",
                requestedSeats.size(), subtotal, taxAmount, total
            );
            sendResponse(exchange, 201, resp);
        }
    }

    static class BookingsHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String query = exchange.getRequestURI().getQuery();
            String userEmail = null;
            if (query != null) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length == 2 && pair[0].equals("userEmail")) {
                        userEmail = java.net.URLDecoder.decode(pair[1], "UTF-8");
                    }
                }
            }

            FindIterable<Document> bookings;
            if (userEmail != null) {
                bookings = bookingsDB.find(Filters.eq("userEmail", userEmail));
            } else {
                bookings = bookingsDB.find();
            }

            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Document b : bookings) {
                if (!first) sb.append(",");
                first = false;

                java.util.Date createdAt = b.getDate("createdAt");
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm");
                String bookingDate = createdAt != null ? sdf.format(createdAt) : "Unknown";

                List<Integer> seats = b.getList("seats", Integer.class);

                sb.append("{");
                sb.append("\"id\":").append(escape(b.getObjectId("_id").toHexString())).append(",");
                sb.append("\"showId\":").append(escape(b.getString("showId"))).append(",");
                sb.append("\"movieName\":").append(escape(b.getString("movieName"))).append(",");
                sb.append("\"theatreName\":").append(escape(b.getString("theatreName"))).append(",");
                sb.append("\"showtime\":").append(escape(b.getString("showtime"))).append(",");
                sb.append("\"bookingDate\":").append(escape(bookingDate)).append(",");
                sb.append("\"seats\":").append(seats != null ? seats.toString() : "[]").append(",");
                sb.append("\"total\":").append(b.getDouble("total"));
                sb.append("}");
            }
            sb.append("]");
            sendResponse(exchange, 200, sb.toString());
        }
    }
}
