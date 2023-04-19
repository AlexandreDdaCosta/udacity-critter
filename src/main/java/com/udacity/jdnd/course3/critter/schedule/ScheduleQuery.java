package com.udacity.jdnd.course3.critter.schedule;

import com.udacity.jdnd.course3.critter.exception.CustomApiInvalidParameterException;
import com.udacity.jdnd.course3.critter.pet.Pet;
import com.udacity.jdnd.course3.critter.user.Customer;
import com.udacity.jdnd.course3.critter.user.Employee;
import lombok.Data;
import org.springframework.validation.FieldError;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represents the form that schedule query takes. Does not map
 * to the database directly.
 */

@Data
public class ScheduleQuery {

    private Customer customer;
    private LocalDate date;
    private Employee employee;
    private Pet pet;
    private ScheduleStatus status;

    // Sorting and pagination

    private String dateTimeOrder;
    private Integer limit;
    private Integer offset;

    public ScheduleQuery validate() {
        List<FieldError> fieldErrors = new ArrayList<>(Collections.emptyList());

        if (dateTimeOrder != null) {
            List<String> validOrdering = Arrays.asList("ASC","DESC");
            if (! validOrdering.contains(dateTimeOrder)) {
                FieldError fieldError = new FieldError(
                        "schedule",
                        "dateTimeOrder",
                        String.valueOf(dateTimeOrder),
                        false,
                        null,
                        null,
                        "If specified, date/time order must be \"ASC\" or \"DESC\".");
                fieldErrors.add(fieldError);
            }
        }
        if (limit != null) {
            if (limit < 1 || limit > 100) {
                FieldError fieldError = new FieldError(
                        "schedule",
                        "limit",
                        String.valueOf(limit),
                        false,
                        null,
                        null,
                        "If specified, limit must be between 1 and 100.");
                fieldErrors.add(fieldError);
            }
        } else {
            limit = 100;
        }
        if (offset != null) {
            if (offset < 0) {
                FieldError fieldError = new FieldError(
                        "schedule",
                        "offset",
                        String.valueOf(limit),
                        false,
                        null,
                        null,
                        "Offset cannot be a negative number.");
                fieldErrors.add(fieldError);
            } else if (offset != 0 && offset%limit != 0) {
                FieldError fieldError = new FieldError(
                        "schedule",
                        "offset",
                        "Offset: " +
                                String.valueOf(offset) +
                                ", Limit: " +
                                String.valueOf(limit),
                        false,
                        null,
                        null,
                        "Offset must be zero or a multiple of limit.");
                fieldErrors.add(fieldError);
            }
        } else {
            offset = 0;
        }

        if (! fieldErrors.isEmpty()) {
            throw new CustomApiInvalidParameterException(fieldErrors);
        }

        return this;
    }
}
