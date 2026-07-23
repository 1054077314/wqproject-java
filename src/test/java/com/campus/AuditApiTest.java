package com.campus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class AuditApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void auditLogsSupportActionFilterAndPagination() throws Exception {
        String adminToken = login("admin", "admin1234");

        // Generate a user.disable audit entry
        String username = "au_" + UUID.randomUUID().toString().substring(0, 8);
        mockMvc.perform(post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("username", username, "password", "password123"))));
        MvcResult users = mockMvc.perform(get("/api/admin/users/")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page_size", "100"))
                .andExpect(status().isOk())
                .andReturn();
        long userId = -1;
        for (JsonNode row : objectMapper.readTree(users.getResponse().getContentAsString()).path("data").path("results")) {
            if (username.equals(row.path("username").asText())) {
                userId = row.path("id").asLong();
                break;
            }
        }
        assertTrue(userId > 0);
        mockMvc.perform(put("/api/admin/users/" + userId + "/")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("is_active", false))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/audit-logs/")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("action", "user.disable")
                        .param("page", "1")
                        .param("page_size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").isNumber())
                .andExpect(jsonPath("$.data.results").isArray())
                .andExpect(jsonPath("$.data.results[0].action").value("user.disable"));

        mockMvc.perform(get("/api/admin/audit-logs/")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "1")
                        .param("page_size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.results.length()", org.hamcrest.Matchers.lessThanOrEqualTo(2)));
    }

    private String login(String username, String password) throws Exception {
        MvcResult login = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(login.getResponse().getContentAsString()).path("data").path("token").asText();
    }
}
