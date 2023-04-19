package com.udacity.jdnd.course3.critter.schedule;

import com.udacity.jdnd.course3.critter.activity.Activity;
import com.udacity.jdnd.course3.critter.exception.CustomApiInvalidParameterException;
import com.udacity.jdnd.course3.critter.exception.CustomApiNoSuchElementException;
import com.udacity.jdnd.course3.critter.pet.Pet;
import com.udacity.jdnd.course3.critter.pet.PetActivityType;
import com.udacity.jdnd.course3.critter.pet.PetActivityTypeService;
import com.udacity.jdnd.course3.critter.pet.PetService;
import com.udacity.jdnd.course3.critter.user.Customer;
import com.udacity.jdnd.course3.critter.user.CustomerService;
import com.udacity.jdnd.course3.critter.user.Employee;
import com.udacity.jdnd.course3.critter.user.EmployeeService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.FieldError;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class ScheduleService {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private OfficeScheduleService officeScheduleService;

    @Autowired
    private PetActivityTypeService petActivityTypeService;

    @Autowired
    private PetService petService;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private CustomScheduleRepositoryImpl scheduleSearchRepository;

    private final int schedulingInterval = 5; // minutes
    public final int lunchHourMinutes = 60; // Should be a multiple of schedulingInterval

    public int getLunchHourMinutes() {
        return lunchHourMinutes;
    }

    public List<Schedule> findAll(ScheduleQuery scheduleQuery) {
        return scheduleSearchRepository.customScheduleSearch(scheduleQuery);
    }

    public Schedule findById(Long scheduleId) {
        try {
            return scheduleRepository.findById(scheduleId).get();
        } catch (NoSuchElementException e) {
            FieldError fieldError = new FieldError(
                    "schedule",
                    "id",
                    String.valueOf(scheduleId),
                    false,
                    null,
                    null,
                    "Unknown schedule ID.");
            throw new CustomApiNoSuchElementException(fieldError);
        }
    }

    public List<Schedule> getCustomerSchedule(long customerId) {
        Customer customer = customerService.findById(customerId);
        List<Pet> pets = petService.findAllByCustomerId(customerId);
        List<Schedule> schedules = new ArrayList<>();
        for (Pet pet : pets) {
            schedules.addAll(getPetSchedule(pet.getId()));
        }
        return schedules;
    }

    public List<Schedule> getEmployeeSchedule(long employeeId) {
        Employee employee = employeeService.findById(employeeId);
        return employee.getSchedules();
    }

    public List<Schedule> getPetSchedule(long petId) {
        Pet pet = petService.findById(petId);
        return pet.getSchedules();
    }

    public List<ScheduleAvailability> getScheduleAvailability(
            ScheduleAvailabilityQuery scheduleAvailabilityQuery) {

        List<ScheduleAvailability> scheduleAvailabilityList = new ArrayList<>();

        // Create list of employees matching requested activities and day of week

        DayOfWeek dayOfWeek = scheduleAvailabilityQuery.getDate().getDayOfWeek();
        OfficeSchedule officeSchedule = officeScheduleService.findByDayOfWeek(dayOfWeek.toString());
        if (officeSchedule == null) {
            // Office closed on requested day
            List<FieldError> fieldErrors = new ArrayList<>(Collections.emptyList());
            FieldError fieldError = new FieldError(
                    "schedule",
                    "date",
                    String.valueOf(scheduleAvailabilityQuery.getDate()),
                    false,
                    null,
                    null,
                    "Office closed on requested day.");
            fieldErrors.add(fieldError);
            throw new CustomApiInvalidParameterException(fieldErrors);
        }
        LocalDate appointmentDate = scheduleAvailabilityQuery.getDate();
        int arraySize = (int) (officeSchedule.getOfficeOpens().until(
                officeSchedule.getOfficeCloses(), ChronoUnit.MINUTES) / schedulingInterval);
        List<Employee> employees = employeeService.findEmployeesForService(
                scheduleAvailabilityQuery.getActivities(),
                dayOfWeek);
        if (employees == null || employees.size() == 0) {
            return scheduleAvailabilityList;
        }
        employees.sort((e1, e2) -> e1.getName().compareTo(e2.getName()));

        for (Employee employee : employees) {

            // Retrieve daily pending schedule and generate open time ranges

            ScheduleQuery scheduleQuery = new ScheduleQuery();
            scheduleQuery.setDate(scheduleAvailabilityQuery.getDate());
            scheduleQuery.setEmployee(employee);
            scheduleQuery.setStatus(ScheduleStatus.PENDING);
            scheduleQuery.setDateTimeOrder("ASC");
            scheduleQuery = scheduleQuery.validate();
            List<Schedule> existingEmployeeSchedule = findAll(scheduleQuery);

            // Populate employee schedule

            List<Schedule> employeeSchedule = readEntitySchedule(arraySize,
                    existingEmployeeSchedule,
                    null,
                    officeSchedule);

            // Create a schedule availability object. Each range is a ScheduleAvailabilityRange.

            ScheduleAvailability scheduleAvailability = new ScheduleAvailability();
            scheduleAvailability.setEmployeeId(employee.getId());
            scheduleAvailability.setEmployeeName(employee.getName());
            int checkIndex = 0;
            int rangeStart = 0;
            boolean rangeStarted = false;
            List<ScheduleAvailabilityRange> scheduleAvailabilityRanges = new ArrayList<>();
            while (checkIndex < employeeSchedule.size()) {
                Schedule checkIndexSchedule = employeeSchedule.get(checkIndex);
                if (checkIndexSchedule == null) {
                    // If date is today, remove ranges with start time in the past
                    if (scheduleAvailabilityQuery.getDate().equals(LocalDate.now())) {
                        LocalTime currentIndexTime = officeSchedule
                                .getOfficeOpens()
                                .plusMinutes((long) schedulingInterval * checkIndex);
                        if (currentIndexTime.isBefore(LocalTime.now())) {
                            checkIndex++;
                            continue;
                        }
                    }
                    if (! rangeStarted) {
                        rangeStart = checkIndex;
                        rangeStarted = true;
                    }
                } else if (rangeStarted) {
                    ScheduleAvailabilityRange scheduleAvailabilityRange =
                            new ScheduleAvailabilityRange();
                    LocalTime startTime = officeSchedule
                            .getOfficeOpens()
                            .plusMinutes((long) schedulingInterval * rangeStart);
                    scheduleAvailabilityRange.setStartTime(startTime);
                    LocalTime endTime = officeSchedule
                            .getOfficeOpens()
                            .plusMinutes((long) schedulingInterval * checkIndex);
                    scheduleAvailabilityRange.setEndTime(endTime);
                    scheduleAvailabilityRanges.add(scheduleAvailabilityRange);
                    rangeStarted = false;
                }
                checkIndex++;
            }
            if (rangeStarted) {
                ScheduleAvailabilityRange scheduleAvailabilityRange =
                        new ScheduleAvailabilityRange();
                LocalTime startTime = officeSchedule
                        .getOfficeOpens()
                        .plusMinutes((long) schedulingInterval * rangeStart);
                scheduleAvailabilityRange.setStartTime(startTime);
                LocalTime endTime = officeSchedule
                        .getOfficeOpens()
                        .plusMinutes((long) schedulingInterval * (checkIndex));
                scheduleAvailabilityRange.setEndTime(endTime);
                scheduleAvailabilityRanges.add(scheduleAvailabilityRange);
            }
            scheduleAvailability.setLocalTimeRange(scheduleAvailabilityRanges);

            // Append schedule availability object to scheduleAvailabilityList

            scheduleAvailabilityList.add(scheduleAvailability);
        }

        return scheduleAvailabilityList;
    }

    @Transactional
    public Schedule save(Schedule schedule, boolean preview) {
        Schedule existingScheduleEntry = null;
        if (schedule.getId() != null && schedule.getId() != 0) {
            existingScheduleEntry = findById(schedule.getId());
        }
        String dayOfWeek = schedule.getDate().getDayOfWeek().toString();
        OfficeSchedule officeSchedule = officeScheduleService.findByDayOfWeek(dayOfWeek);
        if (officeSchedule == null) {
            // Office closed on requested day
            List<FieldError> fieldErrors = new ArrayList<>(Collections.emptyList());
            FieldError fieldError = new FieldError(
                    "schedule",
                    "date",
                    String.valueOf(schedule.getDate()),
                    false,
                    null,
                    null,
                    "Office closed on requested day.");
            fieldErrors.add(fieldError);
            throw new CustomApiInvalidParameterException(fieldErrors);
        }
        verifySchedule(schedule, existingScheduleEntry, officeSchedule);
        schedule = buildSchedule(schedule, existingScheduleEntry, officeSchedule);
        if (preview) {
            return schedule;
        }
        schedule = scheduleRepository.save(schedule);
        for (Pet pet : schedule.getPets()) {
            pet.addSchedule(schedule);
            petService.save(pet, pet.getCustomer().getId());
        }
        for (Employee employee : schedule.getEmployees()) {
            employee.addSchedule(schedule);
            employeeService.save(employee);
        }
        return schedule;
    }

    public Schedule buildSchedule(Schedule newSchedule,
                                  Schedule oldSchedule,
                                  OfficeSchedule officeSchedule) {

        // Build known schedules for all employees/pets requested for
        // this schedule.

        // The ArrayLists have these characteristics:
        // 1. Each element is a five-minute activity window
        // 2. The windows are in time order
        // 3. The first element represents the first five minutes of the work day
        // 4. The last element represents the last five minutes of the work day
        // 5. Each element contains the schedule ID of the appointment
        //    currently occupying the activity window

        // Calculate the size of the ArrayLists using the business schedule
        // Pre-populate with zeros

        List<FieldError> fieldErrors = new ArrayList<>(Collections.emptyList());

        LocalDate appointmentDate = newSchedule.getDate();
        String dayOfWeek = newSchedule.getDate().getDayOfWeek().toString();
        int arraySize = (int) (officeSchedule.getOfficeOpens().until(
                officeSchedule.getOfficeCloses(), ChronoUnit.MINUTES) / schedulingInterval);

        // Fill window entries that match all existing schedules

        Map<Employee, List<Schedule>> employeeSchedules = new HashMap<Employee, List<Schedule>>();
        for (Employee employee: newSchedule.getEmployees()) {

            // Get current employee schedule for the appointment date

            ScheduleQuery scheduleQuery = new ScheduleQuery();
            scheduleQuery.setDate(newSchedule.getDate());
            scheduleQuery.setEmployee(employee);
            scheduleQuery.setStatus(ScheduleStatus.PENDING);
            scheduleQuery.setDateTimeOrder("ASC");
            scheduleQuery = scheduleQuery.validate();
            List<Schedule> existingEmployeeSchedule = findAll(scheduleQuery);

            // Populate employee schedule

            List<Schedule> employeeSchedule = readEntitySchedule(arraySize,
                    existingEmployeeSchedule,
                    oldSchedule,
                    officeSchedule);
            employeeSchedules.put(employee, employeeSchedule);
        }

        Map<Pet, List<Schedule>> petSchedules = new HashMap<Pet, List<Schedule>>();
        for (Pet pet: newSchedule.getPets()) {

            // Get current pet schedule for the appointment date

            ScheduleQuery scheduleQuery = new ScheduleQuery();
            scheduleQuery.setDate(newSchedule.getDate());
            scheduleQuery.setPet(pet);
            scheduleQuery.setStatus(ScheduleStatus.PENDING);
            scheduleQuery.setDateTimeOrder("ASC");
            scheduleQuery = scheduleQuery.validate();
            List<Schedule> existingPetSchedule = findAll(scheduleQuery);

            // Populate pet schedule

            List<Schedule> petSchedule = readEntitySchedule(arraySize,
                    existingPetSchedule,
                    oldSchedule,
                    officeSchedule);
            petSchedules.put(pet, petSchedule);
        }

        // Calculate the time window length needed for the appointment based on:
        // Number of pets involved
        // Types of services requested, given pet type
        //
        // Note that certain services can be offered concurrently for multiple pets
        // of the same type.

        // Calculate the cost of the appointment based on:
        // Number of pets involved
        // Number of employees requested
        // Types of services requested, given pet type
        //
        // Adding employees increases the cost proportionally per employee requested
        // and has no effect on time window length

        Integer totalMinutes = 0;
        BigDecimal serviceCost = BigDecimal.valueOf(0);
        for (Activity activity : newSchedule.getActivities()) {
            List<PetActivityType> petActivities = new ArrayList<PetActivityType>();
            for (Pet pet : newSchedule.getPets()) {
                PetActivityType petActivityType = petActivityTypeService.
                        findByPetTypeAndActivity(pet.getType(), activity);
                if (petActivities.contains(petActivityType)) {
                    if (! petActivityType.getActivity().isConcurrent()) {
                        // Don't add time when activities can happen concurrently
                        totalMinutes += petActivityType.getMinutes();
                    }
                    serviceCost = serviceCost.add(petActivityType.getCostForAdditionalPet());
                } else {
                    totalMinutes += petActivityType.getMinutes();
                    serviceCost = serviceCost.add(petActivityType.getCostForFirstPet());
                    petActivities.add(petActivityType);
                }
            }
        }
        BigDecimal employeeAdjustment = new BigDecimal(newSchedule.getEmployees().size());
        newSchedule.setServiceCost(serviceCost.multiply(employeeAdjustment));

        // Search for and select an available window.
        //
        // Use fixed start time if requested; otherwise, use the start time
        // defined for the first located window.
        //
        // Take into account a mandatory one-hour lunch break for each requested
        // employee during the lunch window
        //
        // Throw CustomApiInvalidParameterException if no acceptable window can be found
        // Set end time if window located

        if (newSchedule.getStartTime() != null) {
            int startIndex = (int) officeSchedule.getOfficeOpens().until(
                    newSchedule.getStartTime(),
                    ChronoUnit.MINUTES)
                    / schedulingInterval;
            int endIndex = startIndex + totalMinutes / schedulingInterval - 1;
            LocalTime endTime = newSchedule
                    .getStartTime()
                    .plusMinutes(schedulingInterval * (endIndex - startIndex + 1));
            if (endTime.isAfter(officeSchedule.getOfficeCloses())) {
                FieldError fieldError = new FieldError(
                        "schedule",
                        "startTime",
                        String.valueOf(newSchedule.getStartTime()),
                        false,
                        null,
                        null,
                        "Start time is too late; " +
                                "appointment would go past office close.");
                fieldErrors.add(fieldError);
            } else {
                newSchedule.setEndTime(endTime);
                fieldErrors = compareEntitySchedules(
                        employeeSchedules,
                        petSchedules,
                        startIndex,
                        endIndex,
                        officeSchedule,
                        newSchedule);
            }
        } else {
            int startIndex = 0;
            int endIndex = startIndex + totalMinutes / schedulingInterval - 1;
            LocalTime startTime = null;
            LocalTime endTime = null;
            while (endIndex <= arraySize) {
                startTime = officeSchedule.getOfficeOpens()
                        .plusMinutes(schedulingInterval * startIndex);
                endTime = startTime
                    .plusMinutes(schedulingInterval * (endIndex - startIndex + 1));
                if (endTime.isAfter(officeSchedule.getOfficeCloses())) {
                    FieldError fieldError = new FieldError(
                            "schedule",
                            "endTime",
                            String.valueOf(newSchedule.getEndTime()),
                            false,
                            null,
                            null,
                            "No available appointments.");
                    fieldErrors.add(fieldError);
                    break;
                }
                List<FieldError> compareFieldErrors = compareEntitySchedules(
                        employeeSchedules,
                        petSchedules,
                        startIndex,
                        endIndex,
                        officeSchedule,
                        newSchedule);
                if (compareFieldErrors.isEmpty()) {
                    break;
                }
                startIndex++;
                endIndex++;
            }

            if (fieldErrors.isEmpty()) {
                newSchedule.setStartTime(startTime);
                newSchedule.setEndTime(endTime);
            }
        }

        if (! fieldErrors.isEmpty()) {
            throw new CustomApiInvalidParameterException(fieldErrors);
        }

        return newSchedule;
    }

    public List<FieldError> compareEntitySchedules(
            Map<Employee, List<Schedule>> employeeSchedules,
            Map<Pet, List<Schedule>> petSchedules,
            int startIndex,
            int endIndex,
            OfficeSchedule officeSchedule,
            Schedule newSchedule) {

        // Iterate through all employee and pet schedules to see if they
        // work with the proposed schedule

        List<FieldError> fieldErrors = new ArrayList<>(Collections.emptyList());

        // Sort the employee map for consistent error ordering
        class EmployeeComparator implements Comparator<Employee> {
            public int compare(Employee e1,Employee e2) {
                return (int) (e1.getId() - e2.getId());
            }
        }
        TreeMap<Employee, List<Schedule>> sortedEmployeeSchedules =
                new TreeMap<Employee, List<Schedule>>(new EmployeeComparator());
        sortedEmployeeSchedules.putAll(employeeSchedules);

        for (Map.Entry<Employee, List<Schedule>> employeeSet : sortedEmployeeSchedules.entrySet()) {

            int checkIndex = startIndex;
            boolean failedAppointment = false;
            while (checkIndex <= endIndex) {
                Schedule checkIndexSchedule = employeeSet.getValue().get(checkIndex);
                if (checkIndexSchedule != null) {
                    // Time slot taken; record the conflict
                    FieldError fieldError = new FieldError(
                            "schedule",
                            "employee",
                            "Employee " +
                                    String.valueOf(employeeSet.getKey().getId()) +
                                    ", " +
                                    "Schedule " +
                                    String.valueOf(checkIndexSchedule.getId()),
                            false,
                            null,
                            null,
                            "Employee scheduling conflict.");
                    fieldErrors.add(fieldError);
                    failedAppointment = true;
                    break;
                }
                checkIndex++;
            }
            if (! failedAppointment &&
                    officeSchedule.getLunchHourRangeStart().toString() != "00:00" &&
                    officeSchedule.getLunchHourRangeEnd().toString() != "00:00") {

                // Lunch hour check

                int startLunchIndex = (int) (officeSchedule.getOfficeOpens().until(
                        officeSchedule.getLunchHourRangeStart(), ChronoUnit.MINUTES)
                        / schedulingInterval);
                int endLunchIndex = (int) (officeSchedule.getOfficeOpens().until(
                        officeSchedule.getLunchHourRangeEnd(), ChronoUnit.MINUTES)
                        / schedulingInterval - 1);
                List<Schedule> lunchIntervalSchedule = new ArrayList<Schedule>();
                while (lunchIntervalSchedule.size() < endLunchIndex - startLunchIndex + 1) {
                    lunchIntervalSchedule.add(null);
                }

                // Fill all occupied intervals in the lunch interval schedule
                int checkLunchIndex = startLunchIndex;
                int lunchIntervalScheduleIndex = 0;
                while (checkLunchIndex <= endLunchIndex) {
                    Schedule schedule = employeeSet.getValue().get(checkLunchIndex);
                    if (schedule != null) {
                        // Add existing employee schedule to the lunchtime schedule
                        lunchIntervalSchedule.set(lunchIntervalScheduleIndex, schedule);
                    } else if (checkLunchIndex >= startIndex && checkLunchIndex <= endIndex) {
                        // Add proposed new schedule to the lunchtime schedule
                        lunchIntervalSchedule.set(lunchIntervalScheduleIndex, newSchedule);
                    }
                    checkLunchIndex++;
                    lunchIntervalScheduleIndex++;
                }

                // Find the longest unbroken interval for lunch in
                // the lunch interval schedule
                int unbrokenInterval = 0;
                int longestUnbrokenInterval = 0;
                lunchIntervalScheduleIndex = 0;
                while (lunchIntervalScheduleIndex < lunchIntervalSchedule.size()) {
                    if (lunchIntervalSchedule.get(lunchIntervalScheduleIndex) == null) {
                        unbrokenInterval++;
                    } else {
                        if (unbrokenInterval > longestUnbrokenInterval) {
                            longestUnbrokenInterval = unbrokenInterval;
                        }
                        unbrokenInterval = 0;
                    }
                    lunchIntervalScheduleIndex++;
                }
                if (unbrokenInterval > longestUnbrokenInterval) {
                    longestUnbrokenInterval = unbrokenInterval;
                }
                int requiredLunchInterval = lunchHourMinutes / schedulingInterval;
                if (longestUnbrokenInterval < requiredLunchInterval) {
                    FieldError fieldError = new FieldError(
                            "schedule",
                            "employee",
                            "Employee " +
                                    String.valueOf(employeeSet.getKey().getId()),
                            false,
                            null,
                            null,
                            "Employee lunch scheduling conflict.");
                    fieldErrors.add(fieldError);
                }
            }
        }

        // Sort the pet map for consistent error ordering
        class PetComparator implements Comparator<Pet> {
            public int compare(Pet p1, Pet p2) {
                return (int) (p1.getId() - p2.getId());
            }
        }
        TreeMap<Pet, List<Schedule>> sortedPetSchedules =
                new TreeMap<Pet, List<Schedule>>(new PetComparator());
        sortedPetSchedules.putAll(petSchedules);

        for (Map.Entry<Pet, List<Schedule>> petSet : sortedPetSchedules.entrySet()) {
            int checkIndex = startIndex;
            while (checkIndex <= endIndex) {
                Schedule checkIndexSchedule = petSet.getValue().get(checkIndex);
                if (checkIndexSchedule != null) {
                    // Time slot taken; record the conflict
                    FieldError fieldError = new FieldError(
                            "schedule",
                            "pet",
                            "Pet " +
                                    String.valueOf(petSet.getKey().getId()) +
                                    ", " +
                                    "Schedule " +
                                    String.valueOf(checkIndexSchedule.getId()),
                            false,
                            null,
                            null,
                            "Pet scheduling conflict.");
                    fieldErrors.add(fieldError);
                    break;
                }
                checkIndex++;
            }
        }
        return fieldErrors;
    }

    private List<Schedule> readEntitySchedule(int arraySize,
                                              List<Schedule> existingEntitySchedule,
                                              Schedule oldSchedule,
                                              OfficeSchedule officeSchedule)
    {
        List<Schedule> entitySchedule = new ArrayList<Schedule>();
        while (entitySchedule.size() < arraySize) {
            entitySchedule.add(null);
        }

        // Populate entity list

        int entityListIndex = 0;
        LocalTime entityListIndexTime = officeSchedule.getOfficeOpens();
        for (Schedule existingAppointment : existingEntitySchedule) {
            if (oldSchedule != null && oldSchedule.getId().equals(existingAppointment.getId())) {
                // Skip a schedule whose ID matches the old schedule
                // This condition implies we are updating an existing schedule,
                // so we remove that schedule from consideration
                continue;
            }
            while (entityListIndexTime.isBefore(existingAppointment.getStartTime())) {
                entityListIndex++;
                entityListIndexTime = officeSchedule.getOfficeOpens().plusMinutes(
                        (long) schedulingInterval * entityListIndex);
                if (entityListIndex > arraySize) {
                    break;
                }
            }
            while (entityListIndexTime.isBefore(existingAppointment.getEndTime())) {
                entitySchedule.set(entityListIndex, existingAppointment);
                entityListIndex++;
                entityListIndexTime = officeSchedule.getOfficeOpens().plusMinutes(
                        (long) schedulingInterval * entityListIndex);
                if (entityListIndex > arraySize) {
                    break;
                }
            }
        }


        return entitySchedule;
    }

    public void verifySchedule(Schedule newSchedule,
                               Schedule oldSchedule,
                               OfficeSchedule officeSchedule) {
        List<FieldError> fieldErrors = new ArrayList<>(Collections.emptyList());

        if (oldSchedule != null) {
            if (oldSchedule.getStatus() == ScheduleStatus.CANCELLED) {
                FieldError fieldError = new FieldError(
                        "schedule",
                        "status",
                        "Appointment has been cancelled; no changes allowed.");
                fieldErrors.add(fieldError);
            } else if (oldSchedule.getStatus() == ScheduleStatus.COMPLETE) {
                FieldError fieldError = new FieldError(
                        "schedule",
                        "status",
                        "Appointment has been completed; no changes allowed.");
                fieldErrors.add(fieldError);
            }
            if (! fieldErrors.isEmpty()) {
                throw new CustomApiInvalidParameterException(fieldErrors);
            }
        }

        // Archiving checks

        for (Employee employee : newSchedule.getEmployees()) {
            if (employee.isArchived()) {
                FieldError fieldError = new FieldError(
                        "schedule",
                        "employee",
                        String.valueOf(employee.getId()),
                        false,
                        null,
                        null,
                        "Requested employee is archived and cannot be scheduled.");
                fieldErrors.add(fieldError);
            }
        }
        for (Pet pet : newSchedule.getPets()) {
            if (pet.isArchived()) {
                FieldError fieldError = new FieldError(
                        "schedule",
                        "pet",
                        String.valueOf(pet.getId()),
                        false,
                        null,
                        null,
                        "Requested pet is archived and cannot be scheduled.");
                fieldErrors.add(fieldError);
            } else if (pet.getCustomer().isArchived()) {
                FieldError fieldError = new FieldError(
                        "schedule",
                        "customer",
                        String.valueOf(pet.getCustomer().getId()) +
                                " : " +
                                String.valueOf(pet.getId()),
                        false,
                        null,
                        null,
                        "Owner of requested pet is archived and cannot be scheduled.");
                fieldErrors.add(fieldError);
            }
        }

        // Start time checks (office open/close times)

        if (newSchedule.getStartTime() != null) {
            if (newSchedule.getStartTime().isBefore(officeSchedule.getOfficeOpens())) {
                FieldError fieldError = new FieldError(
                        "schedule",
                        "startTime",
                        newSchedule.getStartTime().toString(),
                        false,
                        null,
                        null,
                        "Start time cannot be before office opening time.");
                fieldErrors.add(fieldError);
            } else if (newSchedule.getStartTime().isAfter(officeSchedule.getOfficeCloses()) ||
                    newSchedule.getStartTime().equals(officeSchedule.getOfficeCloses())) {
                FieldError fieldError = new FieldError(
                        "schedule",
                        "startTime",
                        newSchedule.getStartTime().toString(),
                        false,
                        null,
                        null,
                        "Start time cannot be at or later than office closing time.");
                fieldErrors.add(fieldError);
            }
        }

        if (! fieldErrors.isEmpty()) {
            throw new CustomApiInvalidParameterException(fieldErrors);
        }
    }
}
