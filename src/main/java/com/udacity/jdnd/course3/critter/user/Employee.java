package com.udacity.jdnd.course3.critter.user;

import com.google.common.collect.Lists;
import com.udacity.jdnd.course3.critter.activity.Activity;
import com.udacity.jdnd.course3.critter.exception.CustomApiInvalidParameterException;
import com.udacity.jdnd.course3.critter.note.EmployeeNote;
import com.udacity.jdnd.course3.critter.schedule.Schedule;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Nationalized;
import org.springframework.validation.FieldError;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@Entity
public class Employee {

    @Transient
    private final int maximumNameSize = 200;

    @Transient
    private final int maximumNumberOfNotes = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean archived;

    @ElementCollection
    private List<DayOfWeek> daysAvailable;

    @Nationalized
    @Column(length = maximumNameSize, unique = true, nullable = false)
    private String name;

    @OneToMany(targetEntity = EmployeeNote.class)
    private List<EmployeeNote> notes = new ArrayList<EmployeeNote>();

    @ManyToMany(targetEntity = Activity.class)
    private List<Activity> skills;

    @ManyToMany(targetEntity = Schedule.class)
    private List<Schedule> schedules;

    private LocalDateTime lastUpdateTime;

    @Transient
    private Long noteId;

    @Transient
    @Nationalized
    private String noteDescription;

    @Transient
    @Nationalized
    private String noteText;

    public Employee addEmployeeNote(EmployeeNote employeeNote) {
        if (notes == null) {
            notes = new ArrayList<>();
        }
        notes.add(employeeNote);
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

    public List<String> getSkillNames() {
        List<String> skillNames = Lists.newArrayList();
        if (skills != null) {
            for (Activity skill : skills) {
                skillNames.add(skill.getName());
            }
        }
        return skillNames;
    }

    public Employee validate() {
        List<FieldError> fieldErrors = new ArrayList<>(Collections.emptyList());
        if (id != null && id < 0) {
            FieldError fieldError = new FieldError(
                    "employee",
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
                    "employee",
                    "name",
                    "Name is empty.");
            fieldErrors.add(fieldError);
        } else if (name.trim().length() > maximumNameSize) {
            FieldError fieldError = new FieldError(
                    "employee",
                    "name",
                    "Name is too long; maximum " + maximumNameSize + " characters.");
            fieldErrors.add(fieldError);
        } else {
            name = name.trim();
        }
        if (noteText != null) {
            if (notes.size() + 1 > maximumNumberOfNotes) {
                FieldError fieldError = new FieldError(
                        "employee",
                        "notes",
                        "Number of notes exceeds limit of " + maximumNumberOfNotes + ".");
                fieldErrors.add(fieldError);
            }
        } else if (notes.size() > maximumNumberOfNotes) {
            FieldError fieldError = new FieldError(
                    "employee",
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
