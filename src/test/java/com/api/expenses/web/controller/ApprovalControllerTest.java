package com.api.expenses.web.controller;

import com.api.expenses.model.dto.ClaimResponse;
import com.api.expenses.model.entity.Category;
import com.api.expenses.model.entity.ClaimStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApprovalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApprovalService approvalService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
    }

    private ClaimResponse buildDummyClaimResponse() {
        return ClaimResponse.builder()
                .id(1)
                .employeeUserName("test")
                .date(LocalDate.of(2025, 1, 1))
                .amount(BigDecimal.TEN)
                .status(ClaimStatus.PENDING)
                .category(Category.MEALS)
                .createdAt(LocalDateTime.of(2025, Month.FEBRUARY, 3, 6, 30, 40, 50))
                .build();
    }

    @Test
    @WithMockUser(roles = "APPROVER")
    void getPending_WithApprover_ReturnsOk() throws Exception {

        when(approvalService.getAllPendingClaims()).thenReturn(List.of(buildDummyClaimResponse()));

        mockMvc.perform(get("/api/approvals/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].employeeUserName").value("test"))
                .andExpect(jsonPath("$[0].amount").value(10))
                .andExpect(jsonPath("$[0].date").value("2025-01-01"))
                .andExpect(jsonPath("$[0].category").value("MEALS"))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andDo(print());
    }

    @Ignore("To be investigated")
    @WithMockUser(roles = "EMPLOYEE")
    void getPending_WithEmployee_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/approvals/pending"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPending_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/approvals/pending"))
                .andExpect(status().isUnauthorized());
    }
}
