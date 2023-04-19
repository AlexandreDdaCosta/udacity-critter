package com.udacity.jdnd.course3.critter.user;

import com.udacity.jdnd.course3.critter.exception.CustomApiInvalidParameterException;
import com.udacity.jdnd.course3.critter.exception.CustomApiNoSuchElementException;
import com.udacity.jdnd.course3.critter.note.CustomerNote;
import com.udacity.jdnd.course3.critter.note.CustomerNoteRepository;
import com.udacity.jdnd.course3.critter.note.Note;
import com.udacity.jdnd.course3.critter.pet.Pet;
import com.udacity.jdnd.course3.critter.schedule.Schedule;
import com.udacity.jdnd.course3.critter.schedule.ScheduleStatus;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.stereotype.Service;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CustomerService {

    @Autowired
    CustomerNoteRepository customerNoteRepository;

    @Autowired
    private CustomerRepository customerRepository;

    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    public Customer findById(Long customerId) {
        try {
            return findEntryById(customerId);
        }  catch (InvalidDataAccessApiUsageException e) {
            FieldError fieldError = new FieldError(
                    "customer",
                    "id",
                    null,
                    false,
                    null,
                    null,
                    "Missing customer ID.");
            throw new CustomApiNoSuchElementException(fieldError);
        } catch (NoSuchElementException e) {
            FieldError fieldError = new FieldError(
                    "customer",
                    "id",
                    String.valueOf(customerId),
                    false,
                    null,
                    null,
                    "Unknown customer ID.");
            throw new CustomApiNoSuchElementException(fieldError);
        }
    }

    public Customer findEntryById(Long customerId) {
        return customerRepository.findById(customerId).get();
    }

    public CustomerNote getCustomerNote(Long customerId, Long noteId) {
        CustomerNote customerNote = customerNoteRepository.findByIdAndCustomerId(noteId, customerId);
        if (customerNote == null) {
            FieldError fieldError = new FieldError(
                    "note",
                    "customer ID + note ID",
                    String.valueOf(customerId) + ", " + String.valueOf(noteId),
                    false,
                    null,
                    null,
                    "No such note exists.");
            throw new CustomApiNoSuchElementException(fieldError);
        }
        return customerNote;
    }

    public List<Note> getCustomerNotes(Long customerId) {
        Customer customer = findById(customerId);
        return customerNoteRepository.findAllProjectedBy(customerId);
    }

    public List<Long> getCustomerNoteIds(Long customerId) {
        Customer customer = findById(customerId);
        return customerNoteRepository.findAllIdsProjectedBy(customerId);
    }

    @Transactional
    public Customer save(Customer customer) {
        Customer existingCustomer = customerRepository.findByNameAndPhoneNumber(
                customer.getName(),
                customer.getPhoneNumber()
        );
        if (existingCustomer != null) {
            if (customer.getId() == null ||
                    customer.getId() == 0 ||
                    !customer.getId().equals(existingCustomer.getId())) {
                FieldError fieldError = new FieldError(
                        "customer",
                        "name + phoneNumber",
                        customer.getName() + " " + customer.getPhoneNumber(),
                        false,
                        null,
                        null,
                        "A customer with that name and phone number already exists.");
                throw new CustomApiInvalidParameterException(fieldError);
            }
        }
        Customer existingCustomerEntry = null;
        if (customer.getId() != null && customer.getId() != 0) {
            existingCustomerEntry = findById(customer.getId());
            if (existingCustomerEntry.isArchived() && customer.isArchived()) {
                FieldError fieldError = new FieldError(
                        "customer",
                        "archived",
                        "true",
                        false,
                        null,
                        null,
                        "Customer entry is archived; no changes allowed.");
                throw new CustomApiInvalidParameterException(fieldError);
            }
            customer.setNotes(existingCustomerEntry.getNotes());
        }

        verifyCustomerSave(customer, existingCustomerEntry);

        String noteDescription = null;
        String noteText = null;
        CustomerNote customerNote = null;
        if (customer.getNoteText() != null) {
            noteDescription = customer.getNoteDescription();
            noteText = customer.getNoteText();
            customerNote = new CustomerNote();
            customerNote.setDescription(noteDescription);
            customerNote.setNote(noteText);
            customerNote = customerNote.validate();
        }
        customer = customerRepository.save(customer);
        if (customerNote != null) {
            customerNote.setCustomer(customer);
            customerNote = customerNoteRepository.save(customerNote);
            customer = customer.addCustomerNote(customerNote);
            customer = customerRepository.save(customer);
        }
        if (noteText != null) {
            customer.setNoteId(customerNote.getId());
            customer.setNoteDescription(noteDescription);
            customer.setNoteText(noteText);
        }
        return customer;
    }

    @Transactional
    public CustomerNote saveNote(CustomerNote customerNote) {
        boolean addedNote = false;
        if (customerNote.getId() != null && customerNote.getId() != 0) {
            getCustomerNote(
                    customerNote.getCustomer().getId(),
                    customerNote.getId());
        } else {
            addedNote = true;
        }
        customerNote = customerNoteRepository.save(customerNote);
        if (addedNote) {
            Customer customer = customerNote.getCustomer();
            customer = customer.addCustomerNote(customerNote);
            customerRepository.save(customer);
        }
        return customerNote;
    }

    public void verifyCustomerSave(Customer newCustomer, Customer oldCustomer) {
        List<FieldError> fieldErrors = new ArrayList<>(Collections.emptyList());

        if (newCustomer.isArchived() &&
                oldCustomer != null &&
                oldCustomer.getPets() != null) {
            for (Pet pet: oldCustomer.getPets()) {
                if (pet.getSchedules() != null) {
                    for (Schedule schedule : pet.getSchedules()) {
                        if (schedule.getStatus() == ScheduleStatus.PENDING) {
                            FieldError fieldError = new FieldError(
                                    "customer",
                                    "archived",
                                    String.valueOf(pet.getId()),
                                    false,
                                    null,
                                    null,
                                    "Customer cannot be archived " +
                                            "due to pending pet schedule.");
                            fieldErrors.add(fieldError);
                            break;
                        }
                    }
                }
            }
        }

        if (! fieldErrors.isEmpty()) {
            throw new CustomApiInvalidParameterException(fieldErrors);
        }
    }
}
