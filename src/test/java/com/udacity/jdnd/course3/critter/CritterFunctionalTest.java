package com.udacity.jdnd.course3.critter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.udacity.jdnd.course3.critter.activity.Activity;
import com.udacity.jdnd.course3.critter.activity.ActivityService;
import com.udacity.jdnd.course3.critter.date.DateVerification;
import com.udacity.jdnd.course3.critter.exception.CustomApiInvalidParameterException;
import com.udacity.jdnd.course3.critter.exception.CustomApiNoSuchElementException;
import com.udacity.jdnd.course3.critter.note.Note;
import com.udacity.jdnd.course3.critter.pet.*;
import com.udacity.jdnd.course3.critter.schedule.*;
import com.udacity.jdnd.course3.critter.user.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.FieldError;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * This is a set of functional tests to validate the basic capabilities desired for this application.
 * Students will need to configure the application to run these tests by adding application.properties file
 * to the test/resources directory that specifies the datasource. It can run using an in-memory H2 instance
 * and should not try to re-use the same datasource used by the rest of the app.
 *
 * These tests should all pass once the project is complete.
 */
@Transactional
@SpringBootTest(classes = CritterApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CritterFunctionalTest {

    @Autowired
    private ActivityService activityService;

    @Autowired
    private DateVerification dateVerification;

    @Autowired
    private OfficeScheduleService officeScheduleService;

    @Autowired
    private PetActivityTypeService petActivityTypeService;

    @Autowired
    private PetTypeService petTypeService;

    @Autowired
    private PetController petController;

    @Autowired
    private ScheduleController scheduleController;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private UserController userController;

    private static String testPetType = "UNICORN";

    @Test
    @Order(1)
    public void testCreateCustomer(){
        CustomerDTO customerDTO = createCustomerDTO();
        CustomerDTO newCustomer = userController.createUpdateCustomer(customerDTO);
        CustomerGetDTO retrievedCustomer = userController.getAllCustomers().get(0);
        Assertions.assertEquals(newCustomer.getName(), customerDTO.getName());
        Assertions.assertEquals(newCustomer.getId(), retrievedCustomer.getId());
        Assertions.assertTrue(retrievedCustomer.getId() > 0);
    }

    @Test
    @Order(2)
    public void testCreateEmployee(){
        EmployeeDTO employeeDTO = createEmployeeDTO();
        EmployeeDTO newEmployee = userController.createUpdateEmployee(employeeDTO);
        EmployeeGetDTO retrievedEmployee = userController.getEmployee(newEmployee.getId());
        Assertions.assertEquals(employeeDTO.getSkills(), newEmployee.getSkills());
        Assertions.assertEquals(newEmployee.getId(), retrievedEmployee.getId());
        Assertions.assertTrue(retrievedEmployee.getId() > 0);
    }

    @Test
    @Order(3)
    public void testAddPetsToCustomer() {
        CustomerDTO customerDTO = createCustomerDTO();
        CustomerDTO newCustomer = userController.createUpdateCustomer(customerDTO);

        PetDTO petDTO = createPetDTO();
        petDTO.setOwnerId(newCustomer.getId());
        PetDTO newPet = petController.createUpdatePet(petDTO);

        //make sure pet contains customer id
        PetGetDTO retrievedPet = petController.getPet(newPet.getId());
        Assertions.assertEquals(retrievedPet.getId(), newPet.getId());
        Assertions.assertEquals(retrievedPet.getOwnerId(), newCustomer.getId());

        //make sure you can retrieve pets by owner
        List<PetGetDTO> pets = petController.getPetsByOwner(newCustomer.getId());
        Assertions.assertEquals(newPet.getId(), pets.get(0).getId());
        Assertions.assertEquals(newPet.getName(), pets.get(0).getName());

        //check to make sure customer now also contains pet
        CustomerGetDTO retrievedCustomer = userController.getAllCustomers().get(0);
        Assertions.assertTrue(retrievedCustomer.getPetIds() != null && retrievedCustomer.getPetIds().size() > 0);
        Assertions.assertEquals(retrievedCustomer.getPetIds().get(0), retrievedPet.getId());
    }

    @Test
    @Order(4)
    public void testFindPetsByOwner() {
        CustomerDTO customerDTO = createCustomerDTO();
        CustomerDTO newCustomer = userController.createUpdateCustomer(customerDTO);

        PetDTO petDTO = createPetDTO();
        petDTO.setOwnerId(newCustomer.getId());
        PetDTO newPet = petController.createUpdatePet(petDTO);
        petDTO.setType(testPetType);
        petDTO.setName("Monoceratus");
        PetDTO newPet2 = petController.createUpdatePet(petDTO);

        List<PetGetDTO> pets = petController.getPetsByOwner(newCustomer.getId());
        Assertions.assertEquals(pets.size(), 2);
        Assertions.assertEquals(pets.get(0).getOwnerId(), newCustomer.getId());
        Assertions.assertEquals(pets.get(0).getId(), newPet.getId());
    }

    @Test
    @Order(5)
    public void testFindOwnerByPet() {
        CustomerDTO customerDTO = createCustomerDTO();
        CustomerDTO newCustomer = userController.createUpdateCustomer(customerDTO);
        
        PetDTO petDTO = createPetDTO();
        petDTO.setOwnerId(newCustomer.getId());
        PetDTO newPet = petController.createUpdatePet(petDTO);

        CustomerDTO owner = userController.getOwnerByPet(newPet.getId());

        Assertions.assertEquals(owner.getId(), newCustomer.getId());
        Assertions.assertEquals(owner.getPetIds().get(0), newPet.getId());
    }

    @Test
    @Order(6)
    public void testChangeEmployeeAvailability() {
        EmployeeDTO employeeDTO = createEmployeeDTO();
        EmployeeDTO emp1 = userController.createUpdateEmployee(employeeDTO);
        Assertions.assertNull(emp1.getDaysAvailable());

        List<String> availability = Lists.newArrayList("MONDAY", "TUESDAY", "WEDNESDAY");
        userController.setAvailability(availability, emp1.getId());

        EmployeeGetDTO emp2 = userController.getEmployee(emp1.getId());
        Assertions.assertEquals(availability, emp2.getDaysAvailable());
    }

    @Test
    @Order(7)
    public void testFindEmployeesByServiceAndTime() {
        EmployeeDTO emp1 = createEmployeeDTO();
        EmployeeDTO emp2 = createEmployeeDTO();
        EmployeeDTO emp3 = createEmployeeDTO();

        emp1.setDaysAvailable(Lists.newArrayList("MONDAY", "TUESDAY", "WEDNESDAY"));
        emp2.setDaysAvailable(Lists.newArrayList("WEDNESDAY", "THURSDAY", "FRIDAY"));
        emp3.setDaysAvailable(Lists.newArrayList("FRIDAY", "SATURDAY", "SUNDAY"));

        emp1.setSkills(Lists.newArrayList("FEEDING", "PETTING"));
        emp2.setSkills(Lists.newArrayList("PETTING", "WALKING"));
        emp3.setSkills(Lists.newArrayList("WALKING", "BATHING"));

        EmployeeDTO emp1n = userController.createUpdateEmployee(emp1);
        EmployeeDTO emp2n = userController.createUpdateEmployee(emp2);
        EmployeeDTO emp3n = userController.createUpdateEmployee(emp3);

        //make a request that matches employee 1 or 2
        EmployeeRequestDTO er1 = new EmployeeRequestDTO();
        er1.setDate("2019/12/25"); // Wednesday
        er1.setSkills(Lists.newArrayList("PETTING"));

        Set<Long> eIds1 = userController.findEmployeesForService(er1).stream().map(EmployeeGetDTO::getId).collect(Collectors.toSet());
        Set<Long> eIds1expected = Sets.newHashSet(emp1n.getId(), emp2n.getId());
        Assertions.assertEquals(eIds1, eIds1expected);

        //make a request that matches only employee 3
        EmployeeRequestDTO er2 = new EmployeeRequestDTO();
        er2.setDate("2019/12/27"); // Friday
        er2.setSkills(Lists.newArrayList("WALKING", "BATHING"));

        Set<Long> eIds2 = userController.findEmployeesForService(er2).stream().map(EmployeeGetDTO::getId).collect(Collectors.toSet());
        Set<Long> eIds2expected = Sets.newHashSet(emp3n.getId());
        Assertions.assertEquals(eIds2, eIds2expected);
    }

    @Test
    @Order(8)
    public void testSchedulePetsForServiceWithEmployee() throws Exception {
        EmployeeDTO employeeTemp = createEmployeeDTO();
        employeeTemp.setDaysAvailable(Lists.newArrayList("MONDAY", "TUESDAY", "WEDNESDAY"));
        EmployeeDTO employeeDTO = userController.createUpdateEmployee(employeeTemp);
        CustomerDTO customerDTO = userController.createUpdateCustomer(createCustomerDTO());
        PetDTO petTemp = createPetDTO();
        petTemp.setOwnerId(customerDTO.getId());
        PetDTO petDTO = petController.createUpdatePet(petTemp);

        String date = "2019/12/25";
        List<Long> petList = Lists.newArrayList(petDTO.getId());
        List<Long> employeeList = Lists.newArrayList(employeeDTO.getId());
        List<String> skillSet = Lists.newArrayList("PETTING");

        scheduleController.createUpdateSchedule(createScheduleDTO(petList, employeeList, date, skillSet));
        ScheduleQueryDTO scheduleQueryDTO = new ScheduleQueryDTO();
        ScheduleDTO scheduleDTO = scheduleController.getAllSchedules(scheduleQueryDTO).get(0);

        Assertions.assertEquals(scheduleDTO.getActivities(), skillSet);
        Assertions.assertEquals(scheduleDTO.getDate(), date);
        Assertions.assertEquals(scheduleDTO.getEmployeeIds(), employeeList);
        Assertions.assertEquals(scheduleDTO.getPetIds(), petList);
    }

    @Test
    @Order(9)
    public void testFindScheduleByEntities() {
        ScheduleDTO sched1 = populateSchedule(1, 2, "2019/12/25", Lists.newArrayList("BATHING", "PETTING", "WALKING"));
        ScheduleDTO sched2 = populateSchedule(3, 1, "2019/12/26", Lists.newArrayList("BATHING", "PETTING", "FEEDING"));

        //add a third schedule that shares some employees and pets with the other schedules
        ScheduleDTO sched3 = new ScheduleDTO();
        sched3.setEmployeeIds(sched1.getEmployeeIds());
        sched3.setPetIds(sched2.getPetIds());
        sched3.setActivities(Lists.newArrayList("BATHING", "PETTING"));
        sched3.setDate("2020/03/25");
        scheduleController.createUpdateSchedule(sched3);

        /*
            We now have 3 schedule entries. The third schedule entry has the same employees as the 1st schedule
            and the same pets/owners as the second schedule. So if we look up schedule entries for the employee from
            schedule 1, we should get both the first and third schedule as our result.
         */

        //Employee 1 in is both schedule 1 and 3
        List<ScheduleGetDTO> scheds1e = scheduleController.getScheduleForEmployee(sched1.getEmployeeIds().get(0));
        compareSchedules(sched1, scheds1e.get(0));
        compareSchedules(sched3, scheds1e.get(1));

        //Employee 2 is only in schedule 2
        List<ScheduleGetDTO> scheds2e = scheduleController.getScheduleForEmployee(sched2.getEmployeeIds().get(0));
        compareSchedules(sched2, scheds2e.get(0));

        //Pet 1 is only in schedule 1
        List<ScheduleGetDTO> scheds1p = scheduleController.getScheduleForPet(sched1.getPetIds().get(0));
        compareSchedules(sched1, scheds1p.get(0));

        //Pet from schedule 2 is in both schedules 2 and 3
        List<ScheduleGetDTO> scheds2p = scheduleController.getScheduleForPet(sched2.getPetIds().get(0));
        compareSchedules(sched2, scheds2p.get(0));
        compareSchedules(sched3, scheds2p.get(1));

        //Owner of the first pet will only be in schedule 1
        List<ScheduleGetDTO> scheds1c = scheduleController.getScheduleForCustomer(userController.getOwnerByPet(sched1.getPetIds().get(0)).getId());
        compareSchedules(sched1, scheds1c.get(0));

        //Owner of pet from schedule 2 will be in both schedules 2 and 3
        List<ScheduleGetDTO> scheds2c = scheduleController.getScheduleForCustomer(userController.getOwnerByPet(sched2.getPetIds().get(0)).getId());
        compareSchedules(sched2, scheds2c.get(0));
        compareSchedules(sched3, scheds2c.get(1));
    }

    private static EmployeeDTO createEmployeeDTO() {
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setName("TE" + System.currentTimeMillis());
        try {
            Thread.sleep(2);
        } catch (Exception e) {
            System.out.println("Pausing after ID selection.");
        }
        employeeDTO.setSkills(Lists.newArrayList("FEEDING", "PETTING"));
        return employeeDTO;
    }

    private static CustomerDTO createCustomerDTO() {
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setName("TC" + System.currentTimeMillis());
        try {
            Thread.sleep(2);
        } catch (Exception e) {
            System.out.println("Pausing after ID selection.");
        }
        customerDTO.setPhoneNumber("123-456-7890");
        return customerDTO;
    }

    private static PetDTO createPetDTO() {
        PetDTO petDTO = new PetDTO();
        petDTO.setName("TP" + System.currentTimeMillis());
        try {
            Thread.sleep(2);
        } catch (Exception e) {
            System.out.println("Pausing after ID selection.");
        }
        petDTO.setType(testPetType);
        return petDTO;
    }

    private static EmployeeRequestDTO createEmployeeRequestDTO() {
        EmployeeRequestDTO employeeRequestDTO = new EmployeeRequestDTO();
        employeeRequestDTO.setDate("2019/12/25"); // Wednesday
        employeeRequestDTO.setSkills(Lists.newArrayList("FEEDING", "WALKING"));
        return employeeRequestDTO;
    }

    private static ScheduleDTO createScheduleDTO(List<Long> petIds, List<Long> employeeIds, String date, List<String> activities) {
        ScheduleDTO scheduleDTO = new ScheduleDTO();
        scheduleDTO.setPetIds(petIds);
        scheduleDTO.setEmployeeIds(employeeIds);
        scheduleDTO.setDate(date);
        scheduleDTO.setActivities(activities);
        return scheduleDTO;
    }

    private ScheduleDTO populateSchedule(int numEmployees,
                                         int numPets,
                                         String date,
                                         List<String> activities) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateVerification.localDateFormat());
        LocalDate localDate = LocalDate.parse(date, formatter);
        List<Long> employeeIds = IntStream.range(0, numEmployees)
                .mapToObj(i -> createEmployeeDTO())
                .map(e -> {
                    e.setSkills(activities);
                    e.setDaysAvailable(Lists.newArrayList(localDate.getDayOfWeek().toString()));
                    return userController.createUpdateEmployee(e).getId();
                }).collect(Collectors.toList());
        CustomerDTO cust = userController.createUpdateCustomer(createCustomerDTO());
        List<Long> petIds = IntStream.range(0, numPets)
                .mapToObj(i -> createPetDTO())
                .map(p -> {
                    p.setOwnerId(cust.getId());
                    return petController.createUpdatePet(p).getId();
                }).collect(Collectors.toList());
        return scheduleController.createUpdateSchedule(createScheduleDTO(petIds, employeeIds, date, activities));
    }

    private static void compareSchedules(ScheduleDTO sched1, ScheduleDTO sched2) {
        Assertions.assertEquals(sched1.getPetIds(), sched2.getPetIds());
        Assertions.assertEquals(sched1.getActivities(), sched2.getActivities());
        Assertions.assertEquals(sched1.getEmployeeIds(), sched2.getEmployeeIds());
        Assertions.assertEquals(sched1.getDate(), sched2.getDate());
    }

    @Test
    @Order(10)
    public void testBadFormInput() {
        CustomApiInvalidParameterException eInvalid = null;
        CustomApiNoSuchElementException eNoSuch = null;

        //
        // Bad employee data
        //

        EmployeeDTO employeeDTO = createEmployeeDTO();
        employeeDTO.setNoteDescription("Test note description.");
        employeeDTO.setNoteText("Test note text.");
        EmployeeDTO badEmployeeDTO = new EmployeeDTO();
        BeanUtils.copyProperties(employeeDTO, badEmployeeDTO);

        // Bad employee ID

        badEmployeeDTO.setId(-1);
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    userController.createUpdateEmployee(badEmployeeDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "employee",
                "id",
                "-1",
                "Invalid entity ID.");

        // Bad employee name

        badEmployeeDTO.setName(" ");
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    userController.createUpdateEmployee(badEmployeeDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(1),
                "employee",
                "name",
                null,
                "Name is empty.");
        badEmployeeDTO.setName(new String(new char[201]).replace('\0', 'Z'));
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    userController.createUpdateEmployee(badEmployeeDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(1),
                "employee",
                "name",
                null,
                "Name is too long; maximum 200 characters.");

        // Bad employee skills

        BeanUtils.copyProperties(employeeDTO, badEmployeeDTO);
        badEmployeeDTO.setSkills(Lists.newArrayList("PETTING", "BARFING"));
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    userController.createUpdateEmployee(badEmployeeDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "employee",
                "activities",
                "BARFING",
                "Unknown activity.");

        // Bad availability setting

        badEmployeeDTO.setDaysAvailable(Lists.newArrayList("MONDAY", "FOOBARDAY"));
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    userController.createUpdateEmployee(badEmployeeDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(1),
                "employee",
                "daysAvailable",
                "FOOBARDAY",
                "Invalid day of week.");

        // Bad note created during employee creation/update

        BeanUtils.copyProperties(employeeDTO, badEmployeeDTO);
        badEmployeeDTO.setNoteDescription(" ");
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    userController.createUpdateEmployee(badEmployeeDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "note",
                "description",
                null,
                "Description is empty.");
        badEmployeeDTO.setNoteDescription(new String(new char[201]).replace('\0', 'Z'));
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    userController.createUpdateEmployee(badEmployeeDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "note",
                "description",
                null,
                "Description is too long; maximum 200 characters.");
        BeanUtils.copyProperties(employeeDTO, badEmployeeDTO);
        badEmployeeDTO.setNoteText(" ");
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    userController.createUpdateEmployee(badEmployeeDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "note",
                "note",
                null,
                "Note is empty.");
        badEmployeeDTO.setNoteText(new String(new char[2001]).replace('\0', 'Z'));
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    userController.createUpdateEmployee(badEmployeeDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "note",
                "note",
                null,
                "Note is too long; maximum 2000 characters.");
        EmployeeDTO newEmployee = userController.createUpdateEmployee(employeeDTO);

        // Duplicate employee

        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    userController.createUpdateEmployee(employeeDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "employee",
                "name",
                newEmployee.getName(),
                "An employee with that name already exists.");

        // Failed retrieval/update by employee ID

        eNoSuch = Assertions.assertThrows(
                CustomApiNoSuchElementException.class, () -> {
                    userController.getEmployee(1000);
                });
        matchFieldError(eNoSuch.getFieldError(),
                "employee",
                "id",
                "1000",
                "Unknown employee ID.");
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    userController.setAvailability(
                            Lists.newArrayList(
                                    "MONDAY",
                                    "TUESDAY",
                                    "WEDNESDAY"),
                            1000);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "employee",
                "id",
                "1000",
                "Unknown employee ID.");

        // Retrieve employees by bad skill set and invalid date

        EmployeeRequestDTO employeeRequestDTO = new EmployeeRequestDTO();
        employeeRequestDTO.setDate("BADDATEFORMAT");
        employeeRequestDTO.setSkills(Lists.newArrayList("PETTING", "BARFING"));
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    userController.findEmployeesForService(employeeRequestDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "employee",
                "activities",
                "BARFING",
                "Unknown activity.");
        matchFieldError(eInvalid.getFieldErrors().get(1),
                "employee",
                "date",
                "BADDATEFORMAT",
                "Invalid date; correct format " +
                        dateVerification.localDateFormat() + ".");

        // Successful employee availability setting
        // Note the duplicate (for testing)

        userController.setAvailability(
                Lists.newArrayList(
                        "MONDAY",
                        "MONDAY",
                        "TUESDAY",
                        "WEDNESDAY",
                        "THURSDAY",
                        "FRIDAY",
                        "SATURDAY"),
                newEmployee.getId());

        //
        // Bad customer data
        //

        CustomerDTO customerDTO = createCustomerDTO();
        CustomerDTO badCustomerDTO = new CustomerDTO();
        BeanUtils.copyProperties(customerDTO, badCustomerDTO);

        // Bad customer ID

        badCustomerDTO.setId(-1);
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    userController.createUpdateCustomer(badCustomerDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "customer",
                "id",
                "-1",
                "Invalid entity ID.");

        // Bad customer name

        badCustomerDTO.setName(" ");
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    userController.createUpdateCustomer(badCustomerDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(1),
                "customer",
                "name",
                null,
                "Name is empty.");
        badCustomerDTO.setName(new String(new char[201]).replace('\0', 'Z'));
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    userController.createUpdateCustomer(badCustomerDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(1),
                "customer",
                "name",
                null,
                "Name is too long; maximum 200 characters.");

        // Bad phone number

        badCustomerDTO.setPhoneNumber(null);
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    userController.createUpdateCustomer(badCustomerDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(2),
                "customer",
                "phoneNumber",
                null,
                "Phone number is empty.");
        badCustomerDTO.setPhoneNumber(new String(new char[51]).replace('\0', '1'));
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    userController.createUpdateCustomer(badCustomerDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(2),
                "customer",
                "phoneNumber",
                null,
                "Phone number is too long; maximum 50 characters.");
        badCustomerDTO.setPhoneNumber("@#$%^&*()");
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    userController.createUpdateCustomer(badCustomerDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(2),
                "customer",
                "phoneNumber",
                badCustomerDTO.getPhoneNumber(),
                "Phone number is invalid; only digits, hyphens, and spaces allowed.");

        // Bad note created during customer creation/update

        BeanUtils.copyProperties(customerDTO, badCustomerDTO);
        badCustomerDTO.setNoteDescription(" ");
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    userController.createUpdateCustomer(badCustomerDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "note",
                "description",
                null,
                "Description is empty.");
        badCustomerDTO.setNoteDescription(new String(new char[201]).replace('\0', 'Z'));
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    userController.createUpdateCustomer(badCustomerDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "note",
                "description",
                null,
                "Description is too long; maximum 200 characters.");
        BeanUtils.copyProperties(customerDTO, badCustomerDTO);
        badCustomerDTO.setNoteText(" ");
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    userController.createUpdateCustomer(badCustomerDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(1),
                "note",
                "note",
                null,
                "Note is empty.");
        badCustomerDTO.setNoteText(new String(new char[2001]).replace('\0', 'Z'));
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    userController.createUpdateCustomer(badCustomerDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(1),
                "note",
                "note",
                null,
                "Note is too long; maximum 2000 characters.");
        CustomerDTO newCustomer = userController.createUpdateCustomer(customerDTO);

        // Duplicate customer

        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    userController.createUpdateCustomer(customerDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "customer",
                "name + phoneNumber",
                newCustomer.getName() + " " + newCustomer.getPhoneNumber(),
                "A customer with that name and phone number already exists.");
        eNoSuch = Assertions.assertThrows(
                CustomApiNoSuchElementException.class, () -> {
                    userController.getCustomer(1000);
                });
        matchFieldError(eNoSuch.getFieldError(),
                "customer",
                "id",
                "1000",
                "Unknown customer ID.");

        //
        // Bad pet data
        //

        PetDTO petDTO = createPetDTO();

        // Bad customer ID

        petDTO.setOwnerId(1000);
        eNoSuch = Assertions.assertThrows(
                CustomApiNoSuchElementException.class, () -> {
                    petController.createUpdatePet(petDTO);
                });
        matchFieldError(eNoSuch.getFieldError(),
                "customer",
                "id",
                "1000",
                "Unknown customer ID.");
        petDTO.setOwnerId(newCustomer.getId());

        // Bad pet ID

        PetDTO badPetDTO = new PetDTO();
        BeanUtils.copyProperties(petDTO, badPetDTO);
        badPetDTO.setId(-1);
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    petController.createUpdatePet(badPetDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "pet",
                "id",
                String.valueOf(-1L),
                "Invalid entity ID.");

        // Bad pet name

        badPetDTO.setName(" ");
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    petController.createUpdatePet(badPetDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(1),
                "pet",
                "name",
                null,
                "Name is empty.");
        badPetDTO.setName(new String(new char[101]).replace('\0', 'Z'));
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    petController.createUpdatePet(badPetDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(1),
                "pet",
                "name",
                null,
                "Name is too long; maximum 100 characters.");

        // Bad birth date

        String tomorrowLocal = LocalDate.now().plusDays(1).toString();
        String tomorrow = tomorrowLocal.replaceAll("-", "/");
        badPetDTO.setBirthDate(tomorrow);
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    petController.createUpdatePet(badPetDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(1),
                "pet",
                "birthDate",
                tomorrowLocal,
                "Birth date is in the future.");
        badPetDTO.setBirthDate("NOTAVALIDDATE");
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    petController.createUpdatePet(badPetDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "pet",
                "birthDate",
                "NOTAVALIDDATE",
                "Invalid birth date.");

        // Invalid pet type

        badPetDTO.setType("BADPETTYPE");
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    petController.createUpdatePet(badPetDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(1),
                "pet",
                "type",
                "BADPETTYPE",
                "Unknown pet type.");


        // Currently unserviced pet type

        badPetDTO.setType("CHIMERA");
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    petController.createUpdatePet(badPetDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(1),
                "pet",
                "type",
                "CHIMERA",
                "Pet type currently not serviced.");

        // Invalid species/breed designation

        badPetDTO.setBreedOrSpecies(new String(
                new char[201]).replace('\0', 'Z'));
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    petController.createUpdatePet(badPetDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(3),
                "pet",
                "breedOrSpecies",
                null,
                "Breed/species name is too long; maximum 100 characters.");
        BeanUtils.copyProperties(petDTO, badPetDTO);

        // Invalid note created during pet creation/deletion

        badPetDTO.setNoteDescription(" ");
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    petController.createUpdatePet(badPetDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "note",
                "description",
                null,
                "Description is empty.");
        badPetDTO.setNoteDescription(new String(new char[201]).replace('\0', 'Z'));
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    petController.createUpdatePet(badPetDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "note",
                "description",
                null,
                "Description is too long; maximum 200 characters.");
        BeanUtils.copyProperties(petDTO, badPetDTO);
        badPetDTO.setNoteText(" ");
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    petController.createUpdatePet(badPetDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(1),
                "note",
                "note",
                null,
                "Note is empty.");
        badPetDTO.setNoteText(new String(new char[2001]).replace('\0', 'Z'));
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    petController.createUpdatePet(badPetDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(1),
                "note",
                "note",
                null,
                "Note is too long; maximum 2000 characters.");

        // Target owner is archived

        BeanUtils.copyProperties(petDTO, badPetDTO);
        CustomerDTO archivedCustomerDTO = createCustomerDTO();
        archivedCustomerDTO.setArchived(true);
        CustomerDTO newArchivedCustomer = userController.createUpdateCustomer(archivedCustomerDTO);
        badPetDTO.setOwnerId(newArchivedCustomer.getId());
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    petController.createUpdatePet(badPetDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "customer",
                "archived",
                "true",
                "Pet target owner entry is archived; no changes allowed.");
        PetDTO newPet = petController.createUpdatePet(petDTO);

        // Duplicate pet under customer

        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    PetDTO newPetDuplicate = petController.createUpdatePet(petDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "pet",
                "type + name + customer",
                null,
                "A pet of that type and name under that customer already exists.");

        // Target customer for update is archived

        PetDTO testBadPetDTO = newPet;
        testBadPetDTO.setOwnerId(newArchivedCustomer.getId());
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    petController.createUpdatePet(testBadPetDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "customer",
                "archived",
                "true",
                "Pet target owner entry is archived; no changes allowed.");

        //
        // Bad schedule data
        //

        // Create valid initial customer/pet/employee data for test

        LocalDate appointmentDate = LocalDate.now();
        appointmentDate = appointmentDate.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        String date = appointmentDate.toString().replace("-","/");
        List<Long> petList = Lists.newArrayList(newPet.getId());
        EmployeeDTO newScheduleEmployee = createEmployeeDTO();
        newScheduleEmployee.setSkills(Lists.newArrayList("WALKING","AGILITY"));
        newScheduleEmployee = userController.createUpdateEmployee(newScheduleEmployee);
        userController.setAvailability(
                Lists.newArrayList(
                        "MONDAY",
                        "TUESDAY",
                        "WEDNESDAY",
                        "THURSDAY",
                        "FRIDAY",
                        "SATURDAY"),
                newScheduleEmployee.getId());
        List<Long> employeeList = Lists.newArrayList(newScheduleEmployee.getId());
        List<String> skillSet = Lists.newArrayList("WALKING", "AGILITY");
        ScheduleDTO scheduleDTO = createScheduleDTO(petList, employeeList, date, skillSet);
        ScheduleDTO badScheduleDTO = new ScheduleDTO();
        BeanUtils.copyProperties(scheduleDTO, badScheduleDTO);

        // Invalid activities specified for appointment

        badScheduleDTO.setActivities(null);
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    scheduleController.createUpdateSchedule(badScheduleDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "schedule",
                "activities",
                null,
                "No activities scheduled.");
        List<String> badSkillSet = Lists.newArrayList("FOOBAR", "POOHBEAR");
        badScheduleDTO.setActivities(badSkillSet);
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    scheduleController.createUpdateSchedule(badScheduleDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "schedule",
                "activities",
                "FOOBAR",
                "Unknown activity.");
        matchFieldError(eInvalid.getFieldErrors().get(1),
                "schedule",
                "activities",
                "POOHBEAR",
                "Unknown activity.");

        // Invalid employees specified

        badScheduleDTO.setEmployeeIds(null);
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    scheduleController.createUpdateSchedule(badScheduleDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(3),
                "schedule",
                "employees",
                null,
                "No employees scheduled.");
        badScheduleDTO.setEmployeeIds(Lists.newArrayList(1000L, 1001L));
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    scheduleController.createUpdateSchedule(badScheduleDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(2),
                "employee",
                "id",
                String.valueOf(1000L),
                "Unknown employee ID.");
        matchFieldError(eInvalid.getFieldErrors().get(3),
                "employee",
                "id",
                String.valueOf(1001L),
                "Unknown employee ID.");

        // Create employees without required skills for testing

        badScheduleDTO.setActivities(skillSet);
        EmployeeDTO firstBadEmployeeDTO = createEmployeeDTO();
        EmployeeDTO firstBadEmployee = userController.createUpdateEmployee(firstBadEmployeeDTO);
        EmployeeDTO secondBadEmployeeDTO = createEmployeeDTO();
        EmployeeDTO secondBadEmployee = userController.createUpdateEmployee(secondBadEmployeeDTO);
        // Note the duplicate for testing
        firstBadEmployee.setSkills(Lists.newArrayList("FEEDING","FEEDING"));
        firstBadEmployee = userController.createUpdateEmployee(firstBadEmployee);
        secondBadEmployee.setSkills(Lists.newArrayList("WALKING","FEEDING"));
        secondBadEmployee = userController.createUpdateEmployee(secondBadEmployee);
        badScheduleDTO.setEmployeeIds(Lists.newArrayList(
                firstBadEmployee.getId(),
                secondBadEmployee.getId()));

        // Missing employee skills

        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    scheduleController.createUpdateSchedule(badScheduleDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "schedule",
                "employees",
                "Employee ID " + String.valueOf(firstBadEmployee.getId()) + " : WALKING",
                "Requested employee does not have required skill.");
        matchFieldError(eInvalid.getFieldErrors().get(1),
                "schedule",
                "employees",
                "Employee ID " + String.valueOf(firstBadEmployee.getId()) + " : AGILITY",
                "Requested employee does not have required skill.");
        matchFieldError(eInvalid.getFieldErrors().get(2),
                "schedule",
                "employees",
                "Employee ID " + String.valueOf(secondBadEmployee.getId()) + " : AGILITY",
                "Requested employee does not have required skill.");
        badScheduleDTO.setEmployeeIds(employeeList);

        // Invalid appointment date

        badScheduleDTO.setDate("NOTAVALIDDATE");
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    scheduleController.createUpdateSchedule(badScheduleDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "schedule",
                "date",
                "NOTAVALIDDATE",
                "Invalid date; correct format " +
                        dateVerification.localDateFormat() + ".");

        // Invalid notes

        badScheduleDTO.setNotes(new String(new char[10001]).replace('\0', 'Z'));
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    scheduleController.createUpdateSchedule(badScheduleDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(1),
                "schedule",
                "notes",
                null,
                "Notes cannot exceed 10000 characters.");

        // Invalid appointment status

        badScheduleDTO.setStatus("FOOBAR");
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    scheduleController.createUpdateSchedule(badScheduleDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(1),
                "schedule",
                "status",
                "FOOBAR",
                "Invalid status.");

        // Invalid start time

        BeanUtils.copyProperties(scheduleDTO, badScheduleDTO);
        badScheduleDTO.setStartTime("NOTAVALIDTIME");
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    scheduleController.createUpdateSchedule(badScheduleDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "schedule",
                "startTime",
                "NOTAVALIDTIME",
                "Invalid time; correct format " +
                        dateVerification.localTimeFormat() + ".");
        badScheduleDTO.setStartTime("08:31");
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    scheduleController.createUpdateSchedule(badScheduleDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "schedule",
                "startTime",
                "08:31",
                "Times must be specified in five-minute intervals.");
        badScheduleDTO.setStartTime("01:00");
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    scheduleController.createUpdateSchedule(badScheduleDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "schedule",
                "startTime",
                "01:00",
                "Start time cannot be before office opening time.");
        badScheduleDTO.setStartTime("23:00");
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    scheduleController.createUpdateSchedule(badScheduleDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "schedule",
                "startTime",
                "23:00",
                "Start time cannot be at or later than office closing time.");

        // Update employees with invalid availability for testing

        BeanUtils.copyProperties(scheduleDTO, badScheduleDTO);
        firstBadEmployee.setSkills(Lists.newArrayList("WALKING", "AGILITY"));
        firstBadEmployee = userController.createUpdateEmployee(firstBadEmployee);
        secondBadEmployee.setSkills(Lists.newArrayList("WALKING","AGILITY"));
        secondBadEmployee.setDaysAvailable(Lists.newArrayList("FRIDAY","SATURDAY"));
        secondBadEmployee = userController.createUpdateEmployee(secondBadEmployee);

        // Incorrect employee availability

        badScheduleDTO.setEmployeeIds(Lists.newArrayList(
                firstBadEmployee.getId(),
                secondBadEmployee.getId()));
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    scheduleController.createUpdateSchedule(badScheduleDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "schedule",
                "employees",
                "Employee ID " + String.valueOf(firstBadEmployee.getId()),
                "Requested employee has no set availability.");
        matchFieldError(eInvalid.getFieldErrors().get(1),
                "schedule",
                "employees",
                "Employee ID " +
                        String.valueOf(secondBadEmployee.getId()) +
                        " : MONDAY",
                "Requested employee does not work on requested day.");

        // Invalid or missing pets for appointment

        BeanUtils.copyProperties(scheduleDTO, badScheduleDTO);
        badScheduleDTO.setPetIds(null);
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    scheduleController.createUpdateSchedule(badScheduleDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "schedule",
                "pets",
                null,
                "No pets scheduled.");
        badScheduleDTO.setPetIds(Lists.newArrayList(1000L, 1001L));
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    scheduleController.createUpdateSchedule(badScheduleDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "pet",
                "id",
                String.valueOf(1000L),
                "Unknown pet ID.");
        matchFieldError(eInvalid.getFieldErrors().get(1),
                "pet",
                "id",
                String.valueOf(1001L),
                "Unknown pet ID.");

        // Create pets of invalid type for appointment activities

        CustomerDTO secondNewCustomerDTO = createCustomerDTO();
        CustomerDTO secondNewCustomer = userController.createUpdateCustomer(secondNewCustomerDTO);
        PetDTO firstBadPetDTO = createPetDTO();
        firstBadPetDTO.setType("GRYPHON");
        firstBadPetDTO.setOwnerId(newCustomer.getId());
        PetDTO firstBadPet = petController.createUpdatePet(firstBadPetDTO);
        PetDTO secondBadPetDTO = createPetDTO();
        secondBadPetDTO.setType("HYDRA");
        secondBadPetDTO.setOwnerId(secondNewCustomer.getId());
        PetDTO secondBadPet = petController.createUpdatePet(secondBadPetDTO);

        // Invalid activities for pet type

        badScheduleDTO.setPetIds(Lists.newArrayList(
                firstBadPet.getId(),
                secondBadPet.getId()));
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    scheduleController.createUpdateSchedule(badScheduleDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "schedule",
                "pets",
                "Pet Type GRYPHON : Activity Type WALKING",
                "Activity not available for indicated pet type.");
        matchFieldError(eInvalid.getFieldErrors().get(1),
                "schedule",
                "pets",
                "Pet Type GRYPHON : Activity Type AGILITY",
                "Activity not available for indicated pet type.");

        // Pets must all belong to the same customer

        matchFieldError(eInvalid.getFieldErrors().get(2),
                "schedule",
                "pets",
                "Pet ID " +
                        String.valueOf(secondBadPet.getId()) +
                        " : Customer ID " +
                        String.valueOf(secondBadPet.getOwnerId()) +
                        ", Pet ID " +
                        String.valueOf(firstBadPet.getId()) +
                        " : Customer ID " +
                        String.valueOf(firstBadPet.getOwnerId()),
                "Pets do not all belong to the same customer.");

        // Repeated checks for pet type/activity mismatch

        matchFieldError(eInvalid.getFieldErrors().get(3),
                "schedule",
                "pets",
                "Pet Type HYDRA : Activity Type WALKING",
                "Activity not available for indicated pet type.");
        matchFieldError(eInvalid.getFieldErrors().get(4),
                "schedule",
                "pets",
                "Pet Type HYDRA : Activity Type AGILITY",
                "Activity not available for indicated pet type.");

        //
        // Bad note data
        // NOTE: These are notes created independently of employee, customer, or
        // pet creation/update
        //

        //
        // Bad employee note data
        //

        EmployeeNoteDTO employeeNoteDTO = new EmployeeNoteDTO();

        // Bad employee specification

        eNoSuch = Assertions.assertThrows(
                CustomApiNoSuchElementException.class, () -> {
                    userController.createUpdateEmployeeNote(employeeNoteDTO);
                });
        matchFieldError(eNoSuch.getFieldError(),
                "employee",
                "id",
                null,
                "Missing employee ID.");
        employeeNoteDTO.setEmployeeId(-1L);
        eNoSuch = Assertions.assertThrows(
                CustomApiNoSuchElementException.class, () -> {
                    userController.createUpdateEmployeeNote(employeeNoteDTO);
                });
        matchFieldError(eNoSuch.getFieldError(),
                "employee",
                "id",
                String.valueOf(-1L),
                "Unknown employee ID.");
        employeeNoteDTO.setEmployeeId(newEmployee.getId());

        // Invalid description

        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    userController.createUpdateEmployeeNote(employeeNoteDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "note",
                "description",
                null,
                "Description is empty.");
        employeeNoteDTO.setDescription(new String(new char[201]).replace('\0', 'Z'));
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    userController.createUpdateEmployeeNote(employeeNoteDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "note",
                "description",
                null,
                "Description is too long; maximum 200 characters.");
        employeeNoteDTO.setDescription("Test description.");
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    userController.createUpdateEmployeeNote(employeeNoteDTO);
                });

        // Invalid note text

        matchFieldError(eInvalid.getFieldErrors().get(0),
                "note",
                "note",
                null,
                "Note is empty.");
        employeeNoteDTO.setNote(new String(new char[2001]).replace('\0', 'Z'));
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    userController.createUpdateEmployeeNote(employeeNoteDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "note",
                "note",
                null,
                "Note is too long; maximum 2000 characters.");

        // Invalid parameters for note retrieval

        eNoSuch = Assertions.assertThrows(
                CustomApiNoSuchElementException.class, () -> {
                    userController.getEmployeeNote(1000L, 1000L);
                });
        matchFieldError(eNoSuch.getFieldError(),
                "note",
                "employee ID + note ID",
                "1000, 1000",
                "No such note exists.");
        eNoSuch = Assertions.assertThrows(
                CustomApiNoSuchElementException.class, () -> {
                    userController.getEmployeeNote(newEmployee.getId(), 1000L);
                });
        matchFieldError(eNoSuch.getFieldError(),
                "note",
                "employee ID + note ID",
                String.valueOf(newEmployee.getId()) + ", 1000",
                "No such note exists.");

        // Bad employee ID for retrieval of all notes

        eNoSuch = Assertions.assertThrows(
                CustomApiNoSuchElementException.class, () -> {
                    userController.getEmployeeNotes(1000L);
                });
        matchFieldError(eNoSuch.getFieldError(),
                "employee",
                "id",
                String.valueOf(1000),
                "Unknown employee ID.");
        eNoSuch = Assertions.assertThrows(
                CustomApiNoSuchElementException.class, () -> {
                    userController.getEmployeeNoteIds(1000L);
                });
        matchFieldError(eNoSuch.getFieldError(),
                "employee",
                "id",
                String.valueOf(1000),
                "Unknown employee ID.");

        //
        // Bad customer note data
        //

        CustomerNoteDTO customerNoteDTO = new CustomerNoteDTO();

        // Bad customer specification

        eNoSuch = Assertions.assertThrows(
                CustomApiNoSuchElementException.class, () -> {
                    userController.createUpdateCustomerNote(customerNoteDTO);
                });
        matchFieldError(eNoSuch.getFieldError(),
                "customer",
                "id",
                null,
                "Missing customer ID.");
        customerNoteDTO.setCustomerId(-1L);
        eNoSuch = Assertions.assertThrows(
                CustomApiNoSuchElementException.class, () -> {
                    userController.createUpdateCustomerNote(customerNoteDTO);
                });
        matchFieldError(eNoSuch.getFieldError(),
                "customer",
                "id",
                String.valueOf(-1L),
                "Unknown customer ID.");
        customerNoteDTO.setCustomerId(newCustomer.getId());

        // Invalid description

        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    userController.createUpdateCustomerNote(customerNoteDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "note",
                "description",
                null,
                "Description is empty.");
        customerNoteDTO.setDescription(new String(new char[201]).replace('\0', 'Z'));
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    userController.createUpdateCustomerNote(customerNoteDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "note",
                "description",
                null,
                "Description is too long; maximum 200 characters.");
        customerNoteDTO.setDescription("Test description.");

        // Invalid note text

        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    userController.createUpdateCustomerNote(customerNoteDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "note",
                "note",
                null,
                "Note is empty.");
        customerNoteDTO.setNote(new String(new char[2001]).replace('\0', 'Z'));
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    userController.createUpdateCustomerNote(customerNoteDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "note",
                "note",
                null,
                "Note is too long; maximum 2000 characters.");

        // Invalid parameters for note retrieval

        eNoSuch = Assertions.assertThrows(
                CustomApiNoSuchElementException.class, () -> {
                    userController.getCustomerNote(1000L, 1000L);
                });
        matchFieldError(eNoSuch.getFieldError(),
                "note",
                "customer ID + note ID",
                "1000, 1000",
                "No such note exists.");
        eNoSuch = Assertions.assertThrows(
                CustomApiNoSuchElementException.class, () -> {
                    userController.getCustomerNote(newCustomer.getId(), 1000L);
                });
        matchFieldError(eNoSuch.getFieldError(),
                "note",
                "customer ID + note ID",
                String.valueOf(newCustomer.getId()) + ", 1000",
                "No such note exists.");

        // Bad customer ID for retrieval of all notes

        eNoSuch = Assertions.assertThrows(
                CustomApiNoSuchElementException.class, () -> {
                    userController.getCustomerNotes(1000L);
                });
        matchFieldError(eNoSuch.getFieldError(),
                "customer",
                "id",
                String.valueOf(1000),
                "Unknown customer ID.");
        eNoSuch = Assertions.assertThrows(
                CustomApiNoSuchElementException.class, () -> {
                    userController.getCustomerNoteIds(1000L);
                });
        matchFieldError(eNoSuch.getFieldError(),
                "customer",
                "id",
                String.valueOf(1000),
                "Unknown customer ID.");

        //
        // Bad pet note data
        //

        PetNoteDTO petNoteDTO = new PetNoteDTO();

        // Bad pet specification

        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    petController.createUpdatePetNote(petNoteDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "pet",
                "id",
                null,
                "Missing pet ID.");
        petNoteDTO.setPetId(-1L);
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    petController.createUpdatePetNote(petNoteDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "pet",
                "id",
                String.valueOf(-1L),
                "Unknown pet ID.");
        petNoteDTO.setPetId(newPet.getId());

        // Invalid description

        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    petController.createUpdatePetNote(petNoteDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "note",
                "description",
                null,
                "Description is empty.");
        petNoteDTO.setDescription(new String(new char[201]).replace('\0', 'Z'));
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    petController.createUpdatePetNote(petNoteDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "note",
                "description",
                null,
                "Description is too long; maximum 200 characters.");
        petNoteDTO.setDescription("Test description.");

        // Invalid note text

        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    petController.createUpdatePetNote(petNoteDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "note",
                "note",
                null,
                "Note is empty.");
        petNoteDTO.setNote(new String(new char[2001]).replace('\0', 'Z'));
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    petController.createUpdatePetNote(petNoteDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "note",
                "note",
                null,
                "Note is too long; maximum 2000 characters.");

        // Invalid parameters for note retrieval

        eNoSuch = Assertions.assertThrows(
                CustomApiNoSuchElementException.class, () -> {
                    petController.getPetNote(1000L, 1000L);
                });
        matchFieldError(eNoSuch.getFieldError(),
                "note",
                "pet ID + note ID",
                "1000, 1000",
                "No such note exists.");
        eNoSuch = Assertions.assertThrows(
                CustomApiNoSuchElementException.class, () -> {
                    petController.getPetNote(newPet.getId(), 1000L);
                });
        matchFieldError(eNoSuch.getFieldError(),
                "note",
                "pet ID + note ID",
                String.valueOf(newPet.getId()) + ", 1000",
                "No such note exists.");

        // Bad pet ID for retrieval of all notes

        eNoSuch = Assertions.assertThrows(
                CustomApiNoSuchElementException.class, () -> {
                    petController.getPetNotes(1000L);
                });
        matchFieldError(eNoSuch.getFieldError(),
                "pet",
                "id",
                String.valueOf(1000),
                "Unknown pet ID.");
        eNoSuch = Assertions.assertThrows(
                CustomApiNoSuchElementException.class, () -> {
                    petController.getPetNoteIds(1000L);
                });
        matchFieldError(eNoSuch.getFieldError(),
                "pet",
                "id",
                String.valueOf(1000),
                "Unknown pet ID.");

        // Bad availability search

        EmployeeRequestDTO badEmployeeRequestDTO = new EmployeeRequestDTO();
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    userController.findEmployeesForService(badEmployeeRequestDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "schedule",
                "skills",
                null,
                "Skills required.");
        matchFieldError(eInvalid.getFieldErrors().get(1),
                "schedule",
                "date",
                null,
                "Date required.");
    }

    @Test
    @Order(11)
    public void testUpdate() {

        List<OfficeSchedule> allOfficeDays = officeScheduleService.findAll();
        OfficeSchedule officeSchedule = allOfficeDays.get(0);
        OfficeSchedule updateOfficeSchedule = allOfficeDays.get(1);
        DayOfWeek dayOfWeek = DayOfWeek.valueOf(officeSchedule.getDayOfWeek());
        DayOfWeek updateDayOfWeek = DayOfWeek.valueOf(updateOfficeSchedule.getDayOfWeek());
        LocalDate appointmentDate = LocalDate.now();
        appointmentDate = appointmentDate.with(TemporalAdjusters.next(dayOfWeek));
        String date = appointmentDate.toString().replace("-","/");
        appointmentDate = LocalDate.now();
        appointmentDate = appointmentDate.with(TemporalAdjusters.next(updateDayOfWeek));
        String updateDate = appointmentDate.toString().replace("-","/");
        String startTime = officeSchedule.getOfficeOpens().toString();
        String updateStartTime = updateOfficeSchedule
                .getOfficeOpens()
                .plusMinutes(15)
                .toString();

        LocalDateTime insertTime;
        Long updateId;
        String updateName;
        List<String> updateSkills = Lists.newArrayList(
                "WALKING",
                "AGILITY");
        List<String> scheduleUpdateSkills = Lists.newArrayList(
                "FEEDING",
                "PETTING");
        List<String> updateDaysAvailable = Lists.newArrayList(
                dayOfWeek.toString());
        List<String> scheduleUpdateDaysAvailable = Lists.newArrayList(
                updateDayOfWeek.toString());

        //
        // Employee update
        //

        EmployeeDTO employeeDTO = createEmployeeDTO();
        employeeDTO = userController.createUpdateEmployee(employeeDTO);
        insertTime = employeeDTO.getLastUpdateTime();
        updateId = employeeDTO.getId();
        updateName = "TE" + System.currentTimeMillis();
        employeeDTO.setName(updateName);
        employeeDTO.setSkills(updateSkills);
        employeeDTO.setDaysAvailable(updateDaysAvailable);
        employeeDTO.setArchived(true);
        employeeDTO = userController.createUpdateEmployee(employeeDTO);
        Assertions.assertEquals(updateId, employeeDTO.getId());
        Assertions.assertEquals(updateName, employeeDTO.getName());
        Assertions.assertEquals(updateSkills, employeeDTO.getSkills());
        Assertions.assertEquals(updateDaysAvailable, employeeDTO.getDaysAvailable());
        Assertions.assertTrue(employeeDTO.isArchived());
        Assertions.assertTrue(
                insertTime
                        .compareTo(employeeDTO.getLastUpdateTime()) < 0);
        EmployeeGetDTO employeeGetDTO = userController.getEmployee(updateId);
        Assertions.assertEquals(employeeGetDTO.getId(), employeeDTO.getId());
        Assertions.assertEquals(employeeGetDTO.getName(), employeeDTO.getName());
        Assertions.assertEquals(employeeGetDTO.getSkills(), employeeDTO.getSkills());
        Assertions.assertEquals(employeeGetDTO.getDaysAvailable(), employeeDTO.getDaysAvailable());
        Assertions.assertTrue(employeeGetDTO.isArchived());
        Assertions.assertTrue(
                employeeGetDTO
                        .getLastUpdateTime()
                        .compareTo(employeeDTO.getLastUpdateTime()) == 0);
        employeeDTO.setArchived(false);
        employeeDTO = userController.createUpdateEmployee(employeeDTO);

        //
        // Customer update
        //

        CustomerDTO customerDTO = createCustomerDTO();
        customerDTO = userController.createUpdateCustomer(customerDTO);
        insertTime = customerDTO.getLastUpdateTime();
        updateId = customerDTO.getId();
        updateName = "TC" + System.currentTimeMillis();
        customerDTO.setName(updateName);
        String updatePhoneNumber = "098-765-4321";
        customerDTO.setPhoneNumber(updatePhoneNumber);
        customerDTO.setArchived(true);
        customerDTO = userController.createUpdateCustomer(customerDTO);
        Assertions.assertEquals(updateId, customerDTO.getId());
        Assertions.assertEquals(updateName, customerDTO.getName());
        Assertions.assertEquals(updatePhoneNumber, customerDTO.getPhoneNumber());
        Assertions.assertTrue(customerDTO.isArchived());
        Assertions.assertTrue(
                insertTime
                        .compareTo(customerDTO.getLastUpdateTime()) < 0);
        CustomerGetDTO customerGetDTO = userController.getCustomer(updateId);
        Assertions.assertEquals(customerGetDTO.getId(), customerDTO.getId());
        Assertions.assertEquals(customerGetDTO.getName(), customerDTO.getName());
        Assertions.assertEquals(customerGetDTO.getPhoneNumber(), customerDTO.getPhoneNumber());
        Assertions.assertTrue(customerGetDTO.isArchived());
        Assertions.assertTrue(
                customerGetDTO
                        .getLastUpdateTime()
                        .compareTo(customerDTO.getLastUpdateTime()) == 0);
        customerDTO.setArchived(false);
        customerDTO = userController.createUpdateCustomer(customerDTO);

        //
        // Pet update
        //

        PetDTO petDTO = createPetDTO();
        CustomerDTO secondCustomerDTO = createCustomerDTO();
        secondCustomerDTO = userController.createUpdateCustomer(secondCustomerDTO);
        CustomerDTO thirdCustomerDTO = createCustomerDTO();
        thirdCustomerDTO = userController.createUpdateCustomer(thirdCustomerDTO);
        petDTO.setOwnerId(secondCustomerDTO.getId());
        petDTO = petController.createUpdatePet(petDTO);
        Assertions.assertEquals(secondCustomerDTO.getId(), petDTO.getOwnerId());
        insertTime = petDTO.getLastUpdateTime();
        updateId = petDTO.getId();
        updateName = "TP" + System.currentTimeMillis();
        petDTO.setName(updateName);
        petDTO.setBreedOrSpecies("Mythological beast");
        petDTO.setBirthDate("2006/12/01");
        petDTO.setType(testPetType);
        petDTO.setOwnerId(thirdCustomerDTO.getId());
        petDTO.setArchived(true);
        petDTO = petController.createUpdatePet(petDTO);
        Assertions.assertEquals(updateId, petDTO.getId());
        Assertions.assertEquals(updateName, petDTO.getName());
        Assertions.assertEquals(testPetType, petDTO.getType());
        Assertions.assertEquals("Mythological beast", petDTO.getBreedOrSpecies());
        Assertions.assertEquals("2006/12/01", petDTO.getBirthDate());
        Assertions.assertEquals(thirdCustomerDTO.getId(), petDTO.getOwnerId());
        Assertions.assertTrue(petDTO.isArchived());
        Assertions.assertTrue(
                insertTime
                        .compareTo(petDTO.getLastUpdateTime()) < 0);
        PetGetDTO petGetDTO = petController.getPet(updateId);
        Assertions.assertEquals(updateId, petGetDTO.getId());
        Assertions.assertEquals(updateName, petDTO.getName());
        Assertions.assertEquals(testPetType, petDTO.getType());
        Assertions.assertEquals("Mythological beast", petGetDTO.getBreedOrSpecies());
        Assertions.assertEquals("2006/12/01", petGetDTO.getBirthDate());
        Assertions.assertTrue(petGetDTO.isArchived());
        Assertions.assertEquals(thirdCustomerDTO.getId(), petDTO.getOwnerId());
        Assertions.assertTrue(
                petGetDTO
                        .getLastUpdateTime()
                        .compareTo(petDTO.getLastUpdateTime()) == 0);
        petDTO.setArchived(false);
        petDTO = petController.createUpdatePet(petDTO);

        //
        // Schedule update
        //

        // Preliminaries

        EmployeeDTO secondEmployeeDTO = createEmployeeDTO();
        secondEmployeeDTO = userController.createUpdateEmployee(secondEmployeeDTO);
        secondEmployeeDTO.setSkills(scheduleUpdateSkills);
        secondEmployeeDTO.setDaysAvailable(scheduleUpdateDaysAvailable);
        secondEmployeeDTO = userController.createUpdateEmployee(secondEmployeeDTO);
        CustomerDTO fourthCustomerDTO = createCustomerDTO();
        fourthCustomerDTO = userController.createUpdateCustomer(fourthCustomerDTO);
        PetDTO secondPetDTO = createPetDTO();
        secondPetDTO.setType("DRAGON");
        secondPetDTO.setOwnerId(fourthCustomerDTO.getId());
        secondPetDTO = petController.createUpdatePet(secondPetDTO);

        List<Long> employeeList = Lists.newArrayList(employeeGetDTO.getId());
        List<Long> updateEmployeeList = Lists.newArrayList(secondEmployeeDTO.getId());
        List<Long> petList = Lists.newArrayList(petDTO.getId());
        List<Long> updatePetList = Lists.newArrayList(secondPetDTO.getId());

        // Create schedule for first pet, first employee, and first activity set

        ScheduleDTO scheduleDTO = createScheduleDTO(
                petList,
                employeeList,
                date,
                updateSkills);
        scheduleDTO.setStartTime(startTime);
        scheduleDTO.setNotes("Original schedule notes.");
        ScheduleGetDTO scheduleGetDTO = scheduleController.createUpdateSchedule(scheduleDTO);
        ScheduleGetDTO initialScheduleGetDTO = scheduleController.getSchedule(scheduleGetDTO.getId());
        Assertions.assertEquals(initialScheduleGetDTO.getId(), scheduleGetDTO.getId());
        Assertions.assertEquals(initialScheduleGetDTO.getActivities(),
                updateSkills);
        Assertions.assertEquals(initialScheduleGetDTO.getDate(),
                date);
        Assertions.assertEquals(initialScheduleGetDTO.getEmployeeIds(),
                employeeList);
        Assertions.assertEquals(initialScheduleGetDTO.getPetIds(),
                petList);
        Assertions.assertEquals(initialScheduleGetDTO.getStartTime(),
                startTime);
        Assertions.assertEquals(initialScheduleGetDTO.getNotes(),
                "Original schedule notes.");
        Assertions.assertEquals(initialScheduleGetDTO.getStatus(),
                "PENDING");
        Assertions.assertNotNull(initialScheduleGetDTO.getServiceCost());

        // Update schedule to second pet, second employee, and second activity set

        ScheduleDTO updatedScheduleDTO = createScheduleDTO(
                updatePetList,
                updateEmployeeList,
                updateDate,
                scheduleUpdateSkills);
        updatedScheduleDTO.setStartTime(updateStartTime);
        updatedScheduleDTO.setNotes("Updated schedule notes.");
        updatedScheduleDTO.setStatus("CANCELLED");
        updatedScheduleDTO.setId(scheduleGetDTO.getId());
        ScheduleGetDTO updatedScheduleGetDTO = scheduleController.createUpdateSchedule(
                updatedScheduleDTO);

        ScheduleGetDTO originalScheduleGetDTO =
                scheduleController.getSchedule(scheduleGetDTO.getId());
        Assertions.assertEquals(originalScheduleGetDTO.getId(), scheduleGetDTO.getId());
        Assertions.assertEquals(originalScheduleGetDTO.getActivities(),
                scheduleUpdateSkills);
        Assertions.assertEquals(originalScheduleGetDTO.getDate(),
                updateDate);
        Assertions.assertEquals(originalScheduleGetDTO.getEmployeeIds(),
                updateEmployeeList);
        Assertions.assertEquals(originalScheduleGetDTO.getPetIds(),
                updatePetList);
        Assertions.assertEquals(originalScheduleGetDTO.getStartTime(),
                updateStartTime);
        Assertions.assertEquals(originalScheduleGetDTO.getNotes(),
                "Updated schedule notes.");
        Assertions.assertEquals(originalScheduleGetDTO.getStatus(),
                "CANCELLED");
        Assertions.assertNotNull(originalScheduleGetDTO.getServiceCost());
    }

    @Test
    @Order(12)
    public void testNotes() {

        // Set up test variables

        List<Long> noteIds = Lists.newArrayList();
        String insertDescription = "Test note description.";
        String insertText = "Test note text.";
        String updateDescription = "Test note description, updated.";
        String updateText = "Test note text, updated.";
        String noteDescription = "Test note description, inserted.";
        String noteText = "Test note text, inserted.";
        String updateNoteDescription = "Test note description, insert updated.";
        String updateNoteText = "Test note text, insert updated.";
        Long insertNoteId;
        Long updateNoteId;
        Long createNoteId;
        LocalDateTime createNoteTime;
        List<Long> allNoteIds;
        List<Note> allNotes;

        //
        // Employee notes
        //

        // Employee creation note

        EmployeeDTO employeeDTO = createEmployeeDTO();
        employeeDTO.setNoteDescription(insertDescription);
        employeeDTO.setNoteText(insertText);
        employeeDTO = userController.createUpdateEmployee(employeeDTO);
        insertNoteId = employeeDTO.getNoteId();
        noteIds.add(insertNoteId);
        Assertions.assertEquals(employeeDTO.getNoteDescription(), insertDescription);
        Assertions.assertEquals(employeeDTO.getNoteText(), insertText);
        EmployeeNoteDTO checkEmployeeNoteDTO =
                userController.getEmployeeNote(employeeDTO.getId(), insertNoteId);
        Assertions.assertEquals(checkEmployeeNoteDTO.getDescription(), insertDescription);
        Assertions.assertEquals(checkEmployeeNoteDTO.getNote(), insertText);

        // Employee update note

        employeeDTO.setNoteDescription(updateDescription);
        employeeDTO.setNoteText(updateText);
        employeeDTO = userController.createUpdateEmployee(employeeDTO);
        updateNoteId = employeeDTO.getNoteId();
        noteIds.add(updateNoteId);
        Assertions.assertEquals(employeeDTO.getNoteDescription(), updateDescription);
        Assertions.assertEquals(employeeDTO.getNoteText(), updateText);
        checkEmployeeNoteDTO =
                userController.getEmployeeNote(employeeDTO.getId(), updateNoteId);
        Assertions.assertEquals(checkEmployeeNoteDTO.getDescription(), updateDescription);
        Assertions.assertEquals(checkEmployeeNoteDTO.getNote(), updateText);

        // Create independent note

        EmployeeNoteDTO employeeNoteDTO = new EmployeeNoteDTO();
        employeeNoteDTO.setDescription(noteDescription);
        employeeNoteDTO.setNote(noteText);
        employeeNoteDTO.setEmployeeId(employeeDTO.getId());
        employeeNoteDTO = userController.createUpdateEmployeeNote(employeeNoteDTO);
        createNoteId = employeeNoteDTO.getId();
        noteIds.add(createNoteId);
        createNoteTime = employeeNoteDTO.getLastUpdateTime();
        Assertions.assertEquals(employeeNoteDTO.getDescription(), noteDescription);
        Assertions.assertEquals(employeeNoteDTO.getNote(), noteText);

        // Update independent note

        employeeNoteDTO.setDescription(updateNoteDescription);
        employeeNoteDTO.setNote(updateNoteText);
        employeeNoteDTO = userController.createUpdateEmployeeNote(employeeNoteDTO);
        Assertions.assertEquals(employeeNoteDTO.getDescription(), updateNoteDescription);
        Assertions.assertEquals(employeeNoteDTO.getNote(), updateNoteText);
        Assertions.assertEquals(employeeNoteDTO.getId(), createNoteId);
        Assertions.assertTrue(
                employeeNoteDTO
                        .getLastUpdateTime()
                        .compareTo(createNoteTime) > 0);
        createNoteTime = employeeNoteDTO.getLastUpdateTime();

        // Verify independent note

        checkEmployeeNoteDTO =
                userController.getEmployeeNote(employeeDTO.getId(), createNoteId);
        Assertions.assertEquals(checkEmployeeNoteDTO.getDescription(), updateNoteDescription);
        Assertions.assertEquals(checkEmployeeNoteDTO.getNote(), updateNoteText);
        Assertions.assertEquals(checkEmployeeNoteDTO.getLastUpdateTime(), createNoteTime);

        // Verify all created note IDs

        allNoteIds = userController.getEmployeeNoteIds(employeeDTO.getId());
        Assertions.assertEquals(noteIds, allNoteIds);

        // Verify all created notes

        allNotes = userController.getEmployeeNotes(employeeDTO.getId());
        matchNote(allNotes.get(0), insertNoteId, insertDescription, insertText);
        matchNote(allNotes.get(1), updateNoteId, updateDescription, updateText);
        matchNote(allNotes.get(2),
                createNoteId,
                updateNoteDescription,
                updateNoteText);

        //
        // Customer notes
        //

        noteIds = Lists.newArrayList();

        // Customer creation note

        CustomerDTO customerDTO = createCustomerDTO();
        customerDTO.setNoteDescription(insertDescription);
        customerDTO.setNoteText(insertText);
        customerDTO = userController.createUpdateCustomer(customerDTO);
        insertNoteId = customerDTO.getNoteId();
        noteIds.add(insertNoteId);
        Assertions.assertEquals(customerDTO.getNoteDescription(), insertDescription);
        Assertions.assertEquals(customerDTO.getNoteText(), insertText);
        CustomerNoteDTO checkCustomerNoteDTO =
                userController.getCustomerNote(customerDTO.getId(), insertNoteId);
        Assertions.assertEquals(checkCustomerNoteDTO.getDescription(), insertDescription);
        Assertions.assertEquals(checkCustomerNoteDTO.getNote(), insertText);

        // Customer update note

        customerDTO.setNoteDescription(updateDescription);
        customerDTO.setNoteText(updateText);
        customerDTO = userController.createUpdateCustomer(customerDTO);
        updateNoteId = customerDTO.getNoteId();
        noteIds.add(updateNoteId);
        Assertions.assertEquals(customerDTO.getNoteDescription(), updateDescription);
        Assertions.assertEquals(customerDTO.getNoteText(), updateText);
        checkCustomerNoteDTO =
                userController.getCustomerNote(customerDTO.getId(), updateNoteId);
        Assertions.assertEquals(checkCustomerNoteDTO.getDescription(), updateDescription);
        Assertions.assertEquals(checkCustomerNoteDTO.getNote(), updateText);

        // Create independent note

        CustomerNoteDTO customerNoteDTO = new CustomerNoteDTO();
        customerNoteDTO.setDescription(noteDescription);
        customerNoteDTO.setNote(noteText);
        customerNoteDTO.setCustomerId(customerDTO.getId());
        customerNoteDTO = userController.createUpdateCustomerNote(customerNoteDTO);
        createNoteId = customerNoteDTO.getId();
        noteIds.add(createNoteId);
        createNoteTime = customerNoteDTO.getLastUpdateTime();
        Assertions.assertEquals(customerNoteDTO.getDescription(), noteDescription);
        Assertions.assertEquals(customerNoteDTO.getNote(), noteText);

        // Update independent note

        customerNoteDTO.setDescription(updateNoteDescription);
        customerNoteDTO.setNote(updateNoteText);
        customerNoteDTO = userController.createUpdateCustomerNote(customerNoteDTO);
        Assertions.assertEquals(customerNoteDTO.getDescription(), updateNoteDescription);
        Assertions.assertEquals(customerNoteDTO.getNote(), updateNoteText);
        Assertions.assertEquals(customerNoteDTO.getId(), createNoteId);
        Assertions.assertTrue(
                customerNoteDTO
                        .getLastUpdateTime()
                        .compareTo(createNoteTime) > 0);
        createNoteTime = customerNoteDTO.getLastUpdateTime();

        // Verify independent note

        checkCustomerNoteDTO =
                userController.getCustomerNote(customerDTO.getId(), createNoteId);
        Assertions.assertEquals(checkCustomerNoteDTO.getDescription(), updateNoteDescription);
        Assertions.assertEquals(checkCustomerNoteDTO.getNote(), updateNoteText);
        Assertions.assertEquals(checkCustomerNoteDTO.getLastUpdateTime(), createNoteTime);

        // Verify all created note IDs

        allNoteIds = userController.getCustomerNoteIds(customerDTO.getId());
        Assertions.assertEquals(noteIds, allNoteIds);

        // Verify all created notes

        allNotes = userController.getCustomerNotes(customerDTO.getId());
        matchNote(allNotes.get(0), insertNoteId, insertDescription, insertText);
        matchNote(allNotes.get(1), updateNoteId, updateDescription, updateText);
        matchNote(allNotes.get(2),
                createNoteId,
                updateNoteDescription,
                updateNoteText);

        //
        // Pet notes
        //

        noteIds = Lists.newArrayList();

        // Pet creation note

        PetDTO petDTO = createPetDTO();
        petDTO.setNoteDescription(insertDescription);
        petDTO.setNoteText(insertText);
        petDTO.setOwnerId(customerDTO.getId());
        petDTO = petController.createUpdatePet(petDTO);
        insertNoteId = petDTO.getNoteId();
        noteIds.add(insertNoteId);
        Assertions.assertEquals(petDTO.getNoteDescription(), insertDescription);
        Assertions.assertEquals(petDTO.getNoteText(), insertText);
        PetNoteDTO checkPetNoteDTO =
                petController.getPetNote(petDTO.getId(), insertNoteId);
        Assertions.assertEquals(checkPetNoteDTO.getDescription(), insertDescription);
        Assertions.assertEquals(checkPetNoteDTO.getNote(), insertText);

        // Pet update note

        petDTO.setNoteDescription(updateDescription);
        petDTO.setNoteText(updateText);
        petDTO = petController.createUpdatePet(petDTO);
        updateNoteId = petDTO.getNoteId();
        noteIds.add(updateNoteId);
        Assertions.assertEquals(petDTO.getNoteDescription(), updateDescription);
        Assertions.assertEquals(petDTO.getNoteText(), updateText);
        checkPetNoteDTO =
                petController.getPetNote(petDTO.getId(), updateNoteId);
        Assertions.assertEquals(checkPetNoteDTO.getDescription(), updateDescription);
        Assertions.assertEquals(checkPetNoteDTO.getNote(), updateText);

        // Create independent note

        PetNoteDTO petNoteDTO = new PetNoteDTO();
        petNoteDTO.setDescription(noteDescription);
        petNoteDTO.setNote(noteText);
        petNoteDTO.setPetId(petDTO.getId());
        petNoteDTO = petController.createUpdatePetNote(petNoteDTO);
        createNoteId = petNoteDTO.getId();
        noteIds.add(createNoteId);
        createNoteTime = petNoteDTO.getLastUpdateTime();
        Assertions.assertEquals(petNoteDTO.getDescription(), noteDescription);
        Assertions.assertEquals(petNoteDTO.getNote(), noteText);

        // Update independent note

        petNoteDTO.setDescription(updateNoteDescription);
        petNoteDTO.setNote(updateNoteText);
        petNoteDTO = petController.createUpdatePetNote(petNoteDTO);
        Assertions.assertEquals(petNoteDTO.getDescription(), updateNoteDescription);
        Assertions.assertEquals(petNoteDTO.getNote(), updateNoteText);
        Assertions.assertEquals(petNoteDTO.getId(), createNoteId);
        Assertions.assertTrue(
                petNoteDTO
                        .getLastUpdateTime()
                        .compareTo(createNoteTime) > 0);
        createNoteTime = petNoteDTO.getLastUpdateTime();

        // Verify independent note

        checkPetNoteDTO =
                petController.getPetNote(petDTO.getId(), createNoteId);
        Assertions.assertEquals(checkPetNoteDTO.getDescription(), updateNoteDescription);
        Assertions.assertEquals(checkPetNoteDTO.getNote(), updateNoteText);
        Assertions.assertEquals(checkPetNoteDTO.getLastUpdateTime(), createNoteTime);

        // Verify all created note IDs

        allNoteIds = petController.getPetNoteIds(petDTO.getId());
        Assertions.assertEquals(noteIds, allNoteIds);

        // Verify all created notes

        allNotes = petController.getPetNotes(petDTO.getId());
        matchNote(allNotes.get(0), insertNoteId, insertDescription, insertText);
        matchNote(allNotes.get(1), updateNoteId, updateDescription, updateText);
        matchNote(allNotes.get(2),
                createNoteId,
                updateNoteDescription,
                updateNoteText);
    }

    @Test
    @Order(13)
    public void testArchiving() {

        CustomApiInvalidParameterException eInvalid = null;

        //
        // Employee archiving
        //

        EmployeeDTO employeeDTO = createEmployeeDTO();
        employeeDTO.setArchived(true);
        employeeDTO = userController.createUpdateEmployee(employeeDTO);

        // Verify no changes allowed for archived employees

        EmployeeDTO finalEmployeeDTO = employeeDTO;
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    userController.createUpdateEmployee(finalEmployeeDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "employee",
                "archived",
                "true",
                "Employee entry is archived; no changes allowed.");
        employeeDTO.setArchived(false);
        employeeDTO = userController.createUpdateEmployee(finalEmployeeDTO);

        //
        // Customer archiving
        //

        CustomerDTO customerDTO = createCustomerDTO();
        customerDTO.setArchived(true);
        customerDTO = userController.createUpdateCustomer(customerDTO);

        // Verify no changes allowed for archived customers

        CustomerDTO finalCustomerDTO = customerDTO;
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    userController.createUpdateCustomer(finalCustomerDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "customer",
                "archived",
                "true",
                "Customer entry is archived; no changes allowed.");
        customerDTO.setArchived(false);
        customerDTO = userController.createUpdateCustomer(finalCustomerDTO);

        //
        // Pet archiving
        //

        PetDTO petDTO = createPetDTO();
        petDTO.setOwnerId(customerDTO.getId());
        petDTO.setArchived(true);
        petDTO = petController.createUpdatePet(petDTO);

        // Verify no changes allowed for archived pets

        PetDTO finalPetDTO = petDTO;
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    petController.createUpdatePet(finalPetDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "pet",
                "archived",
                "true",
                "Pet entry is archived; no changes allowed.");
        petDTO.setArchived(false);
        petDTO = petController.createUpdatePet(finalPetDTO);
    }


    @Test
    @Order(14)
    public void testAdvancedSchedulingBadRequest() {

        // Create initial testing data

        CustomApiInvalidParameterException eInvalid = null;

        OfficeSchedule officeSchedule = officeScheduleService.findAll().get(0);
        DayOfWeek dayOfWeek = DayOfWeek.valueOf(officeSchedule.getDayOfWeek());
        List<String> daysAvailable = Lists.newArrayList(dayOfWeek.toString());
        LocalDate appointmentDate = LocalDate.now();
        appointmentDate = appointmentDate.with(TemporalAdjusters.next(dayOfWeek));
        String date = appointmentDate.toString().replace("-","/");
        String startTime = officeSchedule.getOfficeOpens().toString();
        List<String> activities = Lists.newArrayList("WALKING", "AGILITY");

        EmployeeDTO firstEmployeeDTO = createEmployeeDTO();
        firstEmployeeDTO = userController.createUpdateEmployee(firstEmployeeDTO);
        firstEmployeeDTO.setSkills(activities);
        firstEmployeeDTO.setDaysAvailable(daysAvailable);
        firstEmployeeDTO = userController.createUpdateEmployee(firstEmployeeDTO);
        EmployeeDTO secondEmployeeDTO = createEmployeeDTO();
        secondEmployeeDTO = userController.createUpdateEmployee(secondEmployeeDTO);
        secondEmployeeDTO.setSkills(activities);
        secondEmployeeDTO.setDaysAvailable(daysAvailable);
        secondEmployeeDTO = userController.createUpdateEmployee(secondEmployeeDTO);
        List<Long> employeeList = Lists.newArrayList(
                firstEmployeeDTO.getId(),
                secondEmployeeDTO.getId());

        CustomerDTO firstCustomerDTO = createCustomerDTO();
        firstCustomerDTO = userController.createUpdateCustomer(firstCustomerDTO);
        CustomerDTO secondCustomerDTO = createCustomerDTO();
        secondCustomerDTO = userController.createUpdateCustomer(secondCustomerDTO);

        PetDTO firstPetDTO = createPetDTO();
        firstPetDTO.setOwnerId(firstCustomerDTO.getId());
        firstPetDTO = petController.createUpdatePet(firstPetDTO);
        PetDTO secondPetDTO = createPetDTO();
        secondPetDTO.setOwnerId(firstCustomerDTO.getId());
        secondPetDTO = petController.createUpdatePet(secondPetDTO);
        List<Long> petList = Lists.newArrayList(
                firstPetDTO.getId(),
                secondPetDTO.getId());

        ScheduleDTO scheduleDTO = createScheduleDTO(petList, employeeList, date, activities);
        scheduleDTO.setStartTime(startTime);

        //
        // Scheduling for archived entities
        //

        // Disallow scheduling for archived employee

        firstEmployeeDTO.setArchived(true);
        firstEmployeeDTO = userController.createUpdateEmployee(firstEmployeeDTO);
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    scheduleController.createUpdateSchedule(scheduleDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "schedule",
                "employee",
                String.valueOf(firstEmployeeDTO.getId()),
                "Requested employee is archived and cannot be scheduled.");
        firstEmployeeDTO.setArchived(false);
        firstEmployeeDTO = userController.createUpdateEmployee(firstEmployeeDTO);

        // Disallow scheduling for archived pet customer

        firstCustomerDTO.setArchived(true);
        firstCustomerDTO = userController.createUpdateCustomer(firstCustomerDTO);
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    scheduleController.createUpdateSchedule(scheduleDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "schedule",
                "customer",
                String.valueOf(firstCustomerDTO.getId()) +
                        " : " +
                String.valueOf(firstPetDTO.getId()),
                "Owner of requested pet is archived and cannot be scheduled.");
        firstCustomerDTO.setArchived(false);
        firstCustomerDTO = userController.createUpdateCustomer(firstCustomerDTO);

        // Disallow scheduling for archived pet

        firstPetDTO.setArchived(true);
        firstPetDTO = petController.createUpdatePet(firstPetDTO);
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    scheduleController.createUpdateSchedule(scheduleDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "schedule",
                "pet",
                String.valueOf(firstPetDTO.getId()),
                "Requested pet is archived and cannot be scheduled.");
        firstPetDTO.setArchived(false);
        firstPetDTO = petController.createUpdatePet(firstPetDTO);

        // Create valid appointment

        ScheduleGetDTO scheduleGetDTO = scheduleController.createUpdateSchedule(scheduleDTO);

        //
        // Employee updates
        //

        // Disallow skill set removal when needed for pending appointment

        List<String> newSkills = Lists.newArrayList("FEEDING");
        firstEmployeeDTO.setSkills(newSkills);
        EmployeeDTO finalFirstEmployeeDTO = firstEmployeeDTO;
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    userController.createUpdateEmployee(finalFirstEmployeeDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "employee",
                "skills",
                "WALKING",
                "Employee skill cannot be removed due to " +
                        "being required for pending schedule.");
        matchFieldError(eInvalid.getFieldErrors().get(1),
                "employee",
                "skills",
                "AGILITY",
                "Employee skill cannot be removed due to " +
                        "being required for pending schedule.");
        firstEmployeeDTO.setSkills(activities);

        //
        // Pet updates
        //

        // Disallow pet type update with pending appointment

        String oldPetType = firstPetDTO.getType();
        firstPetDTO.setType("GRYPHON");
        PetDTO finalFirstPetDTO = firstPetDTO;
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    petController.createUpdatePet(finalFirstPetDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "pet",
                "type",
                null,
                "Pet type cannot be changed due to pending schedule.");
        firstPetDTO.setType(oldPetType);

        // Disallow pet customer update with pending appointment

        firstPetDTO.setOwnerId(secondCustomerDTO.getId());
        PetDTO secondFinalFirstPetDTO = firstPetDTO;
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    petController.createUpdatePet(secondFinalFirstPetDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "pet",
                "customer",
                null,
                "Pet owner cannot be changed due to pending schedule.");
        firstPetDTO.setOwnerId(firstCustomerDTO.getId());

        //
        // Archiving with pending appointment
        //

        // Disallow employee archiving with pending appointment

        firstEmployeeDTO.setArchived(true);
        EmployeeDTO secondFinalFirstEmployeeDTO = firstEmployeeDTO;
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    userController.createUpdateEmployee(secondFinalFirstEmployeeDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "employee",
                "archived",
                null,
                "Employee cannot be archived due to pending schedule.");
        firstEmployeeDTO.setArchived(false);

        // Disallow customer archiving with pending pet appointment

        firstCustomerDTO.setArchived(true);
        CustomerDTO finalFirstCustomerDTO = firstCustomerDTO;
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    userController.createUpdateCustomer(finalFirstCustomerDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(1),
                "customer",
                "archived",
                String.valueOf(firstPetDTO.getId()),
                "Customer cannot be archived due to pending pet schedule.");
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "customer",
                "archived",
                String.valueOf(secondPetDTO.getId()),
                "Customer cannot be archived due to pending pet schedule.");
        firstCustomerDTO.setArchived(false);

        // Disallow pet archiving with pending appointment

        firstPetDTO.setArchived(true);
        PetDTO thirdFinalFirstPetDTO = firstPetDTO;
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    petController.createUpdatePet(thirdFinalFirstPetDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "pet",
                "archived",
                null,
                "Pet cannot be archived due to pending schedule.");
        firstPetDTO.setArchived(false);

        //
        // Schedule update
        //

        // No updates to schedule once schedule is cancelled/complete

        scheduleGetDTO.setStatus("COMPLETE");
        scheduleGetDTO = scheduleController.createUpdateSchedule(scheduleGetDTO);
        ScheduleGetDTO finalScheduleGetDTO = scheduleGetDTO;
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    scheduleController.createUpdateSchedule(finalScheduleGetDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "schedule",
                "status",
                null,
                "Appointment has been completed; no changes allowed.");
        scheduleGetDTO = scheduleController.createUpdateSchedule(scheduleDTO);
        scheduleGetDTO.setStatus("CANCELLED");
        scheduleGetDTO = scheduleController.createUpdateSchedule(scheduleGetDTO);
        ScheduleGetDTO secondFinalScheduleGetDTO = scheduleGetDTO;
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    scheduleController.createUpdateSchedule(secondFinalScheduleGetDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "schedule",
                "status",
                null,
                "Appointment has been cancelled; no changes allowed.");
    }

    @Test
    @Order(15)
    public void testScheduleSearch() {

        // Save IDs for all employees and schedules created here
        // Skip all returned results that don't match the saved IDs

        // Preliminaries

        OfficeSchedule officeSchedule = officeScheduleService.findAll().get(0);
        DayOfWeek dayOfWeek = DayOfWeek.valueOf(officeSchedule.getDayOfWeek());
        LocalDate appointmentDayOfWeek = LocalDate.now();
        appointmentDayOfWeek = appointmentDayOfWeek.with(TemporalAdjusters.next(dayOfWeek));
        LocalDate firstDate = appointmentDayOfWeek.plusDays(7);
        String firstAppointmentDate = firstDate.toString().replace("-","/");
        LocalDate secondDate = appointmentDayOfWeek.plusDays(14);
        String secondAppointmentDate = secondDate.toString().replace("-","/");
        String earlyAppointmentStartTime = officeSchedule.getOfficeOpens().toString();
        String lateAppointmentStartTime = officeSchedule
                .getOfficeOpens()
                .plusMinutes(60)
                .toString();
        List<String> daysAvailable = Lists.newArrayList(dayOfWeek.toString());

        List<String> skills = Lists.newArrayList("WALKING");

        HashMap<Long, Long> scheduleIds = new HashMap<Long, Long>();
        HashMap<Long, ScheduleGetDTO> schedules = new HashMap<Long, ScheduleGetDTO>();

        // Create test employees, customer, and pets

        EmployeeDTO firstEmployeeDTO = createDetailedEmployee(skills, daysAvailable);
        EmployeeDTO secondEmployeeDTO = createDetailedEmployee(skills, daysAvailable);
        CustomerDTO customerDTO = createCustomerDTO();
        customerDTO = userController.createUpdateCustomer(customerDTO);
        PetDTO firstPetDTO = createDetailedPet(customerDTO.getId());
        PetDTO secondPetDTO = createDetailedPet(customerDTO.getId());

        List<Long> firstEmployeeList = Lists.newArrayList(firstEmployeeDTO.getId());
        List<Long> secondEmployeeList = Lists.newArrayList(secondEmployeeDTO.getId());
        List<Long> firstPetList = Lists.newArrayList(firstPetDTO.getId());
        List<Long> secondPetList = Lists.newArrayList(secondPetDTO.getId());

        // Create eight schedules, with the following specifications:
        //
        // Four for each employee
        // Two dates on successive Saturdays, with four appointments on each date
        // Four appointments for each pet, two apiece on each date
        // First pet appointments all marked PENDING
        // Second pet appointments all marked COMPLETE
        // Half of each employee appointments marked PENDING, the other half
        // marked COMPLETE
        //
        // Each pet has an appointment at the same time with different employees,
        // two appointment times for each date

        // Schedule 1
        ScheduleDTO schedule1DTO = createScheduleDTO(
                firstPetList,
                firstEmployeeList,
                firstAppointmentDate,
                skills);
        schedule1DTO.setStartTime(earlyAppointmentStartTime);
        ScheduleGetDTO schedule1GetDTO = scheduleController.createUpdateSchedule(schedule1DTO);
        scheduleIds.put(schedule1GetDTO.getId(), 1L);
        schedules.put(1L, schedule1GetDTO);

        // Schedule 2
        ScheduleDTO schedule2DTO = createScheduleDTO(
                secondPetList,
                secondEmployeeList,
                firstAppointmentDate,
                skills);
        schedule2DTO.setStartTime(earlyAppointmentStartTime);
        schedule2DTO.setStatus("COMPLETE");
        ScheduleGetDTO schedule2GetDTO = scheduleController.createUpdateSchedule(schedule2DTO);
        scheduleIds.put(schedule2GetDTO.getId(), 2L);
        schedules.put(2L, schedule2GetDTO);

        // Schedule 3
        ScheduleDTO schedule3DTO = createScheduleDTO(
                secondPetList,
                firstEmployeeList,
                firstAppointmentDate,
                skills);
        schedule3DTO.setStartTime(lateAppointmentStartTime);
        schedule3DTO.setStatus("COMPLETE");
        ScheduleGetDTO schedule3GetDTO = scheduleController.createUpdateSchedule(schedule3DTO);
        scheduleIds.put(schedule3GetDTO.getId(), 3L);
        schedules.put(3L, schedule3GetDTO);

        // Schedule 4
        ScheduleDTO schedule4DTO = createScheduleDTO(
                firstPetList,
                secondEmployeeList,
                firstAppointmentDate,
                skills);
        schedule4DTO.setStartTime(lateAppointmentStartTime);
        ScheduleGetDTO schedule4GetDTO = scheduleController.createUpdateSchedule(schedule4DTO);
        scheduleIds.put(schedule4GetDTO.getId(), 4L);
        schedules.put(4L, schedule4GetDTO);

        // Schedule 5
        ScheduleDTO schedule5DTO = createScheduleDTO(
                firstPetList,
                firstEmployeeList,
                secondAppointmentDate,
                skills);
        schedule5DTO.setStartTime(earlyAppointmentStartTime);
        ScheduleGetDTO schedule5GetDTO = scheduleController.createUpdateSchedule(schedule5DTO);
        scheduleIds.put(schedule5GetDTO.getId(), 5L);
        schedules.put(5L, schedule5GetDTO);

        // Schedule 6
        ScheduleDTO schedule6DTO = createScheduleDTO(
                secondPetList,
                secondEmployeeList,
                secondAppointmentDate,
                skills);
        schedule6DTO.setStartTime(earlyAppointmentStartTime);
        schedule6DTO.setStatus("COMPLETE");
        ScheduleGetDTO schedule6GetDTO = scheduleController.createUpdateSchedule(schedule6DTO);
        scheduleIds.put(schedule6GetDTO.getId(), 6L);
        schedules.put(6L, schedule6GetDTO);

        // Schedule 7
        ScheduleDTO schedule7DTO = createScheduleDTO(
                secondPetList,
                firstEmployeeList,
                secondAppointmentDate,
                skills);
        schedule7DTO.setStartTime(lateAppointmentStartTime);
        schedule7DTO.setStatus("COMPLETE");
        ScheduleGetDTO schedule7GetDTO = scheduleController.createUpdateSchedule(schedule7DTO);
        scheduleIds.put(schedule7GetDTO.getId(), 7L);
        schedules.put(7L, schedule7GetDTO);

        // Schedule 8
        ScheduleDTO schedule8DTO = createScheduleDTO(
                firstPetList,
                secondEmployeeList,
                secondAppointmentDate,
                skills);
        schedule8DTO.setStartTime(lateAppointmentStartTime);
        ScheduleGetDTO schedule8GetDTO = scheduleController.createUpdateSchedule(schedule8DTO);
        scheduleIds.put(schedule8GetDTO.getId(), 8L);
        schedules.put(8L, schedule8GetDTO);

        // Conduct a series of searches based on combinations of:
        // Employee
        // Date
        // Appointment status
        //
        // It does not matter the employee, date, or status selected, as there
        // are two of each so the result count will be the same.

        // 1. Search for all appointments (8 results)

        ScheduleQueryDTO scheduleQueryDTO = new ScheduleQueryDTO();
        conductSearch(scheduleQueryDTO, scheduleIds, schedules, 8);

        // 2. Search by employee (4 results)

        scheduleQueryDTO = new ScheduleQueryDTO();
        scheduleQueryDTO.setEmployeeId(secondEmployeeDTO.getId());
        conductSearch(scheduleQueryDTO, scheduleIds, schedules, 4);

        // 3. Search by status (4 results)

        scheduleQueryDTO = new ScheduleQueryDTO();
        scheduleQueryDTO.setStatus("COMPLETE");
        conductSearch(scheduleQueryDTO, scheduleIds, schedules, 4);

        // 4. Search by date (4 results)

        scheduleQueryDTO = new ScheduleQueryDTO();
        scheduleQueryDTO.setDate(firstAppointmentDate);
        conductSearch(scheduleQueryDTO, scheduleIds, schedules, 4);

        // 5. Search by employee and status (2 results)

        scheduleQueryDTO = new ScheduleQueryDTO();
        scheduleQueryDTO.setStatus("PENDING");
        scheduleQueryDTO.setEmployeeId(firstEmployeeDTO.getId());
        conductSearch(scheduleQueryDTO, scheduleIds, schedules, 2);

        // 6. Search by employee and date (2 results)

        scheduleQueryDTO = new ScheduleQueryDTO();
        scheduleQueryDTO.setDate(secondAppointmentDate);
        scheduleQueryDTO.setEmployeeId(secondEmployeeDTO.getId());
        conductSearch(scheduleQueryDTO, scheduleIds, schedules, 2);

        // 7. Search by status and date (2 results)

        scheduleQueryDTO = new ScheduleQueryDTO();
        scheduleQueryDTO.setDate(firstAppointmentDate);
        scheduleQueryDTO.setStatus("COMPLETE");
        conductSearch(scheduleQueryDTO, scheduleIds, schedules, 2);

        // 8. Search by employee, status, and date (1 result)

        scheduleQueryDTO = new ScheduleQueryDTO();
        scheduleQueryDTO.setDate(firstAppointmentDate);
        scheduleQueryDTO.setStatus("COMPLETE");
        scheduleQueryDTO.setEmployeeId(firstEmployeeDTO.getId());
        conductSearch(scheduleQueryDTO, scheduleIds, schedules, 1);

        // 9. Search by pet (4 results)

        scheduleQueryDTO = new ScheduleQueryDTO();
        scheduleQueryDTO.setPetId(firstPetDTO.getId());
        conductSearch(scheduleQueryDTO, scheduleIds, schedules, 4);

        // 10. Search by pet and date (2 results)

        scheduleQueryDTO = new ScheduleQueryDTO();
        scheduleQueryDTO.setPetId(secondPetDTO.getId());
        scheduleQueryDTO.setDate(secondAppointmentDate);
        conductSearch(scheduleQueryDTO, scheduleIds, schedules, 2);

        // 11. Search by pet and status (4 results for open appointments)

        scheduleQueryDTO = new ScheduleQueryDTO();
        scheduleQueryDTO.setPetId(firstPetDTO.getId());
        scheduleQueryDTO.setStatus("PENDING");
        conductSearch(scheduleQueryDTO, scheduleIds, schedules, 4);

        // 12. Search by pet and status (0 results for no open appointments)

        scheduleQueryDTO = new ScheduleQueryDTO();
        scheduleQueryDTO.setPetId(secondPetDTO.getId());
        scheduleQueryDTO.setStatus("PENDING");
        conductSearch(scheduleQueryDTO, scheduleIds, schedules, 0);

        // 13. Search by pet and employee (2 results)

        scheduleQueryDTO = new ScheduleQueryDTO();
        scheduleQueryDTO.setPetId(firstPetDTO.getId());
        scheduleQueryDTO.setEmployeeId(secondEmployeeDTO.getId());
        conductSearch(scheduleQueryDTO, scheduleIds, schedules, 2);

        // 14. Search by pet, status, and date (2 results for closed appointments)

        scheduleQueryDTO = new ScheduleQueryDTO();
        scheduleQueryDTO.setPetId(secondPetDTO.getId());
        scheduleQueryDTO.setStatus("COMPLETE");
        scheduleQueryDTO.setDate(secondAppointmentDate);
        conductSearch(scheduleQueryDTO, scheduleIds, schedules, 2);

        // 15. Search by pet, status, and date (0 results for closed appointments)

        scheduleQueryDTO = new ScheduleQueryDTO();
        scheduleQueryDTO.setPetId(firstPetDTO.getId());
        scheduleQueryDTO.setStatus("COMPLETE");
        scheduleQueryDTO.setDate(firstAppointmentDate);
        conductSearch(scheduleQueryDTO, scheduleIds, schedules, 0);

        // 16. Search by customer (8 results)

        scheduleQueryDTO = new ScheduleQueryDTO();
        scheduleQueryDTO.setCustomerId(customerDTO.getId());
        conductSearch(scheduleQueryDTO, scheduleIds, schedules, 8);

        // 17. Search by customer and pet (4 results)

        scheduleQueryDTO = new ScheduleQueryDTO();
        scheduleQueryDTO.setCustomerId(customerDTO.getId());
        scheduleQueryDTO.setPetId(secondPetDTO.getId());
        conductSearch(scheduleQueryDTO, scheduleIds, schedules, 4);

        // 18. Search by customer and date (4 results)

        scheduleQueryDTO = new ScheduleQueryDTO();
        scheduleQueryDTO.setCustomerId(customerDTO.getId());
        scheduleQueryDTO.setDate(secondAppointmentDate);
        conductSearch(scheduleQueryDTO, scheduleIds, schedules, 4);

        // 19. Search by customer and status (4 results)

        scheduleQueryDTO = new ScheduleQueryDTO();
        scheduleQueryDTO.setCustomerId(customerDTO.getId());
        scheduleQueryDTO.setStatus("PENDING");
        conductSearch(scheduleQueryDTO, scheduleIds, schedules, 4);

        // 20. Search by customer, date, and status (2 results)

        scheduleQueryDTO = new ScheduleQueryDTO();
        scheduleQueryDTO.setCustomerId(customerDTO.getId());
        scheduleQueryDTO.setDate(secondAppointmentDate);
        scheduleQueryDTO.setStatus("COMPLETE");
        conductSearch(scheduleQueryDTO, scheduleIds, schedules, 2);

        // Date sorting

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(dateVerification.localDateFormat());
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(dateVerification.localTimeFormat());
        List<ScheduleGetDTO> allSchedules = null;
        LocalDateTime lastDateTime = null;

        // 21. Descending date sort

        scheduleQueryDTO = new ScheduleQueryDTO();
        scheduleQueryDTO.setDateTimeOrder("DESC");
        scheduleQueryDTO.setCustomerId(customerDTO.getId());
        allSchedules = scheduleController.getAllSchedules(scheduleQueryDTO);
        lastDateTime = null;
        for (ScheduleGetDTO scheduleGetDTO : allSchedules) {
            LocalDate date = LocalDate.parse(scheduleGetDTO.getDate(), dateFormatter);
            LocalTime startTime = LocalTime.parse(scheduleGetDTO.getStartTime(), timeFormatter);
            LocalDateTime scheduleDateTime = LocalDateTime.of(date, startTime);
            if (lastDateTime != null) {
                Assertions.assertTrue( scheduleDateTime.compareTo(lastDateTime) <= 0);
            }
            lastDateTime = scheduleDateTime;
        }

        // 22. Ascending date sort

        scheduleQueryDTO = new ScheduleQueryDTO();
        scheduleQueryDTO.setDateTimeOrder("ASC");
        scheduleQueryDTO.setCustomerId(customerDTO.getId());
        allSchedules = scheduleController.getAllSchedules(scheduleQueryDTO);
        lastDateTime = null;
        ScheduleGetDTO firstScheduleGetDTO = null;
        ScheduleGetDTO lastScheduleGetDTO = null;
        for (ScheduleGetDTO scheduleGetDTO : allSchedules) {
            if (firstScheduleGetDTO == null) {
                firstScheduleGetDTO = scheduleGetDTO;
            }
            LocalDate date = LocalDate.parse(scheduleGetDTO.getDate(), dateFormatter);
            LocalTime startTime = LocalTime.parse(scheduleGetDTO.getStartTime(), timeFormatter);
            LocalDateTime scheduleDateTime = LocalDateTime.of(date, startTime);
            if (lastDateTime != null) {
                Assertions.assertTrue( scheduleDateTime.compareTo(lastDateTime) >= 0);
            }
            lastDateTime = scheduleDateTime;
            lastScheduleGetDTO = scheduleGetDTO;
        }

        // Pagination

        scheduleQueryDTO.setLimit(1);
        scheduleQueryDTO.setOffset(0);
        allSchedules = scheduleController.getAllSchedules(scheduleQueryDTO);
        int returnedSchedules = 0;
        for (ScheduleGetDTO scheduleGetDTO : allSchedules) {
            Assertions.assertEquals(firstScheduleGetDTO.getDate(), scheduleGetDTO.getDate());
            Assertions.assertEquals(firstScheduleGetDTO.getStartTime(), scheduleGetDTO.getStartTime());
            returnedSchedules++;
        }
        Assertions.assertEquals(returnedSchedules, 1);
        
        scheduleQueryDTO.setLimit(1);
        scheduleQueryDTO.setOffset(7);
        allSchedules = scheduleController.getAllSchedules(scheduleQueryDTO);
        returnedSchedules = 0;
        for (ScheduleGetDTO scheduleGetDTO : allSchedules) {
            Assertions.assertEquals(lastScheduleGetDTO.getDate(), scheduleGetDTO.getDate());
            Assertions.assertEquals(lastScheduleGetDTO.getStartTime(), scheduleGetDTO.getStartTime());
            returnedSchedules++;
        }
        Assertions.assertEquals(returnedSchedules, 1);
    }

    @Test
    @Order(16)
    public void testBadScheduleSearch() {

        CustomApiInvalidParameterException eInvalid = null;

        ScheduleQueryDTO scheduleQueryDTO = new ScheduleQueryDTO();
        scheduleQueryDTO.setCustomerId(1000L);
        scheduleQueryDTO.setEmployeeId(1000L);
        scheduleQueryDTO.setPetId(1000L);
        scheduleQueryDTO.setStatus("FOOBAR");
        scheduleQueryDTO.setDate("BADDATE");
        scheduleQueryDTO.setDateTimeOrder("BADSETTING");
        scheduleQueryDTO.setLimit(-1);
        scheduleQueryDTO.setOffset(-1);
        ScheduleQueryDTO finalScheduleQueryDTO = scheduleQueryDTO;
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    scheduleController.getAllSchedules(finalScheduleQueryDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "customer",
                "id",
                String.valueOf(1000L),
                "Unknown customer ID.");
        matchFieldError(eInvalid.getFieldErrors().get(1),
                "schedule",
                "date",
                "BADDATE",
                "Invalid date; correct format " +
                        dateVerification.localDateFormat() + ".");
        matchFieldError(eInvalid.getFieldErrors().get(2),
                "employee",
                "id",
                String.valueOf(1000L),
                "Unknown employee ID.");
        matchFieldError(eInvalid.getFieldErrors().get(3),
                "pet",
                "id",
                String.valueOf(1000L),
                "Unknown pet ID.");
        matchFieldError(eInvalid.getFieldErrors().get(4),
                "schedule",
                "status",
                "FOOBAR",
                "Invalid status.");
        matchFieldError(eInvalid.getFieldErrors().get(5),
                "schedule",
                "dateTimeOrder",
                String.valueOf("BADSETTING"),
                "If specified, date/time order must be \"ASC\" or \"DESC\".");
        matchFieldError(eInvalid.getFieldErrors().get(6),
                "schedule",
                "limit",
                String.valueOf("-1"),
                "If specified, limit must be between 1 and 100.");
        matchFieldError(eInvalid.getFieldErrors().get(7),
                "schedule",
                "offset",
                String.valueOf("-1"),
                "Offset cannot be a negative number.");

        scheduleQueryDTO = new ScheduleQueryDTO();
        scheduleQueryDTO.setLimit(100);
        scheduleQueryDTO.setOffset(5);
        ScheduleQueryDTO secondFinalScheduleQueryDTO = scheduleQueryDTO;
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    scheduleController.getAllSchedules(secondFinalScheduleQueryDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "schedule",
                "offset",
                "Offset: 5, Limit: 100",
                "Offset must be zero or a multiple of limit.");

        // Bad schedule availability query

        ScheduleAvailabilityDTO scheduleAvailabilityDTO =
                new ScheduleAvailabilityDTO();
        scheduleAvailabilityDTO.setDate(null);
        scheduleAvailabilityDTO.setActivities(null);
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    scheduleController.getScheduleAvailability(scheduleAvailabilityDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "scheduleAvailabilityQuery",
                "activities",
                null,
                "List of activities required.");
        matchFieldError(eInvalid.getFieldErrors().get(1),
                "scheduleAvailabilityQuery",
                "date",
                null,
                "Date required.");
        String searchDate = LocalDate.now().minusDays(1).toString();
        scheduleAvailabilityDTO.setDate(searchDate);
        List<String> badActivitySet = Lists.newArrayList("FOOBAR");
        scheduleAvailabilityDTO.setActivities(badActivitySet);
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    scheduleController.getScheduleAvailability(scheduleAvailabilityDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "schedule",
                "activities",
                "FOOBAR",
                "Unknown activity.");
        matchFieldError(eInvalid.getFieldErrors().get(1),
                "schedule",
                "date",
                searchDate,
                "Invalid date; correct format " +
                        dateVerification.localDateFormat() + ".");
    }

    @Test
    @Order(17)
    public void testAdvancedScheduling() throws Exception {

        // A series of appointments created for four very busy pets over the course
        // of one day.
        // Schedule details may invalidate some tests, especially if the working day is too short

        // Preliminaries

        CustomApiInvalidParameterException eInvalid = null;

        ScheduleDTO scheduleDTO = null;
        ScheduleDTO secondScheduleDTO = null;
        ScheduleGetDTO scheduleGetDTO = null;
        ScheduleGetDTO secondScheduleGetDTO = null;
        List<Long> employeeList = Lists.newArrayList();
        List<Long> petList = Lists.newArrayList();
        List<String> appointmentActivities = Lists.newArrayList();;
        PetType petType = petTypeService.findByName(testPetType);
        String firstActivityName = "WALKING"; // concurrent == true; concurrent time
        Activity firstActivity = activityService.findByName(firstActivityName);
        PetActivityType firstPetActivityType =
                petActivityTypeService.findByPetTypeAndActivity(petType, firstActivity);
        String secondActivityName = "PETTING"; // concurrent == false; additive time
        Activity secondActivity = activityService.findByName(firstActivityName);
        PetActivityType secondPetActivityType =
                petActivityTypeService.findByPetTypeAndActivity(petType, secondActivity);
        Integer minutesNeeded = firstPetActivityType.getMinutes() +
                secondPetActivityType.getMinutes() * 2;

        // Define skills, employee days available, and date for scheduling

        List<Activity> allActivities = activityService.findAll();
        List<String> employeeSkills = allActivities.stream()
                .map(Activity::getName)
                .collect(Collectors.toList());
        List<OfficeSchedule> allOfficeDays = officeScheduleService.findAll();
        OfficeSchedule officeSchedule = allOfficeDays.get(0);
        OfficeSchedule lunchOfficeSchedule = null;
        for (OfficeSchedule searchOfficeSchedule : allOfficeDays) {
            if (searchOfficeSchedule.getLunchHourRangeStart().toString() != "00:00" &&
            searchOfficeSchedule.getLunchHourRangeEnd().toString() != "00:00") {
                lunchOfficeSchedule = searchOfficeSchedule;
                break;
            }
        }
        if (lunchOfficeSchedule != null) {
            officeSchedule = lunchOfficeSchedule;
        }
        if (officeSchedule == null) {
            throw new Exception("Cannot proceed with scheduling tests due to missing schedule.");
        }

        List<String> employeeDaysAvailable = Lists.newArrayList(officeSchedule.getDayOfWeek());
        LocalDate appointmentDayOfWeek = LocalDate.now();
        appointmentDayOfWeek = appointmentDayOfWeek.with(
                TemporalAdjusters.next(DayOfWeek.valueOf(officeSchedule.getDayOfWeek())));
        LocalDate appointmentDateObject = appointmentDayOfWeek.plusDays(56);
        String appointmentDate = appointmentDateObject.toString().replace("-","/");

        // Create 4 employees and 2 customers with 2 pets each

        // Employees

        EmployeeDTO firstEmployeeDTO = createDetailedEmployee(employeeSkills, employeeDaysAvailable);
        EmployeeDTO secondEmployeeDTO = createDetailedEmployee(employeeSkills, employeeDaysAvailable);
        EmployeeDTO thirdEmployeeDTO = createDetailedEmployee(employeeSkills, employeeDaysAvailable);
        EmployeeDTO fourthEmployeeDTO = createDetailedEmployee(employeeSkills, employeeDaysAvailable);

        // Customers

        CustomerDTO firstCustomerDTO = createCustomerDTO();
        firstCustomerDTO = userController.createUpdateCustomer(firstCustomerDTO);
        CustomerDTO secondCustomerDTO = createCustomerDTO();
        secondCustomerDTO = userController.createUpdateCustomer(secondCustomerDTO);

        // Pets

        PetDTO firstCustomerFirstPetDTO = createDetailedPet(firstCustomerDTO.getId());
        PetDTO firstCustomerSecondPetDTO = createDetailedPet(firstCustomerDTO.getId());
        PetDTO secondCustomerFirstPetDTO = createDetailedPet(secondCustomerDTO.getId());
        PetDTO secondCustomerSecondPetDTO = createDetailedPet(secondCustomerDTO.getId());

        // Scheduling tests

        // TEST: Find the early appointment

        petList.add(firstCustomerFirstPetDTO.getId());
        petList.add(firstCustomerSecondPetDTO.getId());
        employeeList.add(firstEmployeeDTO.getId());
        employeeList.add(secondEmployeeDTO.getId());
        appointmentActivities.add(firstActivityName);
        appointmentActivities.add(secondActivityName);
        scheduleDTO = createScheduleDTO(
                petList,
                employeeList,
                appointmentDate,
                appointmentActivities);
        scheduleDTO.setStartTime(officeSchedule
                .getOfficeOpens()
                .plusMinutes(minutesNeeded)
                .toString());

        // Do a preview on first submission
        scheduleDTO.setPreview(true);
        scheduleGetDTO = scheduleController.createUpdateSchedule(scheduleDTO);
        Assertions.assertEquals(scheduleGetDTO.getStartTime(), scheduleDTO.getStartTime());
        Assertions.assertEquals(scheduleGetDTO.getEmployeeIds(), scheduleDTO.getEmployeeIds());
        Assertions.assertEquals(scheduleGetDTO.getPetIds(), scheduleDTO.getPetIds());
        Assertions.assertEquals(scheduleGetDTO.getDate(), appointmentDate);
        Assertions.assertEquals(scheduleGetDTO.getActivities(), appointmentActivities);
        Assertions.assertEquals(scheduleGetDTO.getId(), 0);
        Assertions.assertNotNull(scheduleGetDTO.getServiceCost());

        // Duplicate first submission works because first was a preview
        scheduleDTO.setPreview(false);
        ScheduleGetDTO firstScheduleGetDTO = scheduleController.createUpdateSchedule(scheduleDTO);
        Assertions.assertEquals(firstScheduleGetDTO.getStartTime(), scheduleGetDTO.getStartTime());
        Assertions.assertEquals(firstScheduleGetDTO.getEndTime(), scheduleGetDTO.getEndTime());
        Assertions.assertEquals(firstScheduleGetDTO.getEmployeeIds(), scheduleGetDTO.getEmployeeIds());
        Assertions.assertEquals(firstScheduleGetDTO.getPetIds(), scheduleGetDTO.getPetIds());
        Assertions.assertEquals(firstScheduleGetDTO.getDate(), scheduleGetDTO.getDate());
        Assertions.assertEquals(firstScheduleGetDTO.getActivities(), scheduleGetDTO.getActivities());
        Assertions.assertEquals(firstScheduleGetDTO.getServiceCost(), scheduleGetDTO.getServiceCost());
        Assertions.assertNotEquals(firstScheduleGetDTO.getId(), 0);
        LocalTime startingTime = dateVerification.verifyTime(
                scheduleGetDTO.getStartTime(),
                "schedule",
                "startTime");
        LocalTime endingTime = dateVerification.verifyTime(
                scheduleGetDTO.getEndTime(),
                "schedule",
                "endTime");
        Assertions.assertEquals((long) minutesNeeded,
                (long) startingTime.until(endingTime, ChronoUnit.MINUTES));

        petList = Lists.newArrayList();
        petList.add(secondCustomerFirstPetDTO.getId());
        petList.add(secondCustomerSecondPetDTO.getId());
        secondScheduleDTO = createScheduleDTO(
                petList,
                employeeList,
                appointmentDate,
                appointmentActivities);
        secondScheduleGetDTO = scheduleController.createUpdateSchedule(secondScheduleDTO);
        Assertions.assertEquals(officeSchedule.getOfficeOpens().toString(),
                secondScheduleGetDTO.getStartTime());
        Assertions.assertEquals(secondScheduleGetDTO.getEmployeeIds(), employeeList);
        Assertions.assertEquals(secondScheduleGetDTO.getPetIds(), petList);
        Assertions.assertEquals(secondScheduleGetDTO.getDate(), appointmentDate);
        Assertions.assertEquals(secondScheduleGetDTO.getActivities(), appointmentActivities);
        Assertions.assertNotNull(secondScheduleGetDTO.getServiceCost());
        startingTime = dateVerification.verifyTime(
                secondScheduleGetDTO.getStartTime(),
                "schedule",
                "startTime");
        endingTime = dateVerification.verifyTime(
                secondScheduleGetDTO.getEndTime(),
                "schedule",
                "endTime");
        Assertions.assertEquals((long) minutesNeeded,
                (long) startingTime.until(endingTime, ChronoUnit.MINUTES));

        // TEST: Schedule conflict (resubmit first schedule, with partial forward time offset)

        LocalTime offsetStartingTime = dateVerification.verifyTime(
                firstScheduleGetDTO.getStartTime(),
                "schedule",
                "startTime").plusMinutes(firstPetActivityType.getMinutes());
        scheduleDTO.setStartTime(offsetStartingTime.toString());
        ScheduleDTO finalScheduleDTO = scheduleDTO;
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    scheduleController.createUpdateSchedule(finalScheduleDTO);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "schedule",
                "employee",
                "Employee " + String.valueOf(firstEmployeeDTO.getId()) +
                        ", Schedule " + String.valueOf(firstScheduleGetDTO.getId()),
                "Employee scheduling conflict.");
        matchFieldError(eInvalid.getFieldErrors().get(1),
                "schedule",
                "employee",
                "Employee " + String.valueOf(secondEmployeeDTO.getId()) +
                        ", Schedule " + String.valueOf(firstScheduleGetDTO.getId()),
                "Employee scheduling conflict.");
        matchFieldError(eInvalid.getFieldErrors().get(2),
                "schedule",
                "pet",
                "Pet " + String.valueOf(firstCustomerFirstPetDTO.getId()) +
                        ", Schedule " + String.valueOf(firstScheduleGetDTO.getId()),
                "Pet scheduling conflict.");
        matchFieldError(eInvalid.getFieldErrors().get(3),
                "schedule",
                "pet",
                "Pet " + String.valueOf(firstCustomerSecondPetDTO.getId()) +
                        ", Schedule " + String.valueOf(firstScheduleGetDTO.getId()),
                "Pet scheduling conflict.");

        // TEST: Attempt to block off the lunch window

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateVerification.localTimeFormat());
        ScheduleGetDTO firstLunchScheduleDTO = null;
        ScheduleGetDTO lastLunchScheduleDTO = null;
        if (lunchOfficeSchedule != null) {
            int lunchWindow = (int) lunchOfficeSchedule.getLunchHourRangeStart().until(
                    lunchOfficeSchedule.getLunchHourRangeEnd(), ChronoUnit.MINUTES);
            int maxValidLunchSubmissions =
                    (lunchWindow - scheduleService.getLunchHourMinutes()) / minutesNeeded;
            LocalTime startTime = lunchOfficeSchedule.getLunchHourRangeStart();
            int submissionIndex = 0;
            ScheduleGetDTO lunchScheduleGetDTO = null;
            while (submissionIndex < maxValidLunchSubmissions) {
                startTime = startTime.plusMinutes(minutesNeeded * submissionIndex);
                secondScheduleDTO.setStartTime(startTime.format(formatter));
                lunchScheduleGetDTO = scheduleController.createUpdateSchedule(secondScheduleDTO);
                if (firstLunchScheduleDTO == null) {
                    firstLunchScheduleDTO = lunchScheduleGetDTO;
                }
                submissionIndex++;
            }
            lastLunchScheduleDTO = lunchScheduleGetDTO;

            // Valid lunch time submissions used; next submission causes a conflict

            startTime = startTime.plusMinutes(minutesNeeded * submissionIndex);
            secondScheduleDTO.setStartTime(startTime.format(formatter));
            ScheduleDTO finalSecondScheduleDTO = secondScheduleDTO;
            eInvalid = Assertions.assertThrows(
                    CustomApiInvalidParameterException.class, () -> {
                        scheduleController.createUpdateSchedule(finalSecondScheduleDTO);
                    });
            matchFieldError(eInvalid.getFieldErrors().get(0),
                    "schedule",
                    "employee",
                    "Employee " + String.valueOf(firstEmployeeDTO.getId()),
                    "Employee lunch scheduling conflict.");
            matchFieldError(eInvalid.getFieldErrors().get(1),
                    "schedule",
                    "employee",
                    "Employee " + String.valueOf(secondEmployeeDTO.getId()),
                    "Employee lunch scheduling conflict.");
        }

        // TEST: Too late in the day for requested schedule

        offsetStartingTime = officeSchedule
                .getOfficeCloses()
                .minusMinutes(firstPetActivityType.getMinutes());
        secondScheduleDTO.setStartTime(offsetStartingTime.format(formatter));
        ScheduleDTO finalSecondScheduleDTO1 = secondScheduleDTO;
        eInvalid = Assertions.assertThrows(
                CustomApiInvalidParameterException.class, () -> {
                    scheduleController.createUpdateSchedule(finalSecondScheduleDTO1);
                });
        matchFieldError(eInvalid.getFieldErrors().get(0),
                "schedule",
                "startTime",
                offsetStartingTime.toString(),
                "Start time is too late; appointment would go past office close.");

        // List all availability for the selected date

        ScheduleAvailabilityDTO scheduleAvailabilityDTO =
                new ScheduleAvailabilityDTO();
        scheduleAvailabilityDTO.setActivities(appointmentActivities);
        scheduleAvailabilityDTO.setDate(appointmentDate);
        String scheduleAvailabilityJson =
                scheduleController.getScheduleAvailability(scheduleAvailabilityDTO);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(scheduleAvailabilityJson);

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(dateVerification.localTimeFormat());
        String timeString = null;

        // First two employees have the same schedule

        Assertions.assertEquals(rootNode.get(0).get("employeeId").asText(),
                String.valueOf(firstEmployeeDTO.getId()));
        Assertions.assertEquals(rootNode.get(1).get("employeeId").asText(),
                String.valueOf(secondEmployeeDTO.getId()));
        Assertions.assertEquals(rootNode.get(0).get("localTimeRange"),
                rootNode.get(1).get("localTimeRange"));

        timeString = String.format("%02d",
                rootNode.get(0).get("localTimeRange").get(0).get("startTime").get(0).asInt()) +
                ":" +
                String.format("%02d",
                        rootNode.get(0).get("localTimeRange").get(0).get("startTime").get(1).asInt());
        Assertions.assertEquals(timeString, firstScheduleGetDTO.getEndTime());

        if (lunchOfficeSchedule != null) {
            timeString = String.format("%02d",
                    rootNode.get(0).get("localTimeRange").get(0).get("endTime").get(0).asInt()) +
                    ":" +
                    String.format("%02d",
                            rootNode.get(0).get("localTimeRange").get(0).get("endTime").get(1).asInt());
            Assertions.assertEquals(timeString, firstLunchScheduleDTO.getStartTime());
            timeString = String.format("%02d",
                    rootNode.get(0).get("localTimeRange").get(1).get("startTime").get(0).asInt()) +
                    ":" +
                    String.format("%02d",
                            rootNode.get(0).get("localTimeRange").get(1).get("startTime").get(1).asInt());
            Assertions.assertEquals(timeString, lastLunchScheduleDTO.getEndTime());
            timeString = String.format("%02d",
                    rootNode.get(0).get("localTimeRange").get(1).get("endTime").get(0).asInt()) +
                    ":" +
                    String.format("%02d",
                            rootNode.get(0).get("localTimeRange").get(1).get("endTime").get(1).asInt());
            Assertions.assertEquals(LocalTime.parse(timeString, formatter),
                    officeSchedule.getOfficeCloses());
        } else {
            timeString = String.format("%02d",
                    rootNode.get(0).get("localTimeRange").get(0).get("endTime").get(0).asInt()) +
                    ":" +
                    String.format("%02d",
                            rootNode.get(0).get("localTimeRange").get(0).get("endTime").get(1).asInt());
            Assertions.assertEquals(LocalTime.parse(timeString, formatter),
                    officeSchedule.getOfficeCloses());
        }

        // Last two employees have the same schedule (empty)

        Assertions.assertEquals(rootNode.get(2).get("employeeId").asText(),
                String.valueOf(thirdEmployeeDTO.getId()));
        Assertions.assertEquals(rootNode.get(3).get("employeeId").asText(),
                String.valueOf(fourthEmployeeDTO.getId()));
        Assertions.assertEquals(rootNode.get(2).get("localTimeRange"),
                rootNode.get(3).get("localTimeRange"));
        timeString = String.format("%02d",
                rootNode.get(2).get("localTimeRange").get(0).get("startTime").get(0).asInt()) +
                ":" +
                String.format("%02d",
                        rootNode.get(2).get("localTimeRange").get(0).get("startTime").get(1).asInt());
        Assertions.assertEquals(LocalTime.parse(timeString, formatter),
                officeSchedule.getOfficeOpens());
        timeString = String.format("%02d",
                rootNode.get(2).get("localTimeRange").get(0).get("endTime").get(0).asInt()) +
                ":" +
                String.format("%02d",
                        rootNode.get(2).get("localTimeRange").get(0).get("endTime").get(1).asInt());
        Assertions.assertEquals(LocalTime.parse(timeString, formatter),
                officeSchedule.getOfficeCloses());
    }

    private EmployeeDTO createDetailedEmployee(List<String> skills, List<String> daysAvailable) {
        EmployeeDTO employeeDTO = createEmployeeDTO();
        employeeDTO.setSkills(skills);
        employeeDTO.setDaysAvailable(daysAvailable);
        return userController.createUpdateEmployee(employeeDTO);
    }

    private PetDTO createDetailedPet(Long ownerId) {
        PetDTO petDTO = createPetDTO();
        petDTO.setOwnerId(ownerId);
        return petController.createUpdatePet(petDTO);
    }

    private void conductSearch(
            ScheduleQueryDTO scheduleQueryDTO,
            HashMap<Long, Long> scheduleIds,
            HashMap<Long, ScheduleGetDTO> schedules,
            int expectedCount) {

        // Executes a query and verifies the result

        int appointmentCount;
        Long appointmentId;

        List<ScheduleGetDTO> allAppointments = scheduleController.getAllSchedules(scheduleQueryDTO);
        appointmentCount = 0;
        for (ScheduleGetDTO eachSchedule: allAppointments) {
            appointmentId = eachSchedule.getId();
            if (! scheduleIds.containsKey(appointmentId)) {
                continue;
            }
            appointmentCount++;
            ScheduleGetDTO locatedSchedule = schedules.get(scheduleIds.get(appointmentId));
            matchSchedules(eachSchedule, locatedSchedule);
        }
        Assertions.assertEquals(appointmentCount, expectedCount);
    }

    private static void matchFieldError(
            FieldError fieldError,
            String objectName,
            String fieldName,
            String rejectedValue,
            String defaultMessage) {

        // A utility for confirming the content of returned sub-errors

        Assertions.assertEquals(fieldError.getObjectName(), objectName);
        Assertions.assertEquals(fieldError.getField(), fieldName);
        Assertions.assertEquals(fieldError.getRejectedValue(), rejectedValue);
        Assertions.assertEquals(fieldError.getDefaultMessage(), defaultMessage);
    }

    private void matchNote(Note noteToMatch,
                           Long id,
                           String description,
                           String note) {

        // A utility for confirming the content of returned notes

        Assertions.assertEquals(noteToMatch.getId(), id);
        Assertions.assertEquals(noteToMatch.getDescription(), description);
        Assertions.assertEquals(noteToMatch.getNote(), note);
    }

    private void matchSchedules(ScheduleGetDTO createdSchedule, ScheduleGetDTO retrievedSchedule) {
        Assertions.assertEquals(createdSchedule.getPetIds(),
                retrievedSchedule.getPetIds());
        Assertions.assertEquals(createdSchedule.getEmployeeIds(),
                retrievedSchedule.getEmployeeIds());
        Assertions.assertEquals(createdSchedule.getDate(),
                retrievedSchedule.getDate());
        Assertions.assertEquals(createdSchedule.getStartTime(),
                retrievedSchedule.getStartTime());
        Assertions.assertEquals(createdSchedule.getStatus(),
                retrievedSchedule.getStatus());
        Assertions.assertEquals(createdSchedule.getEndTime(),
                retrievedSchedule.getEndTime());
        Assertions.assertEquals(createdSchedule.getLastUpdateTime(),
                retrievedSchedule.getLastUpdateTime());
        Assertions.assertEquals(createdSchedule.getServiceCost(),
                retrievedSchedule.getServiceCost());
    }
}
