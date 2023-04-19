package com.udacity.jdnd.course3.critter.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static org.springframework.http.HttpStatus.*;

@ControllerAdvice
public class CustomRestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(CustomApiInvalidParameterException.class)
    protected ResponseEntity<Object> handleCustomApiInvalidParameterException(CustomApiInvalidParameterException e) {
        ApiError apiError = new ApiError(BAD_REQUEST);
        apiError.setMessage("Parameter validation error.");
        apiError.addValidationErrors(e.getFieldErrors());
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    @ExceptionHandler(CustomApiNoSuchElementException.class)
    protected ResponseEntity<Object> handleCustomApiNoSuchException(CustomApiNoSuchElementException e) {
        ApiError apiError = new ApiError(NOT_FOUND);
        apiError.setMessage("Entity retrieval error.");
        apiError.addValidationError(e.getFieldError());
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }
}
