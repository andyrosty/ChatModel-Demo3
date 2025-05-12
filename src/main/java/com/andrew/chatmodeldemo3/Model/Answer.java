package com.andrew.chatmodeldemo3.Model;

public record Answer(String answer) {
    // This record represents an answer to a question.
    // It contains a single field 'answer' of type String.
    // The record automatically generates the constructor, getters, equals, hashCode, and toString methods.
    // This makes it easy to create and manipulate instances of Answer without writing boilerplate code.
}
