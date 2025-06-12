package com.andrew.chatmodeldemo3.Controller;

import com.andrew.chatmodeldemo3.Model.Answer;
import com.andrew.chatmodeldemo3.Model.Question;
import com.andrew.chatmodeldemo3.Service.MovieSearchService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller class that handles HTTP requests related to movie search.
 * This controller manages the routing for the search interface and processes user queries.
 */
@Controller
public class MovieSearchController {
    private final MovieSearchService movieSearchService;

    /**
     * Constructor that initializes the controller with the MovieSearchService.
     * 
     * @param movieSearchService The service that will process search queries and generate answers
     */
    public MovieSearchController(MovieSearchService movieSearchService) {
        this.movieSearchService = movieSearchService;
    }

    /**
     * Handles GET requests to the /search endpoint.
     * Displays the movie search page to the user.
     * 
     * @return The name of the view template to render (search.html)
     */
    @GetMapping("/search")
    public String showSearchPage() {
        return "search";
    }

    /**
     * Handles POST requests to /searchMovies endpoint.
     * This endpoint processes movie search queries from the search page.
     * 
     * @param questionText The text of the user's search query from the form
     * @param model The Spring Model object to add attributes for the view
     * @return The name of the view template to render (search.html)
     */
    @PostMapping("/searchMovies")
    public String searchMovies(@RequestParam("question") String questionText, Model model) {
        // Create a Question object from the submitted text
        Question question = new Question(questionText);

        // Get the answer from the movie search service
        Answer answer = movieSearchService.searchMovies(question);

        // Add both the question and answer to the model for display in the view
        model.addAttribute("question", questionText);
        model.addAttribute("answer", answer.answer());

        // Return to the search page with the model attributes
        return "search";
    }
}
