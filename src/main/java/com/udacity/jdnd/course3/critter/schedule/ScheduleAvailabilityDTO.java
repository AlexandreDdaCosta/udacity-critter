package com.udacity.jdnd.course3.critter.schedule;

import lombok.Data;

import java.util.List;

/**
 * Represents the form that schedule availability request data takes. Does not map
 * to the database directly.
 */

@Data
public class ScheduleAvailabilityDTO {

    private List<String> activities;
    private String date;
}
