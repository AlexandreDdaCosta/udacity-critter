package com.udacity.jdnd.course3.critter.schedule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.udacity.jdnd.course3.critter.activity.Activity;
import com.udacity.jdnd.course3.critter.activity.ActivityVerification;
import com.udacity.jdnd.course3.critter.date.DateVerification;
import com.udacity.jdnd.course3.critter.exception.CustomApiInvalidParameterException;
import com.udacity.jdnd.course3.critter.exception.CustomApiNoSuchElementException;
import com.udacity.jdnd.course3.critter.pet.Pet;
import com.udacity.jdnd.course3.critter.pet.PetVerification;
import com.udacity.jdnd.course3.critter.user.CustomerVerification;
import com.udacity.jdnd.course3.critter.user.Employee;
import com.udacity.jdnd.course3.critter.user.EmployeeVerification;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.FieldError;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles web requests related to Schedules.
 */

@RestController
@RequestMapping("/schedule")
public class ScheduleController {

    @Autowired
    private ActivityVerification activityVerification;

    @Autowired
    private CustomerVerification customerVerification;

    @Autowired
    private DateVerification dateVerification;

    @Autowired
    private EmployeeVerification employeeVerification;

    @Autowired
    private PetVerification petVerification;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private ScheduleVerification scheduleVerification;

    private String timeFormat = dateVerification.localTimeFormat();

    @PostMapping
    public ScheduleGetDTO createUpdateSchedule(@RequestBody ScheduleDTO scheduleDTO) {
        boolean preview = scheduleDTO.isPreview();
        Schedule schedule = scheduleService.save(scheduleDTOToEntity(scheduleDTO), preview);
        ScheduleGetDTO scheduleGetDTO = scheduleEntityToGetDTO(schedule);
        scheduleGetDTO.setPreview(preview);
        return scheduleGetDTO;
    }

    @GetMapping("/{scheduleId}")
    public ScheduleGetDTO getSchedule(@PathVariable long scheduleId) {
        Schedule schedule = scheduleService.findById(scheduleId);
        return(scheduleEntityToGetDTO(schedule));
    }

