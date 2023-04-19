package com.udacity.jdnd.course3.critter.user;

import com.udacity.jdnd.course3.critter.note.NoteDTO;
import lombok.Data;

/**
 * Employee note request.
 */

@Data
public class EmployeeNoteDTO extends NoteDTO {

    private Long employeeId;
}
