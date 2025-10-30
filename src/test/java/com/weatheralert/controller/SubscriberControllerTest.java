package com.weatheralert.controller;

import com.weatheralert.repository.SubscriberRepository;
import com.weatheralert.service.EmailService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SubscriberControllerTest {
    @Autowired private MockMvc mvc;
    @Autowired private SubscriberRepository repo;

    @MockBean
    private org.springframework.mail.javamail.JavaMailSender mailSender;

    @BeforeEach
    void setUp() {
        repo.deleteAll();  // ← ADD THIS
    }

    @AfterEach void tearDown() { repo.deleteAll(); }

    @Test void success() throws Exception {
        mvc.perform(post("/api/subscribe")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"a@b.com\",\"city\":\"Berlin\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Subscribed successfully!"));
        assertThat(repo.count()).isEqualTo(1);
    }

    @Test void invalidEmail() throws Exception {
        mvc.perform(post("/api/subscribe")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"bad\",\"city\":\"Berlin\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test void emptyCity() throws Exception {
        mvc.perform(post("/api/subscribe")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"a@b.com\",\"city\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}
