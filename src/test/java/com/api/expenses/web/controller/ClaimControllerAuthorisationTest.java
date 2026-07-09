package com.api.expenses.web.controller;

import com.api.expenses.config.SecurityConfig;
import com.api.expenses.controller.ClaimController;
import com.api.expenses.model.dto.ClaimRequest;
import com.api.expenses.model.dto.ClaimResponse;
import com.api.expenses.model.entity.Category;
import com.api.expenses.model.entity.ClaimStatus;
import com.api.expenses.security.JwtAuthenticationFilter;
import com.api.expenses.security.JwtService;
import com.api.expenses.security.UserDetailsServiceImpl;
import com.api.expenses.service.ClaimService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//TODO - Work on testing the security only
@WebMvcTest(controllers = ClaimController.class)
@EnableMethodSecurity(proxyTargetClass = true)
@Import({SecurityConfig.class, JwtService.class, JwtAuthenticationFilter.class})
public class ClaimControllerAuthorisationTest {

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private ClaimService expenseClaimService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void submitClaim_WithValidRequest_ReturnsCreated() throws Exception {
        ClaimResponse response = ClaimResponse.builder()
                .id(1)
                .employeeUserName("testuser")
                .description("Train to London")
                .amount(new BigDecimal("45.50"))
                .date(LocalDate.of(2024, 1, 15))
                .category(Category.TRAVEL)
                .status(ClaimStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        when(expenseClaimService.submitClaim(any(ClaimRequest.class), any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "    \"description\": \"Train to London\",\n" +
                                "    \"amount\": 45.50,\n" +
                                "    \"date\": \"2024-01-15\",\n" +
                                "    \"category\": \"TRAVEL\"\n" +
                                "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeUserName").value("testuser"))
                .andExpect(jsonPath("$.description").value("Train to London"))
                .andExpect(jsonPath("$.amount").value(45.50))
                .andExpect(jsonPath("$.category").value("TRAVEL"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andDo(print());
    }

}
