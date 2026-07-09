package com.api.expenses.service;

import com.api.expenses.model.dto.ClaimRequest;
import com.api.expenses.model.entity.Category;
import com.api.expenses.model.entity.ClaimResponse;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseClaimServiceTest {

    @Mock
    private ExpenseClaimRepository claimRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDetails currentUser;

    private ExpenseClaimService claimService;

    @BeforeEach
    void setUp() {
        claimService = new ExpenseClaimService(claimRepository, userRepository);
    }

    @Test
    void submitClaim_WithValidRequest_ClaimSubmittedSuccessfully() {
        ClaimRequest request = ClaimRequest.builder()
                .description("Hotel accommodation")
                .amount(new BigDecimal("150.50"))
                .date(LocalDate.now())
                .category(Category.ACCOMODATION)
                .build();

        User employee = new User();
        employee.setId(1);
        employee.setUsername("testuser");

        when(currentUser.getUsername()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(employee));

        ExpenseClaim savedClaim = ExpenseClaim.builder()
                .id(1)
                .employee(employee)
                .description(request.getDescription())
                .amount(request.getAmount())
                .date(request.getDate())
                .category(request.getCategory())
                .status(ClaimStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        when(claimRepository.save(any(ExpenseClaim.class))).thenReturn(savedClaim);

        ClaimResponse response = claimService.submitClaim(request, currentUser);

        assertNotNull(response);
        assertEquals(1, response.getId());
        assertEquals("testuser", response.getEmployeeUserName());
        assertEquals("Hotel accommodation", response.getDescription());
        assertEquals(new BigDecimal("150.50"), response.getAmount());
        assertEquals(Category.ACCOMODATION, response.getCategory());
        assertEquals(ClaimStatus.PENDING, response.getStatus());

        verify(userRepository).findByUsername("testuser");
        verify(claimRepository).save(any(ExpenseClaim.class));
    }

    @Test
    void submitClaim_WithNonExistentUser_ThrowsException() {
        ClaimRequest request = ClaimRequest.builder()
                .description("Test")
                .amount(new BigDecimal("100.00"))
                .date(LocalDate.now())
                .category(Category.TRAVEL)
                .build();

        when(currentUser.getUsername()).thenReturn("nonexistent");
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> claimService.submitClaim(request, currentUser));
        verify(claimRepository, never()).save(any());
    }
}