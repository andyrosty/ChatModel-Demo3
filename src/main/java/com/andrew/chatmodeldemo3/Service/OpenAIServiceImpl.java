package com.andrew.chatmodeldemo3.Service;

import com.andrew.chatmodeldemo3.Model.Answer;
import com.andrew.chatmodeldemo3.Model.Question;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

/**
 * Implementation of the OpenAIService interface that uses Spring AI to interact with OpenAI's API.
 * This service is responsible for sending user questions to the OpenAI API and processing the responses.
 * 
 * The @Service annotation marks this as a Spring service bean, making it available for dependency injection.
 */
@Service
public class OpenAIServiceImpl implements OpenAIService {

    // Spring AI's ChatModel that handles the communication with OpenAI's API
    private final ChatModel chatModel;

    /**
     * Constructor that initializes the service with a ChatModel.
     * Spring automatically injects the appropriate ChatModel implementation based on configuration.
     * 
     * @param chatModel The ChatModel implementation to use for API communication
     */
    public OpenAIServiceImpl(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * Processes a user question by sending it to OpenAI's API and returning the response.
     * 
     * @param question The Question object containing the user's query
     * @return An Answer object containing the AI-generated response
     */
    @Override
    public Answer getAnswer(Question question) {
        // Create a prompt template from the question text
        PromptTemplate promptTemplate = new PromptTemplate(question.question());

        // Generate a prompt from the template
        Prompt prompt = promptTemplate.create();

        // Call the OpenAI API through the ChatModel and get a response
        ChatResponse response = chatModel.call(prompt);

        // Extract the text from the response and create an Answer object
        return new Answer(response.getResult().getOutput().getText());
    }
}
