package com.api.expenses.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Employee Expense Claim API",
                description = "Backend API for submitting and approving expense claims",
                version = "1.0.0"
        )
)
public class OpenApiConfig {
}
