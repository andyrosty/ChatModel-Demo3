package com.andrew.chatmodeldemo3.Model;

/**
 * This record represents a movie from the movies500.csv file.
 * It contains fields for all the movie attributes in the CSV.
 * Java records automatically generate the constructor, getters, equals, hashCode, and toString methods.
 */
public record Movie(
    String title,
    String releaseDate,
    int year,
    String genres,
    String description,
    String director,
    String mainCast,
    int runtimeMinutes,
    String language,
    String country
) {
    // No additional methods needed as the record provides all necessary functionality
}