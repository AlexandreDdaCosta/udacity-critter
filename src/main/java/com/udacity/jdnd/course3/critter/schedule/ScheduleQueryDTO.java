package com.udacity.jdnd.course3.critter.schedule;

import lombok.Data;

/**
 * Represents the form that schedule query request takes. Does not map
 * to the database directly.
 */

@Data
public class ScheduleQueryDTO {

    private Long customerId;
    private String date;
    private Long employeeId;
    private Long petId;
    private String status;

    // Sorting and pagination

    private String dateTimeOrder;
    private Integer limit;
    private Integer offset;
}
