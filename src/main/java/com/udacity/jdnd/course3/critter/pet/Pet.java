package com.udacity.jdnd.course3.critter.pet;

import com.udacity.jdnd.course3.critter.exception.CustomApiInvalidParameterException;
import com.udacity.jdnd.course3.critter.note.PetNote;
import com.udacity.jdnd.course3.critter.schedule.Schedule;
import com.udacity.jdnd.course3.critter.user.Customer;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Nationalized;
import org.springframework.validation.FieldError;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(uniqueConstraints={
       @UniqueConstraint(columnNames = {"type", "name", "customer_id"})
})
public class Pet {

    @Transient
    private final int maximumNameLength = 100;

    @Transient
    private final int maximumNumberOfNotes = 250;

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean archived;

    private LocalDate birthDate;

    @Nationalized
    @Column(length = maximumNameLength)
    private String breedOrSpecies;

    @ManyToOne(targetEntity = Customer.class, optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Nationalized
    @Column(length = maximumNameLength, nullable = false)
    private String name;

    @OneToMany(targetEntity = PetNote.class)
    private List<PetNote> notes = new ArrayList<PetNote>();

    @ManyToMany(targetEntity = Schedule.class)
    private List<Schedule> schedules;

    @ManyToOne
    @JoinColumn(name = "type", referencedColumnName = "id", nullable = false)
    private PetType type;

    private LocalDateTime lastUpdateTime;

    @Transient
    private Long noteId;

    @Transient
    private String noteDescription;

    @Transient
    private String noteText;

    public Pet addPetNote(PetNote petNote) {
        if (notes == null) {
            notes = new ArrayList<>();
        }
        notes.add(petNote);
        return this;
    }

    public void addSchedule(Schedule schedule) {
        if (schedules == null) {
            schedules = new ArrayList<>();
        }
        Schedule scheduleToRemove = null;
        for (Schedule existingSchedule: schedules) {
            if (schedule.getId() == existingSchedule.getId()) {
                scheduleToRemove = existingSchedule;
                break;
            }
        }
        if (scheduleToRemove != null) {
            schedules.remove(scheduleToRemove);
        }
        schedules.add(schedule);
    }

    public Pet validate() {
        List<FieldError> fieldErrors = new ArrayList<>(Collections.emptyList());
        if (id != null && id < 0) {
            FieldError fieldError = new FieldError(
                    "pet",
                    "id",
                    String.valueOf(id),
                    false,
                    null,
                    null,
                    "Invalid entity ID.");
            fieldErrors.add(fieldError);
        }
        if (birthDate != null && birthDate.isAfter(LocalDate.now())) {
            FieldError fieldError = new FieldError(
                    "pet",
                    "birthDate",
                    birthDate.toString(),
                    false,
                    null,
                    null,
                    "Birth date is in the future.");
            fieldErrors.add(fieldError);
        }
        if (breedOrSpecies != null) {
            if (breedOrSpecies.trim().length() > maximumNameLength) {
            FieldError fieldError = new FieldError(
                    "pet",
                    "breedOrSpecies",
                    "Breed/species name is too long; maximum " +
                            maximumNameLength +
                            " characters.");
            fieldErrors.add(fieldError);
            } else {
                breedOrSpecies = breedOrSpecies.trim();
            }
        }
        if (name == null || name.trim().length() == 0) {
            FieldError fieldError = new FieldError(
                    "pet",
                    "name",
                    "Name is empty.");
            fieldErrors.add(fieldError);
        } else if (name.trim().length() > maximumNameLength) {
            FieldError fieldError = new FieldError(
                    "pet",
                    "name",
                    "Name is too long; maximum " + maximumNameLength + " characters.");
            fieldErrors.add(fieldError);
        } else {
            name = name.trim();
        }
        if (noteText != null) {
            if (notes.size() + 1 > maximumNumberOfNotes) {
                FieldError fieldError = new FieldError(
                        "pet",
                        "notes",
                        "Number of notes exceeds limit of " + maximumNumberOfNotes + ".");
                fieldErrors.add(fieldError);
            }
        } else if (notes.size() > maximumNumberOfNotes) {
            FieldError fieldError = new FieldError(
                    "pet",
                    "notes",
                    "Number of notes exceeds limit of " + maximumNumberOfNotes + ".");
            fieldErrors.add(fieldError);
        }
        if (! fieldErrors.isEmpty()) {
            throw new CustomApiInvalidParameterException(fieldErrors);
        }
        this.lastUpdateTime = LocalDateTime.now();
        return this;
    }
}
