package com.udacity.jdnd.course3.critter.user;

import com.google.common.collect.Lists;
import com.udacity.jdnd.course3.critter.activity.Activity;
import com.udacity.jdnd.course3.critter.exception.CustomApiInvalidParameterException;
import com.udacity.jdnd.course3.critter.exception.CustomApiNoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.FieldError;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmployeeVerification {

    @Autowired
    private EmployeeService employeeService;

    public Employee verifyEmployee(Long employeeId) {
        Employee employee = employeeService.findById(employeeId);
        return employee;
    }

    public void verifyEmployeeAvailability(LocalDate date,
                                           List<Employee> employees,
                                           String objectName)
    {
        List<FieldError> fieldErrors = new ArrayList<FieldError>();

        if (date != null) {
            DayOfWeek scheduledDay = DayOfWeek.of(date.getDayOfWeek().getValue());
            if (employees != null ) {
                for (Employee scheduledEmployee : employees) {
                    if (scheduledEmployee.getDaysAvailable() == null) {
                        FieldError fieldError = new FieldError(
                                objectName,
                                "employees",
                                "Employee ID " +
                                        String.valueOf(scheduledEmployee.getId()),
                                false,
                                null,
                                null,
                                "Requested employee has no set availability.");
                        fieldErrors.add(fieldError);
                    } else if (! scheduledEmployee.getDaysAvailable().contains(scheduledDay)) {
                        FieldError fieldError = new FieldError(
                                "schedule",
                                "employees",
                                "Employee ID " +
                                        String.valueOf(scheduledEmployee.getId()) +
                                        " : " +
                                        String.valueOf(scheduledDay),
                                false,
                                null,
                                null,
                                "Requested employee does not work on requested day.");
                        fieldErrors.add(fieldError);
                    }
                }
            }
            if (! fieldErrors.isEmpty()) {
                throw new CustomApiInvalidParameterException(fieldErrors);
            }
        }
    }

    public List<Employee> verifyEmployees(List<Long> employees) {
        List<FieldError> fieldErrors = new ArrayList<FieldError>();
        if (employees != null) {
            List<Employee> scheduledEmployees = Lists.newArrayList();
            for (Long scheduleEmployee: employees) {
                try {
                    Employee employee = employeeService.findById(scheduleEmployee);
                    scheduledEmployees.add(employee);
                } catch (CustomApiNoSuchElementException e) {
                    fieldErrors.add(e.getFieldError());
                }
            }
            if (! fieldErrors.isEmpty()) {
                throw new CustomApiInvalidParameterException(fieldErrors);
            }
            return scheduledEmployees;
        }
        return null;
    }

    public void verifyEmployeeSkills(List<Employee> employees,
                                     List<Activity> activities,
                                     String objectName)
    {
        List<FieldError> fieldErrors = new ArrayList<FieldError>();

        if (employees != null && activities != null) {
            List<String> activityNames = new ArrayList<String>();
            for (Activity activity: activities) {
                activityNames.add(activity.getName());
            }
            for (Employee scheduledEmployee : employees) {
                List<String> employeeSkillNames = new ArrayList<String>();
                if (scheduledEmployee.getSkills() != null) {
                    employeeSkillNames.addAll(scheduledEmployee.getSkillNames());
                }
                for (String activityName: activityNames) {
                    if (scheduledEmployee.getSkills() == null
                            || ! employeeSkillNames.contains(activityName)) {
                        FieldError fieldError = new FieldError(
                                objectName,
                                "employees",
                                "Employee ID " +
                                        String.valueOf(scheduledEmployee.getId()) +
                                        " : " + activityName,
                                false,
                                null,
                                null,
                                "Requested employee does not have required skill.");
                        fieldErrors.add(fieldError);
                    }
                }
            }
            if (! fieldErrors.isEmpty()) {
                throw new CustomApiInvalidParameterException(fieldErrors);
            }
        }
    }
}
