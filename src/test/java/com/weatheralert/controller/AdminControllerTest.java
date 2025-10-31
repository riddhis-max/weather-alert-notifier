package com.weatheralert.controller;

import com.weatheralert.entity.Subscriber;
import com.weatheralert.repository.SubscriberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private SubscriberRepository repo;
    @Autowired private JdbcTemplate jdbcTemplate;

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

    @Test
    void shouldShowZeroCountsWhenNoSubscribers() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(xpath("//div[contains(@class,'stats')]//p[1]/span").string("0"))
                .andExpect(xpath("//div[contains(@class,'stats')]//p[2]/span").string("0"));
    }

    @Test
    void shouldShowCorrectCountsAfterSubscribe() throws Exception {
        repo.save(Subscriber.builder().email("a@b.com").city("Berlin").build());
        repo.save(Subscriber.builder().email("c@d.com").city("").build());

        mockMvc.perform(get("/admin"))
                .andExpect(xpath("//div[contains(@class,'stats')]//p[1]/span").string("2"))
                .andExpect(xpath("//div[contains(@class,'stats')]//p[2]/span").string("1"));
    }

    @Test
    void shouldUpdateStatsAfterDelete() throws Exception {
        Subscriber sub = repo.save(Subscriber.builder().email("x@y.com").city("Hamburg").build());

        mockMvc.perform(post("/admin/delete/{id}", sub.getId()))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/admin"))
                .andExpect(xpath("//div[contains(@class,'stats')]//p[1]/span").string("0"))
                .andExpect(xpath("//div[contains(@class,'stats')]//p[2]/span").string("0"));
    }

    @Test
    void shouldShowNAForEmptyCity() throws Exception {
        jdbcTemplate.execute("INSERT INTO subscribers (email, city) VALUES ('empty@test.com', '')");
        mockMvc.perform(get("/admin"))
                .andExpect(xpath("//td[contains(text(),'empty@test.com')]/following-sibling::td").string("N/A"));
    }

    @Test
    void shouldShowCityWhenPresent() throws Exception {
        jdbcTemplate.execute("INSERT INTO subscribers (email, city) VALUES ('valid@test.com', 'Berlin')");
        mockMvc.perform(get("/admin"))
                .andExpect(xpath("//td[contains(text(),'valid@test.com')]/following-sibling::td").string("Berlin"));
    }

    @Test
    void shouldShowActiveCountCorrectly() throws Exception {
        jdbcTemplate.execute("INSERT INTO subscribers (email, city) VALUES ('a@test.com', 'Paris')");
        jdbcTemplate.execute("INSERT INTO subscribers (email, city) VALUES ('b@test.com', '')");

        mockMvc.perform(get("/admin"))
                .andExpect(xpath("//p[strong='Active:']/span").string("1"));
    }
}