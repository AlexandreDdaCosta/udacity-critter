package com.udacity.jdnd.course3.critter.user;

import lombok.Data;

import java.util.List;

/**
 * Represents a request to find available employees by skills and date. Does not map
 * to the database directly.
 */

@Data
public class EmployeeRequestDTO {

    private List<String> skills;
    private String date;
}
