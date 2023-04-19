package com.udacity.jdnd.course3.critter.user;

import com.google.common.collect.Lists;
import com.udacity.jdnd.course3.critter.activity.Activity;
import com.udacity.jdnd.course3.critter.activity.ActivityVerification;
import com.udacity.jdnd.course3.critter.date.DateVerification;
import com.udacity.jdnd.course3.critter.exception.CustomApiInvalidParameterException;
import com.udacity.jdnd.course3.critter.note.CustomerNote;
import com.udacity.jdnd.course3.critter.note.EmployeeNote;
import com.udacity.jdnd.course3.critter.note.Note;
import com.udacity.jdnd.course3.critter.pet.Pet;
import com.udacity.jdnd.course3.critter.pet.PetService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles web requests related to Users.
 *
 * Includes requests for both customers and employees. Splitting this into separate user and customer controllers
 * would be fine too, though that is not part of the required scope for this class.
 */

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private ActivityVerification activityVerification;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerVerification customerVerification;

    @Autowired
    private DateVerification dateVerification;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeVerification employeeVerification;

    @Autowired
    private PetService petService;

    @PostMapping("/customer")
    public CustomerDTO createUpdateCustomer(@RequestBody CustomerDTO customerDTO) {
        Customer customer = customerService.save(customerDTOToEntity(customerDTO));
        return(customerEntityToDTO(customer));
    }

    @GetMapping("/customer")
    public List<CustomerGetDTO> getAllCustomers(){
        List<Customer> customers = customerService.findAll();
        return customers.stream()
                .map(UserController::customerEntityToGetDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/customer/{customerId}")
    public CustomerGetDTO getCustomer(@PathVariable long customerId) {
        Customer customer = customerService.findById(customerId);
        return(customerEntityToGetDTO(customer));
    }

    @GetMapping("/customer/pet/{petId}")
    public CustomerDTO getOwnerByPet(@PathVariable long petId){
        Customer customer = petService.findCustomerById(petId);
        return(customerEntityToDTO(customer));
    }

    @PostMapping("/customer/note")
    public CustomerNoteDTO createUpdateCustomerNote(@RequestBody CustomerNoteDTO customerNoteDTO) {
        CustomerNote customerNote = customerService.saveNote(
                customerNoteDTOToEntity(customerNoteDTO));
        return(customerNoteEntityToDTO(customerNote));
    }

    @GetMapping("/customer/{customerId}/note/{noteId}")
    public CustomerNoteDTO getCustomerNote(@PathVariable long customerId, @PathVariable long noteId) {
        CustomerNote customerNote = customerService.getCustomerNote(customerId, noteId);
        return(customerNoteEntityToDTO(customerNote));
    }

    @GetMapping("/customer/{customerId}/note")
    public List<Note> getCustomerNotes(@PathVariable long customerId) {
        return customerService.getCustomerNotes(customerId);
    }

    @GetMapping("/customer/{customerId}/noteid")
    public List<Long> getCustomerNoteIds(@PathVariable long customerId) {
        return customerService.getCustomerNoteIds(customerId);
    }

    @PostMapping("/employee")
    public EmployeeDTO createUpdateEmployee(@RequestBody EmployeeDTO employeeDTO) {
        Employee employee = employeeService.save(
                employeeDTOToEntity(employeeDTO));
        return(employeeEntityToDTO(employee));
    }

    @GetMapping("/employee")
    public List<EmployeeGetDTO> getAllEmployees(){
        List<Employee> employees = employeeService.findAll();
        return employees.stream()
                .map(UserController::employeeEntityToGetDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/employee/{employeeId}")
    public EmployeeGetDTO getEmployee(@PathVariable long employeeId) {
        Employee employee = employeeService.findById(employeeId);
        return(employeeEntityToGetDTO(employee));
    }

    @PostMapping("/employee/{employeeId}")
    public List setAvailability(@RequestBody List<String> daysAvailable, @PathVariable long employeeId) {
        return(employeeService.setAvailability(daysAvailable, employeeId));
    }

    @GetMapping("/employee/availability")
    public List<EmployeeGetDTO> findEmployeesForService(@RequestBody EmployeeRequestDTO employeeRequestDTO) {
        List<FieldError> fieldErrors = new ArrayList<FieldError>();

        try {
            if (employeeRequestDTO.getDate() == null) {
                FieldError fieldError = new FieldError(
                        "schedule",
                        "skills",
                        null,
                        false,
                        null,
                        null,
                        "Skills required.");
                fieldErrors.add(fieldError);
            } else {
                activityVerification.verifyActivities(
                        employeeRequestDTO.getSkills(),
                        "employee"
                );
            }
        } catch (CustomApiInvalidParameterException e) {
            fieldErrors.addAll(e.getFieldErrors());
        }

        DayOfWeek dayOfWeek = null;
        try {
            if (employeeRequestDTO.getDate() == null) {
                FieldError fieldError = new FieldError(
                        "schedule",
                        "date",
                        null,
                        false,
                        null,
                        null,
                        "Date required.");
                fieldErrors.add(fieldError);
            } else {
                LocalDate localDate = dateVerification.verifyDateFormat(
                        employeeRequestDTO.getDate(),
                        "employee");
                dayOfWeek = localDate.getDayOfWeek();
            }
        } catch (CustomApiInvalidParameterException e) {
            fieldErrors.addAll(e.getFieldErrors());
        }

        if (! fieldErrors.isEmpty()) {
            throw new CustomApiInvalidParameterException(fieldErrors);
        }

        return employeeService.findEmployeesForService(
                employeeRequestDTO.getSkills(), dayOfWeek)
                .stream()
                .map(UserController::employeeEntityToGetDTO)
                .collect(Collectors.toList());
    }

    @PostMapping("/employee/note")
    public EmployeeNoteDTO createUpdateEmployeeNote(@RequestBody EmployeeNoteDTO employeeNoteDTO) {
        EmployeeNote employeeNote = employeeService.saveNote(
                employeeNoteDTOToEntity(employeeNoteDTO));
        return(employeeNoteEntityToDTO(employeeNote));
    }

    @GetMapping("/employee/{employeeId}/note/{noteId}")
    public EmployeeNoteDTO getEmployeeNote(@PathVariable long employeeId, @PathVariable long noteId) {
        EmployeeNote employeeNote = employeeService.getEmployeeNote(employeeId, noteId);
        return(employeeNoteEntityToDTO(employeeNote));
    }

    @GetMapping("/employee/{employeeId}/note")
    public List<Note> getEmployeeNotes(@PathVariable long employeeId) {
        return employeeService.getEmployeeNotes(employeeId);
    }

    @GetMapping("/employee/{employeeId}/noteid")
    public List<Long> getEmployeeNoteIds(@PathVariable long employeeId) {
        return employeeService.getEmployeeNoteIds(employeeId);
    }

    // DTO <---> Entity conversions

    private Customer customerDTOToEntity(CustomerDTO customerDTO){
        List<FieldError> fieldErrors = new ArrayList<FieldError>();
        Customer customer = new Customer();
        BeanUtils.copyProperties(customerDTO, customer);
        if (customerDTO.isArchived()) {
            customer.setArchived(true);
        } else {
            customer.setArchived(false);
        }
        List<Pet> petList = petService.findAllByCustomerId(customer.getId());
        if (petList != null) {
            customer.setPets(petList);
        }
        if (customerDTO.getNoteDescription() != null || customerDTO.getNoteText() != null) {
            CustomerNote customerNote = new CustomerNote();
            customerNote.setDescription(customerDTO.getNoteDescription());
            customerNote.setNote(customerDTO.getNoteText());
            try {
                customerNote = customerNote.validate();
            } catch (CustomApiInvalidParameterException e) {
                fieldErrors.addAll(e.getFieldErrors());
            }
            customer.setNoteDescription(customerDTO.getNoteDescription());
            customer.setNoteText(customerDTO.getNoteText());
        }

        try {
            customer = customer.validate();
        } catch (CustomApiInvalidParameterException e) {
            fieldErrors.addAll(e.getFieldErrors());
        }

        if (! fieldErrors.isEmpty()) {
            throw new CustomApiInvalidParameterException(fieldErrors);
        }

        return customer;
    }

    private static CustomerDTO customerEntityToDTO(Customer customer){
        CustomerDTO customerDTO = new CustomerDTO();
        BeanUtils.copyProperties(customer, customerDTO);
        List<Long> petIds = new ArrayList<>();
        List<Pet> pets = customer.getPets();
        if (pets != null) {
            for (Pet pet : pets) {
                petIds.add(pet.getId());
            }
            customerDTO.setPetIds(petIds);
        }
        if (customer.getNoteText() != null) {
            customerDTO.setNoteDescription(customer.getNoteDescription());
            customerDTO.setNoteText(customer.getNoteText());
            customerDTO.setNoteId(customer.getNoteId());
        }
        if (customer.isArchived()) {
            customerDTO.setArchived(true);
        } else {
            customerDTO.setArchived(false);
        }
        customer.setLastUpdateTime(customer.getLastUpdateTime());
        return customerDTO;
    }

    private static CustomerGetDTO customerEntityToGetDTO(Customer customer) {
        CustomerDTO customerDTO = customerEntityToDTO(customer);
        CustomerGetDTO customerGetDTO = new CustomerGetDTO();
        BeanUtils.copyProperties(customerDTO, customerGetDTO);
        return customerGetDTO;
    }

    private CustomerNote customerNoteDTOToEntity (CustomerNoteDTO customerNoteDTO) {
        List<FieldError> fieldErrors = new ArrayList<>(Collections.emptyList());
        CustomerNote customerNote = new CustomerNote();
        BeanUtils.copyProperties(customerNoteDTO, customerNote);

        try {
            customerNote.setCustomer(customerVerification.verifyCustomer(customerNoteDTO.getCustomerId()));
        } catch (CustomApiInvalidParameterException e) {
            fieldErrors.addAll(e.getFieldErrors());
        }

        try {
            customerNote = customerNote.validate();
        } catch (CustomApiInvalidParameterException e) {
            fieldErrors.addAll(e.getFieldErrors());
        }

        if (! fieldErrors.isEmpty()) {
            throw new CustomApiInvalidParameterException(fieldErrors);
        }
        return customerNote;
    }

    private CustomerNoteDTO customerNoteEntityToDTO(CustomerNote customerNote) {
        CustomerNoteDTO customerNoteDTO = new CustomerNoteDTO();
        BeanUtils.copyProperties(customerNote, customerNoteDTO);
        customerNoteDTO.setCustomerId(customerNote.getCustomer().getId());
        return customerNoteDTO;
    }

    private Employee employeeDTOToEntity(EmployeeDTO employeeDTO) {
        List<FieldError> fieldErrors = new ArrayList<FieldError>();
        Employee employee = new Employee();
        employee.setId(employeeDTO.getId());
        employee.setName(employeeDTO.getName());
        if (employeeDTO.isArchived()) {
            employee.setArchived(true);
        } else {
            employee.setArchived(false);
        }

        try {
            employee.setSkills(activityVerification.verifyActivities(
                    employeeDTO.getSkills(),
                    "employee"
            ));
        } catch (CustomApiInvalidParameterException e) {
            fieldErrors = e.getFieldErrors();
        }

        if (employeeDTO.getDaysAvailable() != null && employeeDTO.getDaysAvailable().size() > 0) {
            try {
                List<DayOfWeek> availableDays =
                        dateVerification.verifyDaysOfWeek(
                                employeeDTO.getDaysAvailable(),
                                "employee");
                employee.setDaysAvailable(availableDays);
            } catch (CustomApiInvalidParameterException e) {
                fieldErrors.addAll(e.getFieldErrors());
            }
        }

        if (employeeDTO.getNoteDescription() != null || employeeDTO.getNoteText() != null) {
            EmployeeNote employeeNote = new EmployeeNote();
            employeeNote.setDescription(employeeDTO.getNoteDescription());
            employeeNote.setNote(employeeDTO.getNoteText());
            try {
                employeeNote = employeeNote.validate();
            } catch (CustomApiInvalidParameterException e) {
                fieldErrors.addAll(e.getFieldErrors());
            }
            employee.setNoteDescription(employeeDTO.getNoteDescription());
            employee.setNoteText(employeeDTO.getNoteText());
        }

        try {
            employee = employee.validate();
        } catch (CustomApiInvalidParameterException e) {
            fieldErrors.addAll(e.getFieldErrors());
        }

        if (! fieldErrors.isEmpty()) {
            throw new CustomApiInvalidParameterException(fieldErrors);
        }
        return employee;
    }

    private static EmployeeDTO employeeEntityToDTO(Employee employee){
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setId(employee.getId());
        employeeDTO.setName(employee.getName());
        if (employee.getNoteText() != null) {
            employeeDTO.setNoteDescription(employee.getNoteDescription());
            employeeDTO.setNoteText(employee.getNoteText());
            employeeDTO.setNoteId(employee.getNoteId());
        }
        if (employee.isArchived()) {
            employeeDTO.setArchived(true);
        } else {
            employeeDTO.setArchived(false);
        }
        if (employee.getDaysAvailable() != null && employee.getDaysAvailable().size() > 0) {
            List<String> availableDays = new ArrayList<String>();
            for (DayOfWeek dayOfWeek : employee.getDaysAvailable()) {
                availableDays.add(dayOfWeek.toString());
            }
            employeeDTO.setDaysAvailable(availableDays);
        }
        List<Activity> employeeSkills = employee.getSkills();
        if (employeeSkills != null && employeeSkills.size() > 0) {
            List<String> employeeActivities = Lists.newArrayList();
            for (Activity employeeSkill : employeeSkills) {
                employeeActivities.add(employeeSkill.getName());
            }
            employeeDTO.setSkills(employeeActivities);
        }
        employeeDTO.setLastUpdateTime(employee.getLastUpdateTime());
        return employeeDTO;
    }

    private static EmployeeGetDTO employeeEntityToGetDTO(Employee employee) {
        EmployeeDTO employeeDTO = employeeEntityToDTO(employee);
        EmployeeGetDTO employeeGetDTO = new EmployeeGetDTO();
        BeanUtils.copyProperties(employeeDTO, employeeGetDTO);
        return employeeGetDTO;
    }

    private EmployeeNote employeeNoteDTOToEntity (EmployeeNoteDTO employeeNoteDTO) {
        List<FieldError> fieldErrors = new ArrayList<>(Collections.emptyList());
        EmployeeNote employeeNote = new EmployeeNote();
        BeanUtils.copyProperties(employeeNoteDTO, employeeNote);


        try {
            employeeNote.setEmployee(employeeVerification.verifyEmployee(employeeNoteDTO.getEmployeeId()));
        } catch (CustomApiInvalidParameterException e) {
            fieldErrors.addAll(e.getFieldErrors());
        }

        try {
            employeeNote = employeeNote.validate();
        } catch (CustomApiInvalidParameterException e) {
            fieldErrors.addAll(e.getFieldErrors());
        }

        if (! fieldErrors.isEmpty()) {
            throw new CustomApiInvalidParameterException(fieldErrors);
        }
        return employeeNote;
    }

    private EmployeeNoteDTO employeeNoteEntityToDTO(EmployeeNote employeeNote) {
        EmployeeNoteDTO employeeNoteDTO = new EmployeeNoteDTO();
        BeanUtils.copyProperties(employeeNote, employeeNoteDTO);
        employeeNoteDTO.setEmployeeId(employeeNote.getEmployee().getId());
        return employeeNoteDTO;
    }
}
