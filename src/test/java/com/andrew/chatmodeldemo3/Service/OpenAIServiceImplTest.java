package com.andrew.chatmodeldemo3.Service;

import com.andrew.chatmodeldemo3.Model.Question;
import com.andrew.chatmodeldemo3.Model.Answer;
import com.andrew.chatmodeldemo3.Service.OpenAIServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OpenAIServiceImpl, mocking the ChatModel to avoid external API calls.
 */
@ExtendWith(MockitoExtension.class)
class OpenAIServiceImplTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatModel chatModel;

    @InjectMocks
    private OpenAIServiceImpl openAIService;

    @Test
    void getAnswer_ReturnsExpectedAnswer() {
        // Mock the chained call to return a fixed response text
        when(chatModel.call(any(Prompt.class))
                .getResult().getOutput().getText())
                .thenReturn("Mocked OpenAI response");

        Question question = new Question("Hello OpenAI");
        Answer answer = openAIService.getAnswer(question);

        assertNotNull(answer, "Answer should not be null");
        assertEquals("Mocked OpenAI response", answer.answer(), "Response text should match mock");
    }
}