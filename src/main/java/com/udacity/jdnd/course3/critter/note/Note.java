package com.udacity.jdnd.course3.critter.note;

import com.udacity.jdnd.course3.critter.exception.CustomApiInvalidParameterException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@MappedSuperclass
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Nationalized
    @Column(length = 200, nullable = false)
    private String description;

    @Nationalized
    @Column(length = 2000, nullable = false)
    private String note;

    private LocalDateTime lastUpdateTime;

    public Note(Long id, String description, String note, LocalDateTime lastUpdateTime) {
        this.setId(id);
        this.setDescription(description);
        this.setNote(note);
        this.setLastUpdateTime(lastUpdateTime);
    }

    public Note validate() {
        List<FieldError> fieldErrors = new ArrayList<>(Collections.emptyList());
        if (id != null && id < 0) {
            FieldError fieldError = new FieldError(
                    "note",
                    "id",
                    String.valueOf(id),
                    false,
                    null,
                    null,
                    "Invalid entity ID.");
            fieldErrors.add(fieldError);
        }
        if (description == null || description.trim().length() == 0) {
            FieldError fieldError = new FieldError(
                    "note",
                    "description",
                    "Description is empty.");
            fieldErrors.add(fieldError);
        } else if (description.trim().length() > 200) {
            FieldError fieldError = new FieldError(
                    "note",
                    "description",
                    "Description is too long; maximum 200 characters.");
            fieldErrors.add(fieldError);
        } else {
            description = description.trim();
        }
        if (note == null || note.trim().length() == 0) {
            FieldError fieldError = new FieldError(
                    "note",
                    "note",
                    "Note is empty.");
            fieldErrors.add(fieldError);
        } else if (note.trim().length() > 2000) {
            FieldError fieldError = new FieldError(
                    "note",
                    "note",
                    "Note is too long; maximum 2000 characters.");
            fieldErrors.add(fieldError);
        } else {
            note = note.trim();
        }

        if (! fieldErrors.isEmpty()) {
            throw new CustomApiInvalidParameterException(fieldErrors);
        }

        this.lastUpdateTime = LocalDateTime.now();
        return this;
    }
}
