package com.andrew.chatmodeldemo3.Controller;

// Import necessary classes for the controller functionality
import com.andrew.chatmodeldemo3.Model.Answer;
import com.andrew.chatmodeldemo3.Model.Question;
import com.andrew.chatmodeldemo3.Service.OpenAIService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller class that handles HTTP requests related to chat questions and answers.
 * This controller manages the routing for the chat interface and processes user questions.
 */
@Controller
public class QuestionController {

    // Dependency injection of the OpenAI service that will process the questions
    private final OpenAIService openAIService;

    /**
     * Constructor that initializes the controller with the OpenAI service.
     * Spring automatically injects the OpenAIService implementation.
     * 
     * @param openAIService The service that will process questions and generate answers
     */
    public QuestionController(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    /**
     * Handles GET requests to the root URL.
     * Displays the main chat page to the user.
     * 
     * @return The name of the view template to render (chat.html)
     */
    @GetMapping("/")
    public String ShowChatPage(){
        // Returns the name of the template to be rendered (chat.html)
        return "chat";
    }

    /**
     * Handles POST requests to /ask endpoint.
     * This endpoint is designed for API/AJAX calls that expect a JSON response.
     * 
     * @param question The Question object containing the user's query
     * @return An Answer object containing the AI-generated response
     */
    @PostMapping("/ask")
    public Answer getAnswer(@RequestBody Question question){
        // Forwards the question to the OpenAI service and returns the answer
        return openAIService.getAnswer(question);
    }

    /**
     * Handles POST requests to /askQuestion endpoint.
     * This endpoint is designed for form submissions from the chat page.
     * 
     * @param questionText The text of the user's question from the form
     * @param model The Spring Model object to add attributes for the view
     * @return The name of the view template to render (chat.html)
     */
    @PostMapping("/askQuestion")
    public String askQuestion(@RequestParam("question") String questionText, Model model){
        // Create a Question object from the submitted text
        Question question = new Question(questionText);

        // Get the answer from the OpenAI service
        Answer answer = openAIService.getAnswer(question);

        // Add both the question and answer to the model for display in the view
        model.addAttribute("question", questionText);
        model.addAttribute("answer", answer.answer());

        // Return to the chat page with the model attributes
        return "chat";
    }
}
