package com.udacity.jdnd.course3.critter.user;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents the form that employee GET request takes. Does not map
 * to the database directly.
 */

@Data
public class EmployeeGetDTO {
    private long id;
    private boolean archived;
    private List<String> daysAvailable;
    private LocalDateTime lastUpdateTime;
    private String name;
    private List<String> skills;
}
