package com.andrew.chatmodeldemo3.Controller;

import com.andrew.chatmodeldemo3.Model.Question;
import com.andrew.chatmodeldemo3.Model.Answer;
import com.andrew.chatmodeldemo3.Service.OpenAIService;
import com.andrew.chatmodeldemo3.Controller.QuestionController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web MVC tests for QuestionController, mocking OpenAIService responses.
 */
@WebMvcTest(QuestionController.class)
class QuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OpenAIService openAIService;

    @Test
    void showChatPage_ReturnsChatView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("chat"));
    }

    @Test
    void getAnswer_ReturnsAnswerJson() throws Exception {
        when(openAIService.getAnswer(new Question("Hello")))
                .thenReturn(new Answer("Hi"));

        mockMvc.perform(post("/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"Hello\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("Hi"));
    }

    @Test
    void askQuestion_ReturnsChatViewWithModel() throws Exception {
        when(openAIService.getAnswer(new Question("Hello Form")))
                .thenReturn(new Answer("Form Answer"));

        mockMvc.perform(post("/askQuestion")
                        .param("question", "Hello Form"))
                .andExpect(status().isOk())
                .andExpect(view().name("chat"))
                .andExpect(model().attribute("question", "Hello Form"))
                .andExpect(model().attribute("answer", "Form Answer"));
    }
}