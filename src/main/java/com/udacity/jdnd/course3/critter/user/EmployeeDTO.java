package com.udacity.jdnd.course3.critter.user;

import lombok.Data;

/**
 * Represents the form that employee request and response data takes. Does not map
 * to the database directly.
 */

@Data
public class EmployeeDTO extends EmployeeGetDTO {

    private Long noteId;
    private String noteDescription;
    private String noteText;
}
