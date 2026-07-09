package com.api.expenses.web.controller;

import com.api.expenses.model.dto.ClaimRequest;
import com.api.expenses.model.dto.ClaimResponse;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @GetMapping
    @Operation(summary = "Get all my claims",
            security = @SecurityRequirement(name = "Bearer Auth"))
    public ResponseEntity<List<ClaimResponse>> getMyClaims(
            @AuthenticationPrincipal UserDetails currentUser) {

        return ResponseEntity.ok(claimService.getMyClaims(currentUser));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a claim by ID",
            security = @SecurityRequirement(name = "Bearer Auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Claim found"),
            @ApiResponse(responseCode = "403", description = "Not your claim"),
            @ApiResponse(responseCode = "404", description = "Claim not found")
    })
    public ResponseEntity<ClaimResponse> getById(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails currentUser) {

        return ResponseEntity.ok(claimService.getClaimById(id, currentUser));
    }
}
