package com.api.expenses.controller;

import com.api.expenses.model.dto.ClaimResponse;
import com.api.expenses.model.dto.RejectionRequest;
import com.api.expenses.service.ApprovalService;
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
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
@Tag(name = "Approvals")
public class ApprovalController {

    private final ApprovalService approvalService;

    @GetMapping("/pending")
    @Operation(summary = "Get all pending claims",
            security = @SecurityRequirement(name = "Bearer Auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of pending claims"),
            @ApiResponse(responseCode = "403", description = "Not an approver")
    })
    public ResponseEntity<List<ClaimResponse>> getPending() {
        return ResponseEntity.ok(approvalService.getAllPendingClaims());
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve a claim",
            security = @SecurityRequirement(name = "Bearer Auth"))
    public ResponseEntity<ClaimResponse> approve(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails currentUser) {

        return ResponseEntity.ok(approvalService.approveClaim(id, currentUser));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject a claim",
            security = @SecurityRequirement(name = "Bearer Auth"))
    public ResponseEntity<ClaimResponse> reject(
            @PathVariable Integer id,
            @Valid @RequestBody RejectionRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        return ResponseEntity.ok(approvalService.rejectClaim(id, request, currentUser));
    }
}
