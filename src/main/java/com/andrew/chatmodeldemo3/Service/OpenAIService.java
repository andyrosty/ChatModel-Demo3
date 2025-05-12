package com.andrew.chatmodeldemo3.Service;

import com.andrew.chatmodeldemo3.Model.Answer;
import com.andrew.chatmodeldemo3.Model.Question;

/**
 * Interface defining the contract for services that interact with OpenAI's API.
 * This interface follows the Spring service pattern, allowing for dependency injection
 * and making it easier to swap implementations or create mock versions for testing.
 * 
 * Any class implementing this interface must provide a method to process user questions
 * and return AI-generated answers.
 */
public interface OpenAIService {

    /**
     * Processes a user question and returns an AI-generated answer.
     * 
     * @param question The Question object containing the user's query
     * @return An Answer object containing the AI-generated response
     */
    Answer getAnswer(Question question);
}
