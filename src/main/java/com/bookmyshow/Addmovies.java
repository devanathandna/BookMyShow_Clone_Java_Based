import java.util.*;

public class Addmovies {
    private static List<Map<String, String>> moviesDB = Collections.synchronizedList(new ArrayList<>());
    private static int movieIdCounter = 1;

    // Add a new movie
    public static Map<String, String> addMovie(String name, String genre, String duration, String language, String description) {
        Map<String, String> movie = new HashMap<>();
        movie.put("id", String.valueOf(movieIdCounter++));
        movie.put("name", name);
        movie.put("genre", genre);
        movie.put("duration", duration);
        movie.put("language", language);
        movie.put("description", description);
        moviesDB.add(movie);
        System.out.println("[MOVIE ADDED] " + name);
        return movie;
    }

    // Get all movies
    public static List<Map<String, String>> getAllMovies() {
        return new ArrayList<>(moviesDB);
    }

    // Get movie by ID
    public static Map<String, String> getMovieById(String id) {
        for (Map<String, String> movie : moviesDB) {
            if (movie.get("id").equals(id)) {
                return movie;
            }
        }
        return null;
    }

    // Delete movie by ID
    public static boolean deleteMovie(String id) {
        return moviesDB.removeIf(movie -> movie.get("id").equals(id));
    }

    // Get movies count
    public static int getMoviesCount() {
        return moviesDB.size();
    }
}
