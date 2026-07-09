package com.api.expenses.repository;

import com.api.expenses.model.entity.AuditLog;
import com.api.expenses.model.entity.ExpenseClaim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AudiLogRepository extends JpaRepository<AuditLog, Integer> {
    List<AuditLog> findAllByClaimOrderByPerformedAtAsc(ExpenseClaim claim);
}
