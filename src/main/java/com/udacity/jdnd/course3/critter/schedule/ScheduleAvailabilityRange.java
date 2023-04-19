package com.udacity.jdnd.course3.critter.schedule;

import lombok.Data;

import java.time.LocalTime;

/**
 * Represents the form that schedule availability range result takes. Does not map
 * to the database directly.
 */

@Data
public class ScheduleAvailabilityRange {

    private LocalTime startTime;
    private LocalTime endTime;
}
