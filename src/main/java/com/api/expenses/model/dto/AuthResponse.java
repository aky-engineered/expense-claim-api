package com.api.expenses.model.dto;

public record AuthResponse(
        String token,
        String username,
        String role
) {}
