# ChatModel-Demo3

A Spring Boot application that integrates with OpenAI's API to create an interactive chat assistant. This application allows users to ask questions through a web interface and receive AI-generated responses.

## Table of Contents

- [Features](#features)
- [Technologies Used](#technologies-used)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Setup and Installation](#setup-and-installation)
- [Configuration](#configuration)
- [Usage](#usage)
- [API Documentation](#api-documentation)
- [Screenshots](#screenshots)
- [Contributing](#contributing)
- [License](#license)

## Features

- Clean, responsive web interface for chatting with an AI assistant
- Real-time AI responses using OpenAI's API
- Support for Markdown rendering in AI responses
- Visual typing indicator while waiting for responses
- Mobile-friendly design

## Technologies Used

- **Java 21**: Core programming language
- **Spring Boot 3.4.5**: Application framework
- **Spring AI**: Integration with AI models
- **Thymeleaf**: Server-side Java template engine
- **HTML/CSS/JavaScript**: Frontend technologies
- **Maven**: Dependency management and build tool
- **OpenAI API**: AI model provider
- **Showdown.js**: Markdown to HTML conversion

## Project Structure

```
src/main/java/com/andrew/chatmodeldemo3/
├── Controller/
│   └── QuestionController.java    # Handles HTTP requests
├── Model/
│   ├── Answer.java                # Data model for AI responses
│   └── Question.java              # Data model for user questions
├── Service/
│   ├── OpenAIService.java         # Service interface
│   └── OpenAIServiceImpl.java     # Service implementation
└── ChatModelDemo3Application.java # Main application class

src/main/resources/
├── application.properties         # Application configuration
└── templates/
    └── chat.html                  # Thymeleaf template for the chat UI
```

## Prerequisites

- Java Development Kit (JDK) 21 or later
- Maven 3.6 or later
- OpenAI API key

## Setup and Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/ChatModel-Demo3.git
   cd ChatModel-Demo3
   ```

2. Set your OpenAI API key as an environment variable:
   ```bash
   # For Linux/macOS
   export OPENAI_API_KEY=your_api_key_here
   
   # For Windows
   set OPENAI_API_KEY=your_api_key_here
   ```

3. Build the application:
   ```bash
   mvn clean install
   ```

4. Run the application:
   ```bash
   mvn spring-boot:run
   ```

5. Access the application in your web browser at `http://localhost:8080`

## Configuration

The application is configured through the `application.properties` file:

```properties
# Application name
spring.application.name=ChatModel-Demo3

# OpenAI API configuration
spring.ai.openai.api-key=${OPENAI_API_KEY}
```

You can customize the application by adding additional properties as needed.

## Usage

1. Open your web browser and navigate to `http://localhost:8080`
2. You'll see the chat interface with a welcome message from the AI assistant
3. Type your question in the input field at the bottom of the screen
4. Press the "Send" button or hit Enter to submit your question
5. Wait for the AI to generate a response (a typing indicator will be displayed)
6. View the AI's response, which may include formatted text if Markdown was used

## API Documentation

The application provides two endpoints for interacting with the AI:

### Web Interface Endpoint

- **URL**: `/askQuestion`
- **Method**: POST
- **Parameters**: 
  - `question` (form parameter): The user's question text
- **Response**: Renders the chat.html template with the question and answer

### REST API Endpoint

- **URL**: `/ask`
- **Method**: POST
- **Request Body**: JSON object with a `question` field
  ```json
  {
    "question": "What is Spring Boot?"
  }
  ```
- **Response**: JSON object with an `answer` field
  ```json
  {
    "answer": "Spring Boot is a framework that simplifies the development of Spring applications..."
  }
  ```

## Screenshots

[Include screenshots of your application here]

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

[Specify your license here]