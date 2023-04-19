package com.udacity.jdnd.course3.critter.schedule;

import com.udacity.jdnd.course3.critter.activity.Activity;
import com.udacity.jdnd.course3.critter.exception.CustomApiInvalidParameterException;
import com.udacity.jdnd.course3.critter.pet.Pet;
import com.udacity.jdnd.course3.critter.user.Employee;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Nationalized;
import org.springframework.validation.FieldError;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@Data
@Entity
public class Schedule {

    @Transient
    private final int maxActivitiesPerAppointment = 4;

    @Transient
    private final int maxEmployeesPerAppointment = 3;

    @Transient
    private final int maxPetsPerAppointment = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany(targetEntity = Activity.class)
    private List<Activity> activities;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToMany(targetEntity = Employee.class)
    private List<Employee> employees;

    private LocalTime endTime;

    private LocalDateTime lastUpdateTime;

    @Nationalized
    @Column(length = 10000)
    private String notes;

    @ManyToMany(targetEntity = Pet.class)
    private List<Pet> pets;

    private BigDecimal serviceCost;

    private LocalTime startTime;

    private ScheduleStatus status;

    public void addActivity(Activity activity) {
        if (activities == null) {
            activities = new ArrayList<>();
        }
        activities.add(activity);
    }

    public void addEmployee(Employee employee) {
        if (employees == null) {
            employees = new ArrayList<>();
        }
        employees.add(employee);
    }

    public void addPet(Pet pet) {
        if (pets == null) {
            pets = new ArrayList<>();
        }
        pets.add(pet);
    }

    public Schedule validate() {
        Pattern phoneNumberPattern = Pattern.compile("^[0-9- ]+$");
        List<FieldError> fieldErrors = new ArrayList<>(Collections.emptyList());
        if (id != null && id < 0) {
            FieldError fieldError = new FieldError(
                    "schedule",
                    "id",
                    id,
                    false,
                    null,
                    null,
                    "Invalid entity ID.");
            fieldErrors.add(fieldError);
        }
        if (activities == null) {
            FieldError fieldError = new FieldError(
                    "schedule",
                    "activities",
                    "No activities scheduled.");
            fieldErrors.add(fieldError);
        } else if (activities.size() > maxActivitiesPerAppointment) {
            FieldError fieldError = new FieldError(
                    "schedule",
                    "activities",
                    activities.size(),
                    false,
                    null,
                    null,
                    "Too many activities scheduled; maximum of " +
                            maxActivitiesPerAppointment +
                            " allowed.");
            fieldErrors.add(fieldError);
        }
        if (employees == null) {
            FieldError fieldError = new FieldError(
                    "schedule",
                    "employees",
                    "No employees scheduled.");
            fieldErrors.add(fieldError);
        } else if (employees.size() > maxEmployeesPerAppointment) {
            FieldError fieldError = new FieldError(
                    "schedule",
                    "employees",
                    employees.size(),
                    false,
                    null,
                    null,
                    "Too many employees scheduled; maximum of " +
                            maxEmployeesPerAppointment +
                            " allowed.");
            fieldErrors.add(fieldError);
        }
        if (notes != null) {
            if (notes.trim().length() > 10000) {
                FieldError fieldError = new FieldError(
                    "schedule",
                    "notes",
                    "Notes cannot exceed 10000 characters.");
                fieldErrors.add(fieldError);
            } else {
                notes = notes.trim();
            }
        }
        if (pets == null) {
            FieldError fieldError = new FieldError(
                    "schedule",
                    "pets",
                    "No pets scheduled.");
            fieldErrors.add(fieldError);
        } else if (pets.size() > maxPetsPerAppointment) {
            FieldError fieldError = new FieldError(
                    "schedule",
                    "pets",
                    pets.size(),
                    false,
                    null,
                    null,
                    "Too many pets scheduled; maximum of " +
                            maxPetsPerAppointment +
                            " allowed.");
            fieldErrors.add(fieldError);
        }
        if (status == null) {
            FieldError fieldError = new FieldError(
                    "schedule",
                    "status",
                    "No status specified.");
            fieldErrors.add(fieldError);
        }

        if (! fieldErrors.isEmpty()) {
            throw new CustomApiInvalidParameterException(fieldErrors);
        }

        this.lastUpdateTime = LocalDateTime.now();
        return this;
    }
}

