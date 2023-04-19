package com.udacity.jdnd.course3.critter.user;

import com.udacity.jdnd.course3.critter.activity.Activity;
import com.udacity.jdnd.course3.critter.date.DateVerification;
import com.udacity.jdnd.course3.critter.exception.CustomApiInvalidParameterException;
import com.udacity.jdnd.course3.critter.exception.CustomApiNoSuchElementException;
import com.udacity.jdnd.course3.critter.note.EmployeeNote;
import com.udacity.jdnd.course3.critter.note.EmployeeNoteRepository;
import com.udacity.jdnd.course3.critter.note.Note;
import com.udacity.jdnd.course3.critter.schedule.Schedule;
import com.udacity.jdnd.course3.critter.schedule.ScheduleStatus;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.stereotype.Service;
import org.springframework.validation.FieldError;

import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    @Autowired
    DateVerification dateVerification;

    @Autowired
    EmployeeNoteRepository employeeNoteRepository;

    @Autowired
    EmployeeRepository employeeRepository;

    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    public List<Employee> findEmployeesForService(List<String> skills, DayOfWeek dayOfWeek) {
        return employeeRepository.findAll().stream()
                .filter(employee -> new HashSet<>(employee.getSkillNames()).containsAll(skills))
                .filter(employee -> employee.getDaysAvailable().contains(dayOfWeek))
                .collect(Collectors.toList());
    }

    public Employee findById(Long employeeId) {
        try {
            return findEntryById(employeeId);
        }  catch (InvalidDataAccessApiUsageException e) {
            FieldError fieldError = new FieldError(
                    "employee",
                    "id",
                    null,
                    false,
                    null,
                    null,
                    "Missing employee ID.");
            throw new CustomApiNoSuchElementException(fieldError);
        } catch (NoSuchElementException e) {
            FieldError fieldError = new FieldError(
                    "employee",
                    "id",
                    String.valueOf(employeeId),
                    false,
                    null,
                    null,
                    "Unknown employee ID.");
            throw new CustomApiNoSuchElementException(fieldError);
        }
    }

    public Employee findEntryById(Long employeeId) {
        return employeeRepository.findById(employeeId).get();
    }

    public EmployeeNote getEmployeeNote(Long employeeId, Long noteId) {
        EmployeeNote employeeNote = employeeNoteRepository.findByIdAndEmployeeId(noteId, employeeId);
        if (employeeNote == null) {
            FieldError fieldError = new FieldError(
                    "note",
                    "employee ID + note ID",
                    String.valueOf(employeeId) + ", " + String.valueOf(noteId),
                    false,
                    null,
                    null,
                    "No such note exists.");
            throw new CustomApiNoSuchElementException(fieldError);
        }
        return employeeNote;
    }

    public List<Note> getEmployeeNotes(Long employeeId) {
        Employee employee = findById(employeeId);
        return employeeNoteRepository.findAllProjectedBy(employeeId);
    }

    public List<Long> getEmployeeNoteIds(Long employeeId) {
        Employee employee = findById(employeeId);
        return employeeNoteRepository.findAllIdsProjectedBy(employeeId);
    }

    @Transactional
    public Employee save(Employee employee) {
        Employee existingEmployee = employeeRepository.findByName(employee.getName());
        if (existingEmployee != null) {
            if (employee.getId() == null ||
                    employee.getId() == 0 ||
                    !employee.getId().equals(existingEmployee.getId())) {
                FieldError fieldError = new FieldError(
                        "employee",
                        "name",
                        employee.getName(),
                        false,
                        null,
                        null,
                        "An employee with that name already exists.");
                throw new CustomApiInvalidParameterException(fieldError);
            }
        }
        if (employee.getId() != null && employee.getId() != 0) {
            Employee existingEmployeeEntry = findById(employee.getId());
            if (existingEmployeeEntry.isArchived() && employee.isArchived()) {
                FieldError fieldError = new FieldError(
                        "employee",
                        "archived",
                        "true",
                        false,
                        null,
                        null,
                        "Employee entry is archived; no changes allowed.");
                throw new CustomApiInvalidParameterException(fieldError);
            }
            employee.setNotes(existingEmployeeEntry.getNotes());
        }

        verifyEmployeeSave(employee, existingEmployee);

        String noteDescription = null;
        String noteText = null;
        EmployeeNote employeeNote = null;
        if (employee.getNoteText() != null) {
            noteDescription = employee.getNoteDescription();
            noteText = employee.getNoteText();
            employeeNote = new EmployeeNote();
            employeeNote.setDescription(noteDescription);
            employeeNote.setNote(noteText);
            employeeNote = employeeNote.validate();
        }
        employee = employeeRepository.save(employee);
        if (employeeNote != null) {
            employeeNote.setEmployee(employee);
            employeeNote = employeeNoteRepository.save(employeeNote);
            employee = employee.addEmployeeNote(employeeNote);
            employee = employeeRepository.save(employee);
        }
        if (noteText != null) {
            employee.setNoteId(employeeNote.getId());
            employee.setNoteDescription(noteDescription);
            employee.setNoteText(noteText);
        }
        return employee;
    }

    @Transactional
    public EmployeeNote saveNote(EmployeeNote employeeNote) {
        boolean addedNote = false;
        if (employeeNote.getId() != null && employeeNote.getId() != 0) {
            getEmployeeNote(
                    employeeNote.getEmployee().getId(),
                    employeeNote.getId());
        } else {
            addedNote = true;
        }
        employeeNote = employeeNoteRepository.save(employeeNote);
        if (addedNote) {
            Employee employee = employeeNote.getEmployee();
            employee = employee.addEmployeeNote(employeeNote);
            employeeRepository.save(employee);
        }
        return employeeNote;
    }

    public List setAvailability(List<String> daysAvailable, long employeeId) {
        List<FieldError> fieldErrors = new ArrayList<FieldError>();
        Employee employee = null;
        try {
            employee = employeeRepository.findById(employeeId).get();
        } catch (NoSuchElementException e) {
            FieldError fieldError = new FieldError(
                    "employee",
                    "id",
                    String.valueOf(employeeId),
                    false,
                    null,
                    null,
                    "Unknown employee ID.");
            fieldErrors.add(fieldError);
        }
        List<DayOfWeek> availableDays = new ArrayList<DayOfWeek>();
        try {
            availableDays = dateVerification.verifyDaysOfWeek(daysAvailable, "employee");
        } catch (CustomApiInvalidParameterException e) {
            fieldErrors.addAll(e.getFieldErrors());
        }
        if (!fieldErrors.isEmpty()) {
            throw new CustomApiInvalidParameterException(fieldErrors);
        }
        employee.setDaysAvailable(availableDays);
        employeeRepository.save(employee);
        return availableDays;
    }

    public void verifyEmployeeSave(Employee newEmployee, Employee oldEmployee) {
        List<FieldError> fieldErrors = new ArrayList<>(Collections.emptyList());

        boolean pendingSchedules = false;
        List<Activity> requiredPendingScheduleSkills = new ArrayList<Activity>();
        if (oldEmployee != null && oldEmployee.getSchedules() != null) {
            for (Schedule schedule: oldEmployee.getSchedules()) {
                if (schedule.getStatus() == ScheduleStatus.PENDING) {
                    pendingSchedules = true;
                    for (Activity activity: schedule.getActivities()) {
                        if (! requiredPendingScheduleSkills.contains(activity)) {
                            requiredPendingScheduleSkills.add(activity);
                        }
                    }
                }
            }
        }

        if (pendingSchedules && newEmployee.isArchived()) {
            FieldError fieldError = new FieldError(
                    "employee",
                    "archived",
                    "Employee cannot be archived due to pending schedule.");
                    fieldErrors.add(fieldError);
        }

        if (pendingSchedules &&
                ! requiredPendingScheduleSkills.isEmpty()) {
            for (Activity activity : requiredPendingScheduleSkills) {
                if (newEmployee.getSkills() == null ||
                        ! newEmployee.getSkills().contains(activity)) {
                    FieldError fieldError = new FieldError(
                            "employee",
                            "skills",
                            activity.getName(),
                            false,
                            null,
                            null,
                            "Employee skill cannot be removed due to " +
                                    "being required for pending schedule.");
                    fieldErrors.add(fieldError);
                }
            }
        }

        if (! fieldErrors.isEmpty()) {
            throw new CustomApiInvalidParameterException(fieldErrors);
        }
    }
}

