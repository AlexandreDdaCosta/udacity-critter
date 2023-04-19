package com.udacity.jdnd.course3.critter.schedule;

import com.udacity.jdnd.course3.critter.date.DateVerification;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents the form that schedule GET request takes. Does not map
 * to the database directly.
 */

@Data
public class ScheduleGetDTO extends ScheduleDTO {

    @Autowired
    DateVerification dateVerification;

    private String endTime;
    private LocalDateTime lastUpdateTime;
    private BigDecimal serviceCost;

    public void setRawEndTime (LocalTime localTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                dateVerification.localTimeFormat());
        endTime = localTime.format(formatter);
    }
}
