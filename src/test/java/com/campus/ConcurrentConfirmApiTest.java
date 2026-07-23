package com.campus;

import com.campus.appointment.service.AppointmentService;
import com.campus.common.BusinessException;
import com.campus.lock.DistributedLock;
import com.campus.lock.SynchronizedDistributedLock;
import com.campus.product.mapper.ProductMapper;
import com.campus.security.UserPrincipal;
import com.campus.user.entity.User;
import com.campus.user.mapper.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Two confirms race on the same product: per-key lock serializes entry (like Redis), CAS ensures one sold.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@Import(ConcurrentConfirmApiTest.LockTestConfig.class)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:concurrent_confirm_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
class ConcurrentConfirmApiTest {

    @TestConfiguration
    static class LockTestConfig {
        @Bean
        @Primary
        DistributedLock distributedLock() {
            return new SynchronizedDistributedLock();
        }
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    AppointmentService appointmentService;

    @Autowired
    UserMapper userMapper;

    @Autowired
    ProductMapper productMapper;

    @Test
    void concurrentConfirmOnlyOneSucceeds() throws Exception {
        String adminToken = login("admin", "admin1234");
        long catId = createCategory(adminToken);

        String sellerName = "cs_" + UUID.randomUUID().toString().substring(0, 8);
        String buyer1 = "cb1_" + UUID.randomUUID().toString().substring(0, 7);
        String buyer2 = "cb2_" + UUID.randomUUID().toString().substring(0, 7);
        register(sellerName);
        register(buyer1);
        register(buyer2);
        String sellerToken = login(sellerName, "password123");
        String buyer1Token = login(buyer1, "password123");
        String buyer2Token = login(buyer2, "password123");

        long productId = createAndApproveProduct(sellerToken, adminToken, catId);
        long appt1 = createAppointment(buyer1Token, productId);
        long appt2 = createAppointment(buyer2Token, productId);

        User seller = userMapper.findByUsername(sellerName);
        UserPrincipal sellerPrincipal = new UserPrincipal(seller, "concurrent-test");

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch go = new CountDownLatch(1);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger conflict = new AtomicInteger();
        List<String> unexpected = Collections.synchronizedList(new ArrayList<>());

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (long apptId : List.of(appt1, appt2)) {
                futures.add(pool.submit(() -> {
                    ready.countDown();
                    try {
                        go.await(5, TimeUnit.SECONDS);
                        appointmentService.updateStatus(apptId, "confirm", sellerPrincipal);
                        success.incrementAndGet();
                    } catch (Exception e) {
                        if (isConflict(e)) {
                            conflict.incrementAndGet();
                        } else {
                            unexpected.add(rootMessage(e));
                        }
                    }
                }));
            }
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            go.countDown();
            for (Future<?> f : futures) {
                f.get(10, TimeUnit.SECONDS);
            }
        } finally {
            pool.shutdownNow();
        }

        assertEquals(1, success.get(), "exactly one confirm should succeed; unexpected=" + unexpected);
        assertEquals(1, conflict.get(), "the other confirm should get 409; unexpected=" + unexpected);
        assertTrue(unexpected.isEmpty(), "no unexpected errors: " + unexpected);
        assertEquals("sold", productMapper.findById(productId).getStatus());

        mockMvc.perform(patch("/api/appointments/" + appt1 + "/")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("action", "confirm"))))
                .andExpect(status().isConflict());
    }

    private static boolean isConflict(Throwable e) {
        Throwable t = e;
        while (t != null) {
            if (t instanceof BusinessException be && be.getCode() == 409) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }

    private static String rootMessage(Throwable e) {
        Throwable t = e;
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t.getClass().getSimpleName() + ": " + t.getMessage();
    }

    private long createCategory(String adminToken) throws Exception {
        String name = "并发_" + UUID.randomUUID().toString().substring(0, 6);
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
                                "title", "并发键盘",
                                "description", "用于并发成交测试",
                                "price", 99,
                                "category", catId,
                                "contact_info", "wx-c"
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
