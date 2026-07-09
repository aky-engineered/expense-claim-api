package com.api.expenses.service;

import com.api.expenses.model.entity.AuditLog;
import com.api.expenses.model.entity.ExpenseClaim;
import com.api.expenses.model.entity.User;
import com.api.expenses.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

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
}
