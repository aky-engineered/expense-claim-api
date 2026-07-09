package com.api.expenses.service;

import com.api.expenses.exception.NotFoundException;
import com.api.expenses.model.dto.ClaimRequest;
import com.api.expenses.model.dto.ClaimResponse;
import com.api.expenses.model.entity.ClaimStatus;
import com.api.expenses.model.entity.ExpenseClaim;
import com.api.expenses.model.entity.User;
import com.api.expenses.repository.ExpenseClaimRepository;
import com.api.expenses.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseClaimService {

    private final ExpenseClaimRepository claimRepository;
    private final UserRepository userRepository;

    @PreAuthorize("hasRole('EMPLOYEE')")
    public ClaimResponse submitClaim(final ClaimRequest request, final UserDetails currentUser) {
        User employee = resolveUser(currentUser);

        ExpenseClaim claim = ExpenseClaim.builder()
                .employee(employee)
                .description(request.getDescription())
                .amount(request.getAmount())
                .date(request.getDate())
                .category(request.getCategory())
                .status(ClaimStatus.PENDING) //Defaulted value. Not supplied in request
                .build();

        ExpenseClaim saved = claimRepository.save(claim);

        return toResponse(saved);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    public List<ClaimResponse> getMyClaims(final UserDetails currentUser) {
        User employee = resolveUser(currentUser);
        return claimRepository.findAllByEmployee(employee)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ClaimResponse getClaimById(final Integer claimId, final UserDetails currentUser) {
        ExpenseClaim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new NotFoundException("claim", claimId));

        User requester = resolveUser(currentUser);

        // BOLA check — employees can only see their own claims
        boolean isOwner = claim.getEmployee().getId().equals(requester.getId());

        if (!isOwner) {
            throw new AccessDeniedException("You do not have access to this claim");
        }

        return toResponse(claim);
    }

    @PreAuthorize("hasRole('APPROVER')")
    public List<ClaimResponse> getAllPendingClaims() {
        return claimRepository.findAllByStatus(ClaimStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .toList();
    }
    
    private User resolveUser(final UserDetails userDetails) {
        String username = userDetails.getUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("username", username));
    }

    private ClaimResponse toResponse(ExpenseClaim claim) {
        return new ClaimResponse(
                claim.getId(),
                claim.getEmployee().getUsername(),
                claim.getDescription(),
                claim.getAmount(),
                claim.getDate(),
                claim.getCategory(),
                claim.getStatus(),
                claim.getCreatedAt()
        );
    }
}
