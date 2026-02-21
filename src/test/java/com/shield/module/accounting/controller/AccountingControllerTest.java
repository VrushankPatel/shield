package com.shield.module.accounting.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shield.audit.service.ApiRequestLogService;
import com.shield.audit.service.SystemLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.module.platform.service.PlatformRootService;
import com.shield.module.accounting.dto.LedgerResponse;
import com.shield.module.accounting.dto.LedgerSummaryResponse;
import com.shield.module.accounting.entity.LedgerType;
import com.shield.module.accounting.service.AccountingService;
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

@WebMvcTest(controllers = AccountingController.class)
@AutoConfigureMockMvc(addFilters = false)
class AccountingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountingService accountingService;

    @MockBean
    private ApiRequestLogService apiRequestLogService;

    @MockBean
    private SystemLogService systemLogService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private PlatformRootService platformRootService;

    @Test
    void createShouldReturnWrappedResponse() throws Exception {
        UUID id = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        LedgerResponse response = new LedgerResponse(
                id,
                tenantId,
                LedgerType.INCOME,
                "MAINTENANCE",
                BigDecimal.valueOf(1500),
                "REF-1",
                "collection",
                LocalDate.of(2026, 2, 21));

        when(accountingService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/ledger")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "type", "INCOME",
                                "category", "MAINTENANCE",
                                "amount", 1500,
                                "reference", "REF-1",
                                "description", "collection",
                                "entryDate", "2026-02-21"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Ledger entry created"))
                .andExpect(jsonPath("$.data.id").value(id.toString()));
    }

    @Test
    void listShouldReturnPagedResponse() throws Exception {
        UUID id = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        LedgerResponse row = new LedgerResponse(
                id,
                tenantId,
                LedgerType.EXPENSE,
                "REPAIR",
                BigDecimal.valueOf(800),
                "REF-2",
                "repair expense",
                LocalDate.of(2026, 2, 20));

        PagedResponse<LedgerResponse> page = new PagedResponse<>(
                List.of(row),
                0,
                20,
                1,
                1,
                true,
                true);

        when(accountingService.list(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/ledger?page=0&size=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(id.toString()))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void summaryShouldReturnTotals() throws Exception {
        when(accountingService.summary()).thenReturn(new LedgerSummaryResponse(
                BigDecimal.valueOf(5000),
                BigDecimal.valueOf(1200),
                BigDecimal.valueOf(3800)));

        mockMvc.perform(get("/api/v1/ledger/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Ledger summary fetched"))
                .andExpect(jsonPath("$.data.totalIncome").value(5000))
                .andExpect(jsonPath("$.data.totalExpense").value(1200))
                .andExpect(jsonPath("$.data.balance").value(3800));
    }
}
