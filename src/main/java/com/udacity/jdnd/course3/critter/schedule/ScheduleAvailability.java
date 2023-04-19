package com.udacity.jdnd.course3.critter.schedule;

import com.udacity.jdnd.course3.critter.user.Employee;
import lombok.Data;

import java.util.List;

/**
 * Represents the form that schedule availability result takes. Does not map
 * to the database directly.
 */

@Data
public class ScheduleAvailability {

    private Long employeeId;
    private String employeeName;
    private List<ScheduleAvailabilityRange> localTimeRange;
}
