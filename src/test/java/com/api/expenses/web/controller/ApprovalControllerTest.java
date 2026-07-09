package com.api.expenses.web.controller;

import com.api.expenses.Helper;
import com.api.expenses.service.ApprovalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

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

    @Autowired
    private Helper helper;

    @Test
    @WithMockUser(roles = "APPROVER")
    void getPending_WithApprover_ReturnsOk() throws Exception {

        when(approvalService.getAllPendingClaims()).thenReturn(List.of(helper.buildDummyClaimResponse()));

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
