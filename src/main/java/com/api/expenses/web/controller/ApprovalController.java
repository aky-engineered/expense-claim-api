package com.api.expenses.web.controller;

import com.api.expenses.model.dto.ClaimResponse;
import com.api.expenses.service.ExpenseClaimService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
@Tag(name = "Approvals")
public class ApprovalController {

    private final ExpenseClaimService claimService;

    @GetMapping("/pending")
    @Operation(summary = "Get all pending claims",
            security = @SecurityRequirement(name = "Bearer Auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of pending claims"),
            @ApiResponse(responseCode = "403", description = "Not an approver")
    })
    public ResponseEntity<List<ClaimResponse>> getPending() {
        return ResponseEntity.ok(claimService.getAllPendingClaims());
    }
}