    @GetMapping
    public List<ScheduleGetDTO> getAllSchedules(@RequestBody ScheduleQueryDTO scheduleQueryDTO) {
        ScheduleQuery scheduleQuery = scheduleQueryDTOToQuery(scheduleQueryDTO);
        return scheduleService.findAll(scheduleQuery)
                .stream()
                .map(ScheduleController::scheduleEntityToGetDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/availability")
    public String getScheduleAvailability(
            @RequestBody ScheduleAvailabilityDTO scheduleAvailabilityDTO)
            throws JsonProcessingException {
        ScheduleAvailabilityQuery scheduleAvailabilityQuery =
                scheduleAvailabilityDTOToQuery(scheduleAvailabilityDTO);
        List<ScheduleAvailability> scheduleAvailability =
                scheduleService.getScheduleAvailability(scheduleAvailabilityQuery);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        ObjectWriter objectWriter = objectMapper.writer().withDefaultPrettyPrinter();
        return objectWriter.writeValueAsString(scheduleAvailability);
    }

    @GetMapping("/pet/{petId}")
    public List<ScheduleGetDTO> getScheduleForPet(@PathVariable long petId) {
        return scheduleService.getPetSchedule(petId)
                    .stream()
                    .map(ScheduleController::scheduleEntityToGetDTO)
                    .collect(Collectors.toList());
    }

    @GetMapping("/employee/{employeeId}")
    public List<ScheduleGetDTO> getScheduleForEmployee(@PathVariable long employeeId) {
        return scheduleService.getEmployeeSchedule(employeeId)
                .stream()
                .map(ScheduleController::scheduleEntityToGetDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/customer/{customerId}")
    public List<ScheduleGetDTO> getScheduleForCustomer(@PathVariable long customerId) {
        return scheduleService.getCustomerSchedule(customerId)
                .stream()
                .map(ScheduleController::scheduleEntityToGetDTO)
                .collect(Collectors.toList());
    }

    // DTO <---> Entity conversions

    private ScheduleAvailabilityQuery scheduleAvailabilityDTOToQuery(
            ScheduleAvailabilityDTO scheduleAvailabilityDTO) {
        List<FieldError> fieldErrors = new ArrayList<FieldError>();
        ScheduleAvailabilityQuery scheduleAvailabilityQuery = new ScheduleAvailabilityQuery();
        BeanUtils.copyProperties(scheduleAvailabilityDTO, scheduleAvailabilityQuery);

        try {
            activityVerification.verifyActivities(
                    scheduleAvailabilityDTO.getActivities(),
                    "schedule");
        } catch (CustomApiInvalidParameterException e) {
            fieldErrors.addAll(e.getFieldErrors());
        }

        try {
            scheduleAvailabilityQuery.setDate(dateVerification.verifyDateFormat(
                    scheduleAvailabilityDTO.getDate(),
                    "schedule"));
        } catch (CustomApiInvalidParameterException e) {
            fieldErrors.addAll(e.getFieldErrors());
        }

        try {
            scheduleAvailabilityQuery = scheduleAvailabilityQuery.validate();
        } catch (CustomApiInvalidParameterException e) {
            fieldErrors.addAll(e.getFieldErrors());
        }

        if (! fieldErrors.isEmpty()) {
            throw new CustomApiInvalidParameterException(fieldErrors);
        }

        return scheduleAvailabilityQuery;
    }

    private Schedule scheduleDTOToEntity(ScheduleDTO scheduleDTO) {
        List<FieldError> fieldErrors = new ArrayList<FieldError>();
        Schedule schedule = new Schedule();
        BeanUtils.copyProperties(scheduleDTO, schedule);

        try {
            schedule.setActivities(activityVerification.verifyActivities(
                    scheduleDTO.getActivities(),
                    "schedule"));
        } catch (CustomApiInvalidParameterException e) {
            fieldErrors.addAll(e.getFieldErrors());
        }

        try {
            schedule.setDate(dateVerification.verifyDateFormat(
                    scheduleDTO.getDate(),
                    "schedule"));
        } catch (CustomApiInvalidParameterException e) {
            fieldErrors.addAll(e.getFieldErrors());
        }

        try {
            schedule.setEmployees(employeeVerification.verifyEmployees(
                    scheduleDTO.getEmployeeIds()));
        } catch (CustomApiInvalidParameterException e) {
            fieldErrors.addAll(e.getFieldErrors());
        }
        try {
            employeeVerification.verifyEmployeeSkills(
                    schedule.getEmployees(),
                    schedule.getActivities(),
                    "schedule");
        } catch (CustomApiInvalidParameterException e) {
            fieldErrors.addAll(e.getFieldErrors());
        }
        try {
            employeeVerification.verifyEmployeeAvailability(
                    schedule.getDate(),
                    schedule.getEmployees(),
                    "schedule");
        } catch (CustomApiInvalidParameterException e) {
            fieldErrors.addAll(e.getFieldErrors());
        }

        try {
            schedule.setPets(petVerification.verifyPets(
                    scheduleDTO.getPetIds()));
        } catch (CustomApiInvalidParameterException e) {
            fieldErrors.addAll(e.getFieldErrors());
        }
        try {
            petVerification.verifyPetDetails(
                    schedule.getPets(),
                    schedule.getActivities(),
                    "schedule");
        } catch (CustomApiInvalidParameterException e) {
            fieldErrors.addAll(e.getFieldErrors());
        }

        try {
            schedule.setStatus(scheduleVerification.verifyStatus(
                    scheduleDTO.getStatus(),
                    "schedule"));
        } catch (CustomApiInvalidParameterException e) {
            fieldErrors.addAll(e.getFieldErrors());
        }

        try {
            schedule.setStartTime(dateVerification.verifyTime(
                    scheduleDTO.getStartTime(),
                    "schedule",
                    "startTime"));
        } catch (CustomApiInvalidParameterException e) {
            fieldErrors.addAll(e.getFieldErrors());
        }

        try {
            schedule = schedule.validate();
        } catch (CustomApiInvalidParameterException e) {
            fieldErrors.addAll(e.getFieldErrors());
        }

        if (! fieldErrors.isEmpty()) {
            throw new CustomApiInvalidParameterException(fieldErrors);
        }

        return schedule;
    }

    private static ScheduleDTO scheduleEntityToDTO(Schedule schedule) {
        ScheduleDTO scheduleDTO = new ScheduleDTO();
        BeanUtils.copyProperties(schedule, scheduleDTO);
        scheduleDTO.setStatus(schedule.getStatus().toString());
        scheduleDTO.setActivities(
                schedule.getActivities().stream()
                        .map(Activity::getName)
                        .collect(Collectors.toList()));
        scheduleDTO.setEmployeeIds(
                schedule.getEmployees().stream()
                        .map(Employee::getId)
                        .collect(Collectors.toList()));
        scheduleDTO.setPetIds(
                schedule.getPets().stream()
                        .map(Pet::getId)
                        .collect(Collectors.toList()));
        scheduleDTO.setRawDate(schedule.getDate());
        if (schedule.getStartTime() != null) {
            scheduleDTO.setRawStartTime(schedule.getStartTime());
        }
        return scheduleDTO;
    }

    private static ScheduleGetDTO scheduleEntityToGetDTO(Schedule schedule) {
        ScheduleDTO scheduleDTO = scheduleEntityToDTO(schedule);
        ScheduleGetDTO scheduleGetDTO = new ScheduleGetDTO();
        BeanUtils.copyProperties(scheduleDTO, scheduleGetDTO);
        scheduleGetDTO.setServiceCost(schedule.getServiceCost());
        if (schedule.getEndTime() != null) {
            scheduleGetDTO.setRawEndTime(schedule.getEndTime());
        }
        scheduleGetDTO.setLastUpdateTime(schedule.getLastUpdateTime());
        return scheduleGetDTO;
    }

    private ScheduleQuery scheduleQueryDTOToQuery(ScheduleQueryDTO scheduleQueryDTO) {
        List<FieldError> fieldErrors = new ArrayList<FieldError>();
        ScheduleQuery scheduleQuery = new ScheduleQuery();
        BeanUtils.copyProperties(scheduleQueryDTO, scheduleQuery);

        if (scheduleQueryDTO.getCustomerId() != null) {
            try {
                scheduleQuery.setCustomer(customerVerification.verifyCustomer(
                        scheduleQueryDTO.getCustomerId()));
            } catch (CustomApiNoSuchElementException e) {
                fieldErrors.add(e.getFieldError());
            }
        }
        try {
            scheduleQuery.setDate(dateVerification.verifyDateFormat(
                    scheduleQueryDTO.getDate(),
                    "schedule"));
        } catch (CustomApiInvalidParameterException e) {
            fieldErrors.addAll(e.getFieldErrors());
        }
        if (scheduleQueryDTO.getEmployeeId() != null) {
            try {
                scheduleQuery.setEmployee(employeeVerification.verifyEmployee(
                        scheduleQueryDTO.getEmployeeId()));
            } catch (CustomApiNoSuchElementException e) {
                fieldErrors.add(e.getFieldError());
            }
        }
        if (scheduleQueryDTO.getPetId() != null) {
            try {
                scheduleQuery.setPet(petVerification.verifyPet(
                        scheduleQueryDTO.getPetId()));
            } catch (CustomApiNoSuchElementException e) {
                fieldErrors.add(e.getFieldError());
            }
        }
        if (scheduleQueryDTO.getStatus() != null) {
            try {
                ScheduleStatus searchStatus = ScheduleStatus.valueOf(
                        scheduleQueryDTO.getStatus());
                scheduleQuery.setStatus(searchStatus);
            } catch (IllegalArgumentException e) {
                FieldError fieldError = new FieldError(
                        "schedule",
                        "status",
                        String.valueOf(scheduleQueryDTO.getStatus()),
                        false,
                        null,
                        null,
                        "Invalid status.");
                fieldErrors.add(fieldError);
            }
        }

        try {
            scheduleQuery = scheduleQuery.validate();
        } catch (CustomApiInvalidParameterException e) {
            fieldErrors.addAll(e.getFieldErrors());
        }

        if (! fieldErrors.isEmpty()) {
            throw new CustomApiInvalidParameterException(fieldErrors);
        }

        return scheduleQuery;
    }
}
