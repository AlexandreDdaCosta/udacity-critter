package com.udacity.jdnd.course3.critter.schedule;

import com.udacity.jdnd.course3.critter.activity.Activity;
import com.udacity.jdnd.course3.critter.exception.CustomApiInvalidParameterException;
import lombok.Data;
import org.springframework.validation.FieldError;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the form that schedule availability query takes. Does not map
 * to the database directly.
 */

@Data
public class ScheduleAvailabilityQuery {

    private List<String> activities;
    private LocalDate date;

    public ScheduleAvailabilityQuery validate() {
        List<FieldError> fieldErrors = new ArrayList<>(Collections.emptyList());

        if (activities == null) {
            FieldError fieldError = new FieldError(
                    "scheduleAvailabilityQuery",
                    "activities",
                    null,
                    false,
                    null,
                    null,
                    "List of activities required.");
            fieldErrors.add(fieldError);
        }
        if (date == null) {
            FieldError fieldError = new FieldError(
                    "scheduleAvailabilityQuery",
                        "date",
                        null,
                        false,
                        null,
                        null,
                        "Date required.");
                fieldErrors.add(fieldError);
        } else {
            if (LocalDate.now().isAfter(date)) {
                FieldError fieldError = new FieldError(
                        "scheduleAvailabilityQuery",
                        "date",
                        String.valueOf(date),
                        false,
                        null,
                        null,
                        "Past dates may not be searched.");
                fieldErrors.add(fieldError);
            }
        }

        if (! fieldErrors.isEmpty()) {
            throw new CustomApiInvalidParameterException(fieldErrors);
        }

        return this;
    }
}
