package com.api.expenses;

import com.api.expenses.model.dto.ClaimResponse;
import com.api.expenses.model.entity.Category;
import com.api.expenses.model.entity.ClaimStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

@Component
public class Helper {

    public ClaimResponse buildDummyClaimResponse() {
        return ClaimResponse.builder()
                .id(1)
                .employeeUserName("test")
                .date(LocalDate.of(2025, 1, 1))
                .amount(BigDecimal.TEN)
                .status(ClaimStatus.PENDING)
                .category(Category.MEALS)
                .createdAt(LocalDateTime.of(2025, Month.FEBRUARY, 3, 6, 30, 40, 50))
                .build();
    }
}
