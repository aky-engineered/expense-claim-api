package com.api.expenses.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final String field;
    private final Object rejectedValue;

    public NotFoundException(String field, Object rejectedValue) {
        super(String.format("%s with value '%s' not found", field, rejectedValue));
        this.field = field;
        this.rejectedValue = rejectedValue;
    }

}
