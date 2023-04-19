package com.udacity.jdnd.course3.critter.pet;

import com.udacity.jdnd.course3.critter.date.DateVerification;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents the form that pet GET request takes. Does not map
 * to the database directly.
 */

@Data
public class PetGetDTO {

    @Autowired
    DateVerification dateVerification;

    private long id;
    private boolean archived;
    private String birthDate;
    private String breedOrSpecies;
    private LocalDateTime lastUpdateTime;
    private String name;
    private long ownerId;
    private String type;

    public void setRawBirthDate (LocalDate localDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                dateVerification.localDateFormat());
        birthDate = localDate.format(formatter);
    }
}
