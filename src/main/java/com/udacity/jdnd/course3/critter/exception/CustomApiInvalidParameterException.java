package com.udacity.jdnd.course3.critter.exception;

import org.springframework.validation.FieldError;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomApiInvalidParameterException extends InvalidParameterException {

    private List<FieldError> fieldErrors = new ArrayList<>(Collections.emptyList());

    public CustomApiInvalidParameterException(List<FieldError> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }

    public CustomApiInvalidParameterException(FieldError fieldError) {
        fieldErrors.add(fieldError);
    }

    public List<FieldError> getFieldErrors() {
        return fieldErrors;
    }
}
