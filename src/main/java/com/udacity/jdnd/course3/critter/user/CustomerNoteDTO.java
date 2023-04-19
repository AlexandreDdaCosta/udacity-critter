package com.udacity.jdnd.course3.critter.user;

import com.udacity.jdnd.course3.critter.note.NoteDTO;
import lombok.Data;

/**
 * Customer note request
 */

@Data
public class CustomerNoteDTO extends NoteDTO {

    private Long customerId;
}
