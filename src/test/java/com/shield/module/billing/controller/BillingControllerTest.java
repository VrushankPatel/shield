package com.shield.module.billing.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shield.audit.service.ApiRequestLogService;
import com.shield.audit.service.SystemLogService;
import com.shield.module.platform.service.PlatformRootService;
import com.shield.module.billing.dto.BillResponse;
import com.shield.module.billing.entity.BillStatus;
import com.shield.module.billing.service.BillingService;
import com.shield.security.jwt.JwtService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = BillingController.class)
@AutoConfigureMockMvc(addFilters = false)
class BillingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BillingService billingService;

    @MockBean
    private ApiRequestLogService apiRequestLogService;

    @MockBean
    private SystemLogService systemLogService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private PlatformRootService platformRootService;

    @Test
    void generateShouldReturnBillResponse() throws Exception {
        UUID billId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();

        when(billingService.generate(any())).thenReturn(new BillResponse(
                billId,
                tenantId,
                unitId,
                2,
                2026,
                BigDecimal.valueOf(2500),
                LocalDate.of(2026, 2, 28),
                BillStatus.PENDING,
                BigDecimal.valueOf(50)));

        mockMvc.perform(post("/api/v1/billing/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "unitId", unitId,
                                "month", 2,
                                "year", 2026,
                                "amount", 2500,
                                "dueDate", "2026-02-28",
                                "lateFee", 50))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Bill generated"))
                .andExpect(jsonPath("$.data.id").value(billId.toString()))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void getByUnitShouldReturnList() throws Exception {
        UUID billId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();

        when(billingService.getByUnit(unitId)).thenReturn(List.of(new BillResponse(
                billId,
                tenantId,
                unitId,
                1,
                2026,
                BigDecimal.valueOf(1800),
                LocalDate.of(2026, 1, 31),
                BillStatus.PAID,
                BigDecimal.ZERO)));

        mockMvc.perform(get("/api/v1/billing/unit/{unitId}", unitId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Bills fetched"))
                .andExpect(jsonPath("$.data[0].id").value(billId.toString()))
                .andExpect(jsonPath("$.data[0].status").value("PAID"));
    }
}
