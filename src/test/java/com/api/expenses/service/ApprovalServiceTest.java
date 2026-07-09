package com.api.expenses.service;

import com.api.expenses.model.dto.ClaimResponse;
import com.api.expenses.model.entity.Category;
import com.api.expenses.model.entity.ClaimStatus;
import com.api.expenses.model.entity.ExpenseClaim;
import com.api.expenses.model.entity.User;
import com.api.expenses.repository.ExpenseClaimRepository;
import com.api.expenses.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApprovalServiceTest {

    @Mock
    private ExpenseClaimRepository claimRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDetails currentUser;

    private ApprovalService approvalService;


    @BeforeEach
    void setUp() {
        ServiceHelper helper = new ServiceHelper(userRepository);
        approvalService = new ApprovalService(helper, claimRepository);
    }

    @Test
    void getAllPendingClaims_WithPendingClaims_ReturnsResponses() {
        User employee = new User();
        employee.setId(3);
        employee.setUsername("approver.owner");

        ExpenseClaim pending = ExpenseClaim.builder()
                .id(5)
                .employee(employee)
                .description("Conference travel")
                .amount(new BigDecimal("200.00"))
                .date(LocalDate.of(2025, 8, 8))
                .category(Category.TRAVEL)
                .status(ClaimStatus.PENDING)
                .createdAt(LocalDateTime.of(2025, Month.AUGUST, 8, 9, 0))
                .build();

        when(claimRepository.findAllByStatus(ClaimStatus.PENDING)).thenReturn(List.of(pending));

        List<ClaimResponse> responses = approvalService.getAllPendingClaims();

        assertNotNull(responses);
        assertEquals(1, responses.size());

        ClaimResponse resp = responses.get(0);
        assertEquals(5, resp.getId());
        assertEquals("approver.owner", resp.getEmployeeUserName());
        assertEquals(new BigDecimal("200.00"), resp.getAmount());
        assertEquals(Category.TRAVEL, resp.getCategory());
        assertEquals(ClaimStatus.PENDING, resp.getStatus());
    }

    @Test
    void getAllPendingClaims_WithNoPending_ReturnsEmptyList() {
        when(claimRepository.findAllByStatus(ClaimStatus.PENDING)).thenReturn(List.of());

        List<ClaimResponse> responses = approvalService.getAllPendingClaims();

        assertNotNull(responses);
        assertEquals(0, responses.size());
    }

}