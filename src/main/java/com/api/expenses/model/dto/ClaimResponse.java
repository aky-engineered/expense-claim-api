package com.api.expenses.model.dto;

import com.api.expenses.model.entity.Category;
import com.api.expenses.model.entity.ClaimStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimResponse {

    private Integer id;
    private String employeeUserName;
    private String description;
    private BigDecimal amount;
    private LocalDate date;
    private Category category;
    private ClaimStatus status;
    private LocalDateTime createdAt;
}
