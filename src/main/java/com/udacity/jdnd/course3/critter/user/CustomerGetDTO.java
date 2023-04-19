package com.udacity.jdnd.course3.critter.user;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents the form that customer GET request takes. Does not map
 * to the database directly.
 */

@Data
public class CustomerGetDTO {

    private long id;
    private boolean archived;
    private LocalDateTime lastUpdateTime;
    private String name;
    private List<Long> petIds;
    private String phoneNumber;
}
