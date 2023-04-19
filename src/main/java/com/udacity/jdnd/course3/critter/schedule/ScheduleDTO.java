package com.udacity.jdnd.course3.critter.schedule;

import com.udacity.jdnd.course3.critter.date.DateVerification;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Represents the form that schedule request and response data takes. Does not map
 * to the database directly.
 */

@Data
public class ScheduleDTO {

    @Autowired
    DateVerification dateVerification;

    private long id;
    private List<String> activities;
    private String date;
    private List<Long> employeeIds;
    private String notes;
    private List<Long> petIds;
    private String startTime;
    private String status;
    private boolean preview;

    public void setRawDate (LocalDate localDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                dateVerification.localDateFormat());
        date = localDate.format(formatter);
    }

    public void setRawStartTime (LocalTime localTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                dateVerification.localTimeFormat());
        startTime = localTime.format(formatter);
    }
}
