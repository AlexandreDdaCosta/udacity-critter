package com.udacity.jdnd.course3.critter.user;

import lombok.Data;

/**
 * Represents the form that customer request and response data takes. Does not map
 * to the database directly.
 */

@Data
public class CustomerDTO extends CustomerGetDTO {

    private Long noteId;
    private String noteDescription;
    private String noteText;
}
