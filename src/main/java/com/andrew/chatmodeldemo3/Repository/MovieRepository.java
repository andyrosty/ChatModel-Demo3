package com.andrew.chatmodeldemo3.Repository;

import com.andrew.chatmodeldemo3.Model.Movie;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository class for loading and querying movie data from the movies500.csv file.
 * This class is responsible for loading the CSV data into memory and providing
 * methods to search for movies based on various criteria.
 */
@Repository
public class MovieRepository {
    private final List<Movie> movies = new ArrayList<>();

    /**
     * Constructor that loads the movie data from the CSV files when the repository is created.
     */
    public MovieRepository() {
        // Load movies from the original CSV file
        loadMoviesFromCsv("movies500.csv");

        // Load additional movies from the new CSV file
        loadMoviesFromCsv("movies_additional.csv");
    }

    /**
     * Loads movie data from a specified CSV file and parses it into Movie objects.
     * Uses a robust approach to handle CSV parsing with quoted fields.
     * 
     * @param csvFileName The name of the CSV file to load
     */
    private void loadMoviesFromCsv(String csvFileName) {
        try {
            ClassPathResource resource = new ClassPathResource(csvFileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));

            // Skip header line
            String line = reader.readLine();
            int movieCount = 0;

            while ((line = reader.readLine()) != null) {
                try {
                    // Use regex to handle commas within quoted fields
                    String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                    if (data.length >= 10) {
                        Movie movie = new Movie(
                            data[0].replace("\"", ""),
                            data[1].replace("\"", ""),
                            Integer.parseInt(data[2]),
                            data[3].replace("\"", ""),
                            data[4].replace("\"", ""),
                            data[5].replace("\"", ""),
                            data[6].replace("\"", ""),
                            Integer.parseInt(data[7]),
                            data[8].replace("\"", ""),
                            data[9].replace("\"", "")
                        );
                        movies.add(movie);
                        movieCount++;
                    }
                } catch (NumberFormatException e) {
                    // Skip rows with invalid number format
                    System.err.println("Error parsing movie data: " + e.getMessage() + " in line: " + line);
                }
            }

            System.out.println("Loaded " + movieCount + " movies from " + csvFileName);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load movies from " + csvFileName + ": " + e.getMessage());
        }
    }

    /**
     * Returns all movies in the repository.
     * 
     * @return A list of all Movie objects
     */
    public List<Movie> getAllMovies() {
        return movies;
    }

    /**
     * Searches for movies based on a query string.
     * The search is performed on title, genres, director, main cast, description, and year.
     * 
     * @param query The search query
     * @return A list of Movie objects that match the query
     */
    public List<Movie> searchMovies(String query) {
        String lowerCaseQuery = query.toLowerCase();

        // Try to parse the query as a year
        int year = 0;
        try {
            year = Integer.parseInt(lowerCaseQuery);
        } catch (NumberFormatException e) {
            // Not a year query, continue with normal search
        }

        // Use a final variable for the year to use in the lambda
        final int searchYear = year;

        return movies.stream()
            .filter(movie -> 
                movie.title().toLowerCase().contains(lowerCaseQuery) ||
                movie.genres().toLowerCase().contains(lowerCaseQuery) ||
                movie.director().toLowerCase().contains(lowerCaseQuery) ||
                movie.mainCast().toLowerCase().contains(lowerCaseQuery) ||
                movie.description().toLowerCase().contains(lowerCaseQuery) ||
                (searchYear > 0 && movie.year() == searchYear) // Search by exact year match if query is a year
            )
            .toList();
    }
}
