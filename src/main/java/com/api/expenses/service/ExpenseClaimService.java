package com.api.expenses.service;

import com.api.expenses.model.dto.ClaimRequest;
import com.api.expenses.model.dto.ClaimResponse;
import com.api.expenses.model.entity.ClaimStatus;
import com.api.expenses.model.entity.ExpenseClaim;
import com.api.expenses.model.entity.User;
import com.api.expenses.repository.ExpenseClaimRepository;
import com.api.expenses.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseClaimService {

    private final ExpenseClaimRepository claimRepository;
    private final UserRepository userRepository;

    @PreAuthorize("hasRole('EMPLOYEE')")
    public ClaimResponse submitClaim(ClaimRequest request, UserDetails currentUser) {
        User employee = resolveUser(currentUser);

        ExpenseClaim claim = ExpenseClaim.builder()
                .employee(employee)
                .description(request.getDescription())
                .amount(request.getAmount())
                .date(request.getDate())
                .category(request.getCategory())
                .status(ClaimStatus.PENDING) //Defaulted value. Not supplied in request
                .build();

        ExpenseClaim saved = claimRepository.save(claim);

        return toResponse(saved);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    public List<ClaimResponse> getMyClaims(UserDetails currentUser) {
        User employee = resolveUser(currentUser);
        return claimRepository.findAllByEmployee(employee)
                .stream()
                .map(this::toResponse)
                .toList();
    }


    private User resolveUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private ClaimResponse toResponse(ExpenseClaim claim) {
        return new ClaimResponse(
                claim.getId(),
                claim.getEmployee().getUsername(),
                claim.getDescription(),
                claim.getAmount(),
                claim.getDate(),
                claim.getCategory(),
                claim.getStatus(),
                claim.getCreatedAt()
        );
    }
}
