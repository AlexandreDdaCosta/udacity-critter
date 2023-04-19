package com.udacity.jdnd.course3.critter.pet;

import lombok.Data;

/**
 * Represents the form that pet request and response data takes. Does not map
 * to the database directly.
 */

@Data
public class PetDTO extends PetGetDTO {

    private Long noteId;
    private String noteDescription;
    private String noteText;
}
