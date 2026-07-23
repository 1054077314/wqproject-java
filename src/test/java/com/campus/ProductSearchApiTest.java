package com.campus;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class ProductSearchApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void listFiltersBySearchKeyword() throws Exception {
        String adminToken = login("admin", "admin1234");
        String seller = "ps_" + UUID.randomUUID().toString().substring(0, 8);
        mockMvc.perform(post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("username", seller, "password", "password123"))));
        String sellerToken = login(seller, "password123");

        String unique = "紫光" + UUID.randomUUID().toString().substring(0, 6);
        MvcResult cat = mockMvc.perform(post("/api/admin/categories/")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "搜_" + UUID.randomUUID().toString().substring(0, 4), "sort_order", 1))))
                .andExpect(status().isCreated())
                .andReturn();
        long catId = objectMapper.readTree(cat.getResponse().getContentAsString()).path("data").path("id").asLong();

        MvcResult product = mockMvc.perform(post("/api/products/")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", unique + "台灯",
                                "description", "书桌护眼灯",
                                "price", 35,
                                "category", catId,
                                "contact_info", "wx-s"
                        ))))
                .andExpect(status().isCreated())
                .andReturn();
        long productId = objectMapper.readTree(product.getResponse().getContentAsString()).path("data").path("id").asLong();
        mockMvc.perform(post("/api/admin/products/" + productId + "/review/")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("action", "approve"))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/products/").param("search", unique))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").value(1))
                .andExpect(jsonPath("$.data.results[0].title").value(org.hamcrest.Matchers.containsString(unique)));

        mockMvc.perform(get("/api/products/").param("search", "绝对不存在的关键词xyzzy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").value(0));
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
