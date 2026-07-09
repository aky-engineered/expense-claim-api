package com.api.expenses.service;

import com.api.expenses.exception.NotFoundException;
import com.api.expenses.model.dto.AuditResponse;
import com.api.expenses.model.entity.AuditLog;
import com.api.expenses.model.entity.ExpenseClaim;
import com.api.expenses.model.entity.User;
import com.api.expenses.repository.AuditLogRepository;
import com.api.expenses.repository.ExpenseClaimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ExpenseClaimRepository claimRepository;
    private final ServiceHelper helper;

    public void createAudit(final ExpenseClaim claim, final String action, final User performedBy, final String details) {
        AuditLog entry = AuditLog.builder()
                .claim(claim)
                .action(action)
                .performedBy(performedBy)
                .performedAt(LocalDateTime.now())
                .details(details)
                .build();

        auditLogRepository.save(entry);
    }

    @PreAuthorize("hasRole('APPROVER')")
    public List<AuditResponse> getAuditTrail(final Integer claimId, final UserDetails currentUser) {
        ExpenseClaim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim", claimId));

        helper.resolveUser(currentUser);

        return auditLogRepository.findAllByClaimOrderByPerformedAtAsc(claim)
                .stream()
                .map(log -> new AuditResponse(
                        log.getId(),
                        log.getAction(),
                        log.getPerformedBy().getUsername(),
                        log.getPerformedAt(),
                        log.getDetails()
                ))
                .toList();
    }
}
