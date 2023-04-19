package com.udacity.jdnd.course3.critter.pet;

import com.udacity.jdnd.course3.critter.note.NoteDTO;
import lombok.Data;

/**
 * Pet note request.
 */

@Data
public class PetNoteDTO extends NoteDTO {

    private Long petId;
}
