package com.andrew.chatmodeldemo3.Service;

import com.andrew.chatmodeldemo3.Model.Answer;
import com.andrew.chatmodeldemo3.Model.Movie;
import com.andrew.chatmodeldemo3.Model.Question;
import com.andrew.chatmodeldemo3.Repository.MovieRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class that implements RAG (Retrieval-Augmented Generation) for movie search.
 * This service retrieves relevant movies based on the user's query, formats the data
 * for the RAG prompt, and uses the ChatModel to generate a response.
 */
@Service
public class MovieSearchService {
    private final MovieRepository movieRepository;
    private final ChatModel chatModel;
    private final String promptTemplateContent;

    /**
     * Constructor that initializes the service with the necessary dependencies.
     * 
     * @param movieRepository Repository for accessing movie data
     * @param chatModel ChatModel for generating responses
     */
    public MovieSearchService(MovieRepository movieRepository, ChatModel chatModel) {
        this.movieRepository = movieRepository;
        this.chatModel = chatModel;
        
        // Load the RAG prompt template
        try {
            ClassPathResource resource = new ClassPathResource("templates/rag-prompt-template.st");
            this.promptTemplateContent = new String(Files.readAllBytes(Path.of(resource.getURI())));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load RAG prompt template", e);
        }
    }

    /**
     * Searches for movies based on the user's question and generates a response using RAG.
     * 
     * @param question The user's question
     * @return An Answer object containing the AI-generated response
     */
    public Answer searchMovies(Question question) {
        // Search for relevant movies based on the query
        List<Movie> relevantMovies = movieRepository.searchMovies(question.question());
        
        // Convert movies to a string format for the RAG prompt
        String movieDocuments = relevantMovies.stream()
            .map(movie -> String.format(
                "Title: %s\nYear: %d\nGenres: %s\nDirector: %s\nCast: %s\nDescription: %s\n",
                movie.title(), movie.year(), movie.genres(), movie.director(), 
                movie.mainCast(), movie.description()
            ))
            .collect(Collectors.joining("\n---\n"));
        
        // Create the prompt with the user's question and the relevant movie data
        Map<String, Object> variables = new HashMap<>();
        variables.put("input", question.question());
        variables.put("documents", movieDocuments);
        
        PromptTemplate promptTemplate = new PromptTemplate(promptTemplateContent);
        Prompt prompt = promptTemplate.create(variables);
        
        // Call the AI model with the RAG prompt
        String response = chatModel.call(prompt).getResult().getOutput().getText();
        
        return new Answer(response);
    }
}