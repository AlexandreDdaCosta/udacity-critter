package com.udacity.jdnd.course3.critter.note;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Generic note request
 */

@Data
public class NoteDTO {

    private Long id;
    private String description;
    private String note;
    private LocalDateTime lastUpdateTime;
}
