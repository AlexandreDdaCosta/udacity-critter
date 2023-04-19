package com.udacity.jdnd.course3.critter.user;

import com.udacity.jdnd.course3.critter.exception.CustomApiInvalidParameterException;
import com.udacity.jdnd.course3.critter.note.CustomerNote;
import com.udacity.jdnd.course3.critter.pet.Pet;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Nationalized;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@Getter
@Setter
@Entity
@Table(uniqueConstraints={
        @UniqueConstraint(columnNames = {"name", "phoneNumber"})
})
public class Customer {

    @Transient
    private final int maximumNameSize = 200;

    @Transient
    private final int maximumNumberOfNotes = 250;

    @Transient
    private final int maximumPhoneNumberLength = 50;

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean archived;

    @Nationalized
    @Column(length = maximumNameSize, nullable = false)
    private String name;

    @OneToMany(targetEntity = CustomerNote.class)
    private List<CustomerNote> notes = new ArrayList<CustomerNote>();

    @OneToMany(targetEntity = Pet.class)
    private List<Pet> pets;

    @Column(length = maximumPhoneNumberLength, nullable = false)
    private String phoneNumber;

    private LocalDateTime lastUpdateTime;

    @Transient
    private Long noteId;

    @Transient
    @Nationalized
    private String noteDescription;

    @Transient
    @Nationalized
    private String noteText;

    public Customer addCustomerNote(CustomerNote customerNote) {
        if (notes == null) {
            notes = new ArrayList<>();
        }
        notes.add(customerNote);
        return this;
    }

    public Customer addPet(Pet pet) {
        if (pets == null) {
            pets = new ArrayList<>();
        }
        pets.add(pet);
        return this;
    }

    public Customer deletePet(Pet pet) {
        if (pets != null) {
            pets.remove(pet);
        }
        return this;
    }

    public Customer validate() {
        Pattern phoneNumberPattern = Pattern.compile("^[0-9- ]+$");
        List<FieldError> fieldErrors = new ArrayList<>(Collections.emptyList());
        if (id != null && id < 0) {
            FieldError fieldError = new FieldError(
                    "customer",
                    "id",
                    String.valueOf(id),
                    false,
                    null,
                    null,
                    "Invalid entity ID.");
            fieldErrors.add(fieldError);
        }
        if (name == null || name.trim().length() == 0) {
            FieldError fieldError = new FieldError(
                    "customer",
                    "name",
                    "Name is empty.");
            fieldErrors.add(fieldError);
        } else if (name.trim().length() > maximumNameSize) {
            FieldError fieldError = new FieldError(
                    "customer",
                    "name",
                    "Name is too long; maximum " +
                            maximumNameSize +
                            " characters.");
            fieldErrors.add(fieldError);
        } else {
            name = name.trim();
        }
        if (noteText != null) {
            if (notes.size() + 1 > maximumNumberOfNotes) {
                FieldError fieldError = new FieldError(
                        "customer",
                        "notes",
                        "Number of notes exceeds limit of " + maximumNumberOfNotes + ".");
                fieldErrors.add(fieldError);
            }
        } else if (notes.size() > maximumNumberOfNotes) {
            FieldError fieldError = new FieldError(
                    "customer",
                    "notes",
                    "Number of notes exceeds limit of " + maximumNumberOfNotes + ".");
            fieldErrors.add(fieldError);
        }
        if (phoneNumber == null || phoneNumber.trim().length() == 0) {
            FieldError fieldError = new FieldError(
                    "customer",
                    "phoneNumber",
                    "Phone number is empty.");
            fieldErrors.add(fieldError);
        } else if (phoneNumber.trim().length() > maximumPhoneNumberLength) {
            FieldError fieldError = new FieldError(
                    "customer",
                    "phoneNumber",
                    "Phone number is too long; maximum " +
                            maximumPhoneNumberLength +
                            " characters.");
            fieldErrors.add(fieldError);
        } else {
            phoneNumber = phoneNumber.trim();
            if (! phoneNumberPattern.matcher(phoneNumber).matches()) {
                FieldError fieldError = new FieldError(
                        "customer",
                        "phoneNumber",
                        phoneNumber,
                        false,
                        null,
                        null,
                        "Phone number is invalid; only digits, hyphens, and spaces allowed.");
                fieldErrors.add(fieldError);
            }
        }
        if (! fieldErrors.isEmpty()) {
                throw new CustomApiInvalidParameterException(fieldErrors);
        }
        this.lastUpdateTime = LocalDateTime.now();
        return this;
    }
}
