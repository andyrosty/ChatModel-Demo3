package com.andrew.chatmodeldemo3.Model;

/**
 * This record represents a question submitted by a user.
 * It contains a single field 'question' of type String.
 * Java records automatically generate the constructor, getters, equals, hashCode, and toString methods.
 * This makes it easy to create and manipulate instances of Question without writing boilerplate code.
 * 
 * This class is used throughout the application to encapsulate user queries before they are sent to the OpenAI service.
 */
public record Question(String question) {
    // No additional methods needed as the record provides all necessary functionality
}
