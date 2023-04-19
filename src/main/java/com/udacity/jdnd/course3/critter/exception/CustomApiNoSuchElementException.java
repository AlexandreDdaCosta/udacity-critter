package com.udacity.jdnd.course3.critter.exception;

import org.springframework.validation.FieldError;

import java.util.NoSuchElementException;

public class CustomApiNoSuchElementException extends NoSuchElementException {

    private FieldError fieldError;

    public CustomApiNoSuchElementException(FieldError fieldError) {
        this.fieldError = fieldError;
    }

    public FieldError getFieldError() {
        return fieldError;
    }
}
