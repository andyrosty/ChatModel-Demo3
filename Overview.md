 # Overview

 This document provides a high-level UML class diagram illustrating the main classes,
 their packages, and relationships in the ChatModelDemo3 application.

 ```mermaid
 classDiagram
     %% Package and class definitions
     package com.andrew.chatmodeldemo3 {
         class ChatModelDemo3Application {
             +main(String[] args)
         }
         package Controller {
             class QuestionController {
                 -OpenAIService openAIService
                 +String ShowChatPage()
                 +Answer getAnswer(Question)
                 +String askQuestion(String questionText, Model)
             }
         }
         package Service {
             interface OpenAIService {
                 +Answer getAnswer(Question)
             }
             class OpenAIServiceImpl {
                 -ChatModel chatModel
                 +Answer getAnswer(Question)
             }
         }
         package Model {
             class Question {
                 +String question
             }
             class Answer {
                 +String answer
             }
         }
     }
     
     %% Relationships
     QuestionController ..> OpenAIService : <<uses>>
     OpenAIService <|-- OpenAIServiceImpl : implements
     OpenAIServiceImpl --> ChatModel : <<uses>><<external>>
     QuestionController --> Question : <<request>>
     QuestionController --> Answer : <<response>>
     OpenAIServiceImpl --> Question : <<parameter>>
     OpenAIServiceImpl --> Answer : <<returns>>
 ```