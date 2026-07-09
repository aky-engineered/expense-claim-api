package com.api.expenses.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

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

    public String getField() {
        return field;
    }

    public Object getRejectedValue() {
        return rejectedValue;
    }
}
