package com.cedarmarket.orders;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class OrderFlowTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void listsSeededProducts() throws Exception {
        mvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(6)));
    }

    @Test
    void placingAnOrderCalculatesTotalAndReducesStock() throws Exception {
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content("""
                        {"customerEmail":"lina@example.com",
                         "items":[{"productId":1,"quantity":2},{"productId":2,"quantity":1}]}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.total").value(29.75));

        mvc.perform(get("/api/products/1"))
                .andExpect(jsonPath("$.stock").value(38));
    }

    @Test
    void rejectsOrderWhenStockIsTooLow() throws Exception {
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content("""
                        {"customerEmail":"lina@example.com","items":[{"productId":6,"quantity":999}]}"""))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsInvalidOrder() throws Exception {
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content("""
                        {"customerEmail":"not-an-email","items":[]}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.customerEmail").exists());
    }

    @Test
    void cancellingAnOrderRestoresStock() throws Exception {
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content("""
                        {"customerEmail":"omar@example.com","items":[{"productId":4,"quantity":5}]}"""))
                .andExpect(status().isCreated());

        mvc.perform(patch("/api/orders/1/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CANCELLED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        mvc.perform(get("/api/products/4"))
                .andExpect(jsonPath("$.stock").value(50));
    }

    @Test
    void shippedOrderCannotBeCancelled() throws Exception {
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content("""
                        {"customerEmail":"omar@example.com","items":[{"productId":3,"quantity":1}]}"""))
                .andExpect(status().isCreated());
        mvc.perform(patch("/api/orders/1/status").contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"PAID\"}")).andExpect(status().isOk());
        mvc.perform(patch("/api/orders/1/status").contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"SHIPPED\"}")).andExpect(status().isOk());
        mvc.perform(patch("/api/orders/1/status").contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"CANCELLED\"}")).andExpect(status().isConflict());
    }

    @Test
    void productWithOrdersCannotBeDeleted() throws Exception {
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content("""
                        {"customerEmail":"omar@example.com","items":[{"productId":5,"quantity":1}]}"""))
                .andExpect(status().isCreated());
        mvc.perform(delete("/api/products/5")).andExpect(status().isConflict());
        mvc.perform(delete("/api/products/2")).andExpect(status().isNoContent());
    }

    @Test
    void unknownOrderReturns404() throws Exception {
        mvc.perform(get("/api/orders/999")).andExpect(status().isNotFound());
    }
}
