package com.api.expenses.service;

import com.api.expenses.exception.NotFoundException;
import com.api.expenses.model.dto.ClaimRequest;
import com.api.expenses.model.dto.ClaimResponse;
import com.api.expenses.model.entity.ClaimStatus;
import com.api.expenses.model.entity.ExpenseClaim;
import com.api.expenses.model.entity.User;
import com.api.expenses.repository.ExpenseClaimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClaimService {

    private final ExpenseClaimRepository claimRepository;
    private final ServiceHelper helper;

    @PreAuthorize("hasRole('EMPLOYEE')")
    public ClaimResponse submitClaim(final ClaimRequest request, final UserDetails currentUser) {
        User employee = helper.resolveUser(currentUser);

        ExpenseClaim claim = ExpenseClaim.builder()
                .employee(employee)
                .description(request.getDescription())
                .amount(request.getAmount())
                .date(request.getDate())
                .category(request.getCategory())
                .status(ClaimStatus.PENDING) //Defaulted value. Not supplied in request
                .build();

        ExpenseClaim saved = claimRepository.save(claim);

        return helper.toResponse(saved);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    public List<ClaimResponse> getMyClaims(final UserDetails currentUser) {
        User employee = helper.resolveUser(currentUser);
        return claimRepository.findAllByEmployee(employee)
                .stream()
                .map(helper::toResponse)
                .toList();
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ClaimResponse getClaimById(final Integer claimId, final UserDetails currentUser) {
        ExpenseClaim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new NotFoundException("claim", claimId));

        User requester = helper.resolveUser(currentUser);

        // BOLA check — employees can only see their own claims
        boolean isOwner = claim.getEmployee().getId().equals(requester.getId());

        if (!isOwner) {
            throw new AccessDeniedException("You do not have access to this claim");
        }

        return helper.toResponse(claim);
    }
}
