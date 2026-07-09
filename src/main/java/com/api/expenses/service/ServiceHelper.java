package com.api.expenses.service;

import com.api.expenses.exception.NotFoundException;
import com.api.expenses.model.dto.ClaimResponse;
import com.api.expenses.model.entity.ExpenseClaim;
import com.api.expenses.model.entity.User;
import com.api.expenses.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ServiceHelper {

    private final UserRepository userRepository;


    public User resolveUser(final UserDetails userDetails) {
        String username = userDetails.getUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("username", username));
    }

    public ClaimResponse toResponse(final ExpenseClaim claim) {
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
