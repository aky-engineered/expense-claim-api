package com.api.expenses.web.controller;

import com.api.expenses.model.dto.ClaimRequest;
import com.api.expenses.model.dto.ClaimResponse;
import com.api.expenses.model.entity.Category;
import com.api.expenses.model.entity.ClaimStatus;
import com.api.expenses.service.ExpenseClaimService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ClaimControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExpenseClaimService claimService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void submitClaim_WithValidRequest_ReturnsCreated() throws Exception {
        ClaimRequest request = ClaimRequest.builder()
                .description("Hotel for conference")
                .amount(new BigDecimal("150.00"))
                .date(LocalDate.of(2025, 01, 01))
                .category(Category.ACCOMODATION)
                .build();


        ClaimResponse response = ClaimResponse.builder()
                .id(1)
                .employeeUserName("testuser")
                .description("Hotel for conference")
                .amount(BigDecimal.TEN)
                .date(LocalDate.of(2025, 01, 01))
                .category(Category.ACCOMODATION)
                .status(ClaimStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        when(claimService.submitClaim(any(ClaimRequest.class), any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeUserName").value("testuser"))
                .andExpect(jsonPath("$.description").value("Hotel for conference"))
                .andExpect(jsonPath("$.amount").value(10))
                .andExpect(jsonPath("$.date").value("2025-01-01"))
                .andExpect(jsonPath("$.category").value("ACCOMODATION"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void submitClaim_WithInvalidAmount_ReturnsBadRequest() throws Exception {
        ClaimRequest request = ClaimRequest.builder()
                .description("Valid description")
                .amount(new BigDecimal("-50.00"))
                .date(LocalDate.now())
                .category(Category.TRAVEL)
                .build();

        mockMvc.perform(post("/api/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("amount: must be greater than 0")))
                .andDo(print());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void submitClaim_WithInvalidCategory_ReturnsBadRequest() throws Exception {
        String json = "{\"description\": \"Test\", \"amount\": 100, \"date\": \"2026-01-01\", \"category\": \"INVALID\"}";

        mockMvc.perform(post("/api/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("category: invalid value 'INVALID'")))
                .andDo(print());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void submitClaim_WithFutureDate_ReturnsBadRequest() throws Exception {
        ClaimRequest request = ClaimRequest.builder()
                .description("Future expense")
                .amount(new BigDecimal("100.00"))
                .date(LocalDate.now().plusDays(1))
                .category(Category.MEALS)
                .build();

        mockMvc.perform(post("/api/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("date: must be a date in the past or in the present")))
                .andDo(print());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void submitClaim_WithMissingCategory_ReturnsBadRequest() throws Exception {
        String json = "{\"description\": \"Test\", \"amount\": 100, \"date\": \"2026-01-01\"}";

        mockMvc.perform(post("/api/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("category: must not be null")))
                .andDo(print());
    }

    @Test
    void submitClaim_UserWithoutRole_ReturnsUnauthorized() throws Exception {
        ClaimRequest request = ClaimRequest.builder()
                .description("Test")
                .amount(new BigDecimal("100.00"))
                .date(LocalDate.now())
                .category(Category.TRAVEL)
                .build();

        mockMvc.perform(post("/api/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @Disabled("Look into how to test the authorisation")
    @WithMockUser(roles = "ADMIN")
    void submitClaim_UserWithIncorrectRole_ReturnsUnauthorized() throws Exception {
        ClaimRequest request = ClaimRequest.builder()
                .description("Test")
                .amount(new BigDecimal("100.00"))
                .date(LocalDate.now())
                .category(Category.TRAVEL)
                .build();

        mockMvc.perform(post("/api/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void getMyClaims_WithValidRequest_ReturnsOk() throws Exception {
        when(claimService.getMyClaims(null)).thenReturn(List.of(buildDummyClaimResponse()));

        mockMvc.perform(get("/api/claims"))
                .andExpect(status().isOk());
    }

    ClaimResponse buildDummyClaimResponse() {
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
}
