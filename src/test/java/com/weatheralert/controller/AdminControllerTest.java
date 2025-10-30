package com.weatheralert.controller;

import com.weatheralert.entity.Subscriber;
import com.weatheralert.repository.SubscriberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private SubscriberRepository repo;

    @BeforeEach
    void setUp() {
        repo.deleteAll();
    }

    @Test
    void shouldListSubscribers() throws Exception {
        repo.save(Subscriber.builder().email("a@b.com").city("Berlin").build());
        repo.save(Subscriber.builder().email("c@d.com").city("Munich").build());

        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(xpath("//table//tr").nodeCount(3))
                .andExpect(xpath("//td[text()='a@b.com']").exists());
    }

    @Test
    void shouldDeleteSubscriber() throws Exception {
        Subscriber sub = repo.save(Subscriber.builder().email("x@y.com").city("Hamburg").build());

        mockMvc.perform(post("/admin/delete/{id}", sub.getId()))  // ← CORRECT
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        assert repo.findAll().isEmpty();
    }

    @Test
    void shouldShowEmptyMessage() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("No subscribers yet")));
    }
}