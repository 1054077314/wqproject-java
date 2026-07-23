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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class CategoryAppointmentTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void categoryAndAppointmentFlow() throws Exception {
        String adminToken = login("admin", "admin1234");
        String name = "测试分类_" + UUID.randomUUID().toString().substring(0, 6);

        MvcResult createdCat = mockMvc.perform(post("/api/admin/categories/")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", name, "sort_order", 10))))
                .andExpect(status().isCreated())
                .andReturn();
        long catId = objectMapper.readTree(createdCat.getResponse().getContentAsString()).path("data").path("id").asLong();

        String seller = "s_" + UUID.randomUUID().toString().substring(0, 8);
        String buyer = "b_" + UUID.randomUUID().toString().substring(0, 8);
        register(seller);
        register(buyer);
        String sellerToken = login(seller, "password123");
        String buyerToken = login(buyer, "password123");

        MvcResult product = mockMvc.perform(post("/api/products/")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "台灯",
                                "description", "学习用台灯",
                                "price", 35,
                                "category", catId,
                                "contact_info", "qq-1"
                        ))))
                .andExpect(status().isCreated())
                .andReturn();
        long productId = objectMapper.readTree(product.getResponse().getContentAsString()).path("data").path("id").asLong();

        mockMvc.perform(post("/api/admin/products/" + productId + "/review/")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("action", "approve"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/appointments/")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("product_id", productId))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/favorites/")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("product_id", productId))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/comments/")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("product_id", productId, "content", "还在吗"))))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/admin/categories/" + catId + "/")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("该分类下有商品，不可删除"));
    }

    private void register(String username) throws Exception {
        mockMvc.perform(post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("username", username, "password", "password123"))));
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
