package com.api.expenses.repository;

import com.api.expenses.model.entity.ClaimStatus;
import com.api.expenses.model.entity.ExpenseClaim;
import com.api.expenses.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseClaimRepository extends JpaRepository<ExpenseClaim, Integer> {

    List<ExpenseClaim> findAllByEmployee(User employee);
    List<ExpenseClaim> findAllByStatus(ClaimStatus status);
}
