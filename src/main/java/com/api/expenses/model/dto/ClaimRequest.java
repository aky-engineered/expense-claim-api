package com.api.expenses.model.dto;

import com.api.expenses.model.entity.Category;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimRequest {

    @NotBlank
    @Size(max = 255)
    private String description;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotNull
    @PastOrPresent
    @JsonFormat(pattern="yyyy-MM-dd")
    LocalDate date;

    @NotNull
    Category category;
}
