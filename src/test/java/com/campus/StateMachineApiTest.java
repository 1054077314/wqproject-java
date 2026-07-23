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
class StateMachineApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void confirmSellsProductRejectsOtherPendingAndBlocksCancel() throws Exception {
        String adminToken = login("admin", "admin1234");
        long catId = createCategory(adminToken);

        String seller = "s_" + UUID.randomUUID().toString().substring(0, 8);
        String buyer1 = "b1_" + UUID.randomUUID().toString().substring(0, 7);
        String buyer2 = "b2_" + UUID.randomUUID().toString().substring(0, 7);
        register(seller);
        register(buyer1);
        register(buyer2);
        String sellerToken = login(seller, "password123");
        String buyer1Token = login(buyer1, "password123");
        String buyer2Token = login(buyer2, "password123");

        long productId = createAndApproveProduct(sellerToken, adminToken, catId);

        long appt1 = createAppointment(buyer1Token, productId);
        long appt2 = createAppointment(buyer2Token, productId);

        mockMvc.perform(patch("/api/appointments/" + appt1 + "/")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("action", "confirm"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("confirmed"));

        mockMvc.perform(get("/api/products/" + productId + "/")
                        .header("Authorization", "Bearer " + sellerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("sold"));

        mockMvc.perform(get("/api/products/" + productId + "/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("sold"));

        mockMvc.perform(get("/api/my-appointments/as-seller/")
                        .header("Authorization", "Bearer " + sellerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.results[?(@.id == " + appt2 + ")].status").value("rejected"));

        // Confirmed = deal locked — buyer cannot cancel
        mockMvc.perform(patch("/api/appointments/" + appt1 + "/")
                        .header("Authorization", "Bearer " + buyer1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("action", "cancel"))))
                .andExpect(status().isBadRequest());

        // Double confirm → optimistic conflict
        mockMvc.perform(patch("/api/appointments/" + appt1 + "/")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("action", "confirm"))))
                .andExpect(status().isConflict());

        // Sold product cannot be appointed again
        mockMvc.perform(post("/api/appointments/")
                        .header("Authorization", "Bearer " + buyer2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("product_id", productId))))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/products/" + productId + "/")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("title", "成交后修改"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancelThenReopenAppointment() throws Exception {
        String adminToken = login("admin", "admin1234");
        long catId = createCategory(adminToken);
        String seller = "s_" + UUID.randomUUID().toString().substring(0, 8);
        String buyer = "b_" + UUID.randomUUID().toString().substring(0, 8);
        register(seller);
        register(buyer);
        String sellerToken = login(seller, "password123");
        String buyerToken = login(buyer, "password123");
        long productId = createAndApproveProduct(sellerToken, adminToken, catId);

        long apptId = createAppointment(buyerToken, productId);

        mockMvc.perform(patch("/api/appointments/" + apptId + "/")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("action", "cancel"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("cancelled"));

        mockMvc.perform(post("/api/appointments/")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("product_id", productId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("pending"))
                .andExpect(jsonPath("$.data.id").value((int) apptId));
    }

    private long createCategory(String adminToken) throws Exception {
        String name = "分类_" + UUID.randomUUID().toString().substring(0, 6);
        MvcResult created = mockMvc.perform(post("/api/admin/categories/")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", name, "sort_order", 1))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(created.getResponse().getContentAsString()).path("data").path("id").asLong();
    }

    private long createAndApproveProduct(String sellerToken, String adminToken, long catId) throws Exception {
        MvcResult product = mockMvc.perform(post("/api/products/")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "键盘",
                                "description", "机械键盘",
                                "price", 120,
                                "category", catId,
                                "contact_info", "wx-1"
                        ))))
                .andExpect(status().isCreated())
                .andReturn();
        long productId = objectMapper.readTree(product.getResponse().getContentAsString()).path("data").path("id").asLong();
        mockMvc.perform(post("/api/admin/products/" + productId + "/review/")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("action", "approve"))))
                .andExpect(status().isOk());
        return productId;
    }

    private long createAppointment(String buyerToken, long productId) throws Exception {
        MvcResult r = mockMvc.perform(post("/api/appointments/")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("product_id", productId))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(r.getResponse().getContentAsString()).path("data").path("id").asLong();
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
