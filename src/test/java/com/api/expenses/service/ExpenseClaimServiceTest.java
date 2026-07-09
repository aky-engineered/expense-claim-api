package com.api.expenses.service;

import com.api.expenses.exception.NotFoundException;
import com.api.expenses.model.dto.ClaimRequest;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

        assertThrows(NotFoundException.class, () -> claimService.submitClaim(request, currentUser));
        verify(claimRepository, never()).save(any());
    }

    @Test
    void getMyClaims_WithExistingClaims_ReturnsResponses() {
        User employee = new User();
        employee.setId(1);
        employee.setUsername("jane.doe");

        when(currentUser.getUsername()).thenReturn("jane.doe");
        when(userRepository.findByUsername("jane.doe")).thenReturn(Optional.of(employee));

        ExpenseClaim claim = ExpenseClaim.builder()
                .id(10)
                .employee(employee)
                .description("Lunch with client")
                .amount(new BigDecimal("25.00"))
                .date(LocalDate.of(2025, 6, 1))
                .category(Category.MEALS)
                .status(ClaimStatus.PENDING)
                .createdAt(LocalDateTime.of(2025, Month.JUNE, 1, 12, 0))
                .build();

        when(claimRepository.findAllByEmployee(employee)).thenReturn(List.of(claim));

        List<ClaimResponse> responses = claimService.getMyClaims(currentUser);

        assertNotNull(responses);
        assertEquals(1, responses.size());

        ClaimResponse resp = responses.get(0);
        assertEquals(10, resp.getId());
        assertEquals("jane.doe", resp.getEmployeeUserName());
        assertEquals("Lunch with client", resp.getDescription());
        assertEquals(new BigDecimal("25.00"), resp.getAmount());
        assertEquals(Category.MEALS, resp.getCategory());
        assertEquals(ClaimStatus.PENDING, resp.getStatus());
    }

    @Test
    void getMyClaims_WithNoClaims_ReturnsEmptyList() {
        User employee = new User();
        employee.setId(2);
        employee.setUsername("no.claims");

        when(currentUser.getUsername()).thenReturn("no.claims");
        when(userRepository.findByUsername("no.claims")).thenReturn(Optional.of(employee));

        when(claimRepository.findAllByEmployee(employee)).thenReturn(List.of());

        List<ClaimResponse> responses = claimService.getMyClaims(currentUser);

        assertNotNull(responses);
        assertEquals(0, responses.size());
    }

    @Test
    void getMyClaims_WithNonExistentUser_ThrowsException() {
        when(currentUser.getUsername()).thenReturn("unknown");
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> claimService.getMyClaims(currentUser));
    }

    @Test
    void getClaimById_WithExistingClaimAndOwner_ReturnsResponse() {
        User employee = new User();
        employee.setId(1);
        employee.setUsername("owner");

        ExpenseClaim claim = ExpenseClaim.builder()
                .id(1)
                .employee(employee)
                .description("Taxi fare")
                .amount(new BigDecimal("20.00"))
                .date(LocalDate.of(2025, 5, 5))
                .category(Category.TRAVEL)
                .status(ClaimStatus.PENDING)
                .createdAt(LocalDateTime.of(2025, Month.MAY, 5, 10, 0))
                .build();

        when(claimRepository.findById(1)).thenReturn(Optional.of(claim));
        when(currentUser.getUsername()).thenReturn("owner");
        when(userRepository.findByUsername("owner")).thenReturn(Optional.of(employee));

        ClaimResponse response = claimService.getClaimById(1, currentUser);

        assertNotNull(response);
        assertEquals(1, response.getId());
        assertEquals("owner", response.getEmployeeUserName());
        assertEquals(new BigDecimal("20.00"), response.getAmount());
        assertEquals(Category.TRAVEL, response.getCategory());
        assertEquals(ClaimStatus.PENDING, response.getStatus());
    }

    @Test
    void getClaimById_NotFound_ThrowsNotFoundException() {
        when(claimRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> claimService.getClaimById(999, currentUser));
    }

    @Test
    void getClaimById_NotOwner_ThrowsAccessDeniedException() {
        User owner = new User();
        owner.setId(1);
        owner.setUsername("owner");

        ExpenseClaim claim = ExpenseClaim.builder()
                .id(1)
                .employee(owner)
                .description("Dinner")
                .amount(new BigDecimal("30.00"))
                .date(LocalDate.of(2025, 4, 4))
                .category(Category.MEALS)
                .status(ClaimStatus.PENDING)
                .createdAt(LocalDateTime.of(2025, Month.APRIL, 4, 19, 0))
                .build();

        when(claimRepository.findById(1)).thenReturn(Optional.of(claim));

        User other = new User();
        other.setId(2);
        other.setUsername("other");

        when(currentUser.getUsername()).thenReturn("other");
        when(userRepository.findByUsername("other")).thenReturn(Optional.of(other));

        assertThrows(AccessDeniedException.class, () -> claimService.getClaimById(1, currentUser));
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

        List<ClaimResponse> responses = claimService.getAllPendingClaims();

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

        List<ClaimResponse> responses = claimService.getAllPendingClaims();

        assertNotNull(responses);
        assertEquals(0, responses.size());
    }

}