package com.campus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class ProductApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    String userToken;
    String adminToken;
    Long categoryId;

    @BeforeEach
    void setUp() throws Exception {
        String username = "seller_" + UUID.randomUUID().toString().substring(0, 8);
        mockMvc.perform(post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("username", username, "password", "password123"))));
        userToken = login(username, "password123");
        adminToken = login("admin", "admin1234");

        MvcResult cats = mockMvc.perform(get("/api/categories/")).andReturn();
        JsonNode data = objectMapper.readTree(cats.getResponse().getContentAsString()).path("data");
        categoryId = data.get(0).path("id").asLong();
    }

    @Test
    void createRequiresAuthAndAppearsAfterApprove() throws Exception {
        mockMvc.perform(post("/api/products/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "二手自行车",
                                "description", "九成新校园通勤车",
                                "price", 199.5,
                                "category", categoryId,
                                "contact_info", "wechat-demo"
                        ))))
                .andExpect(status().isUnauthorized());

        MvcResult created = mockMvc.perform(post("/api/products/")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "二手自行车",
                                "description", "九成新校园通勤车",
                                "price", 199.5,
                                "category", categoryId,
                                "contact_info", "wechat-demo"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("pending"))
                .andReturn();

        long productId = objectMapper.readTree(created.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(get("/api/products/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.results").isArray());

        mockMvc.perform(post("/api/admin/products/" + productId + "/review/")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("action", "approve"))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/products/" + productId + "/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("active"));
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
