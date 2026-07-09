package com.api.expenses.web.controller;

import com.api.expenses.model.dto.ClaimRequest;
import com.api.expenses.model.entity.ClaimResponse;
import com.api.expenses.service.ExpenseClaimService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
@Tag(name = "Expense Claims")
public class ClaimController {

    private final ExpenseClaimService claimService;

    @PostMapping
    @Operation(summary = "Submit a new expense claim",
            security = @SecurityRequirement(name = "Bearer Auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Claim submitted"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Not an employee")
    })
    public ResponseEntity<ClaimResponse> submit(
            @Valid @RequestBody ClaimRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        return ResponseEntity.status(201).body(claimService.submitClaim(request, currentUser));
    }
}
