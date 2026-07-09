package com.api.expenses.service;

import com.api.expenses.exception.ConflictException;
import com.api.expenses.exception.NotFoundException;
import com.api.expenses.model.dto.ClaimResponse;
import com.api.expenses.model.dto.RejectionRequest;
import com.api.expenses.model.entity.ClaimStatus;
import com.api.expenses.model.entity.ExpenseClaim;
import com.api.expenses.model.entity.User;
import com.api.expenses.repository.ExpenseClaimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final ServiceHelper helper;
    private final ExpenseClaimRepository claimRepository;
    private final AuditService auditService;

    @PreAuthorize("hasRole('APPROVER')")
    public List<ClaimResponse> getAllPendingClaims() {
        return claimRepository.findAllByStatus(ClaimStatus.PENDING)
                .stream()
                .map(helper::toResponse)
                .toList();
    }

    @PreAuthorize("hasRole('APPROVER')")
    public ClaimResponse approveClaim(final Integer claimId, final UserDetails currentUser) {
        ExpenseClaim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Failed to find claim", claimId));

        User approver = helper.resolveUser(currentUser);
        validateIsPending(claim);

        claim.setStatus(ClaimStatus.APPROVED);
        ExpenseClaim saved = claimRepository.save(claim);

        auditService.createAudit(saved, "APPROVED", approver,
                "Approved by " + approver.getUsername());

        return helper.toResponse(saved);
    }

    @PreAuthorize("hasRole('APPROVER')")
    public ClaimResponse rejectClaim(final Integer claimId, final RejectionRequest request,
                                     UserDetails currentUser) {
        ExpenseClaim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Failed to find claim", claimId));

        User approver = helper.resolveUser(currentUser);
        validateIsPending(claim);

        claim.setStatus(ClaimStatus.REJECTED);
        claim.setRejectionReason(request.getReason());
        ExpenseClaim saved = claimRepository.save(claim);

        auditService.createAudit(saved, "REJECTED", approver,
                "Rejected by " + approver.getUsername());

        return helper.toResponse(saved);
    }

    private void validateIsPending(ExpenseClaim claim) {
        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new ConflictException(
                    "Claim cannot be actioned — current status: " + claim.getStatus()
            );
        }
    }
}
