package com.campus;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:pagination_seed_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.flyway.locations=classpath:db/migration,classpath:db/data"
})
class PaginationSeedDataApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void productListKeepsTotalAfterVoMapping() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/products/")
                        .param("page", "1")
                        .param("page_size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.results.length()").value(5))
                .andExpect(jsonPath("$.data.next").isNotEmpty())
                .andReturn();

        long count = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("count").asLong();
        assertThat(count).isGreaterThan(5);
    }
}
