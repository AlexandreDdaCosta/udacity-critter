package com.udacity.jdnd.course3.critter.pet;

import com.udacity.jdnd.course3.critter.exception.CustomApiInvalidParameterException;
import com.udacity.jdnd.course3.critter.exception.CustomApiNoSuchElementException;
import com.udacity.jdnd.course3.critter.note.*;
import com.udacity.jdnd.course3.critter.schedule.Schedule;
import com.udacity.jdnd.course3.critter.schedule.ScheduleStatus;
import com.udacity.jdnd.course3.critter.user.Customer;
import com.udacity.jdnd.course3.critter.user.CustomerService;
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
public class PetService {

    @Autowired
    private CustomerService customerService;

    @Autowired
    PetNoteRepository petNoteRepository;

    @Autowired
    private PetRepository petRepository;

    public List<Pet> findAll() {
        return petRepository.findAll();
    }

    public List<Pet> findAllByCustomerId(Long customerId) {
        return petRepository.findAllByCustomerId(customerId);
    }

    public Pet findById(Long petId) {
        try {
            return petRepository.findById(petId).get();
        } catch (InvalidDataAccessApiUsageException e) {
            FieldError fieldError = new FieldError(
                    "pet",
                    "id",
                    null,
                    false,
                    null,
                    null,
                    "Missing pet ID.");
            throw new CustomApiNoSuchElementException(fieldError);
        } catch (NoSuchElementException e) {
            FieldError fieldError = new FieldError(
                    "pet",
                    "id",
                    String.valueOf(petId),
                    false,
                    null,
                    null,
                    "Unknown pet ID.");
            throw new CustomApiNoSuchElementException(fieldError);
        }
    }

    public Customer findCustomerById(Long petId) {
        try {
            Pet pet = petRepository.findById(petId).get();
            return pet.getCustomer();
        } catch (NoSuchElementException e) {
            FieldError fieldError = new FieldError(
                    "pet",
                    "id",
                    String.valueOf(petId),
                    false,
                    null,
                    null,
                    "Unknown pet ID.");
            throw new CustomApiNoSuchElementException(fieldError);
        }
    }

    public PetNote getPetNote(Long petId, Long noteId) {
        PetNote petNote = petNoteRepository.findByIdAndPetId(noteId, petId);
        if (petNote == null) {
            FieldError fieldError = new FieldError(
                    "note",
                    "pet ID + note ID",
                    String.valueOf(petId) + ", " + String.valueOf(noteId),
                    false,
                    null,
                    null,
                    "No such note exists.");
            throw new CustomApiNoSuchElementException(fieldError);
        }
        return petNote;
    }

    public List<Note> getPetNotes(Long petId) {
        Pet pet = findById(petId);
        return petNoteRepository.findAllProjectedBy(petId);
    }

    public List<Long> getPetNoteIds(Long petId) {
        Pet pet = findById(petId);
        return petNoteRepository.findAllIdsProjectedBy(petId);
    }

    @Transactional
    public Pet save(Pet pet, long ownerId) {
        Customer customer = null;
        try {
            customer = customerService.findEntryById(ownerId);
        } catch (NoSuchElementException e) {
            FieldError fieldError = new FieldError(
                    "customer",
                    "id",
                    String.valueOf(ownerId),
                    false,
                    null,
                    null,
                    "Unknown customer ID.");
            throw new CustomApiInvalidParameterException(fieldError);
        }
        if (customer.isArchived()) {
            FieldError fieldError = new FieldError(
                    "customer",
                    "archived",
                    "true",
                    false,
                    null,
                    null,
                    "Pet target owner entry is archived; no changes allowed.");
            throw new CustomApiInvalidParameterException(fieldError);
        }
        pet.setCustomer(customer);
        Pet existingPet = petRepository.findByTypeAndNameAndCustomer(
                pet.getType(),
                pet.getName(),
                pet.getCustomer()
        );
        if (existingPet != null) {
            if (pet.getId() == null ||
                    pet.getId() == 0 ||
                    !pet.getId().equals(existingPet.getId())) {
                FieldError fieldError = new FieldError(
                        "pet",
                        "type + name + customer",
                        "A pet of that type and name under that customer already exists.");
                throw new CustomApiInvalidParameterException(fieldError);
            }
        }
        Pet existingPetEntry = null;
        if (pet.getId() != null && pet.getId() != 0) {
            existingPetEntry = findById(pet.getId());
            if (existingPetEntry.isArchived() && pet.isArchived()) {
                FieldError fieldError = new FieldError(
                        "pet",
                        "archived",
                        "true",
                        false,
                        null,
                        null,
                        "Pet entry is archived; no changes allowed.");
                throw new CustomApiInvalidParameterException(fieldError);
            }
            pet.setNotes(existingPetEntry.getNotes());
        }
        Customer existingCustomer = null;
        if (existingPetEntry != null) {
            existingCustomer = existingPetEntry.getCustomer();
            if (existingCustomer.isArchived()) {
                FieldError fieldError = new FieldError(
                        "customer",
                        "archived",
                        "true",
                        false,
                        null,
                        null,
                        "Pet owner's entry is archived; no changes allowed.");
                throw new CustomApiInvalidParameterException(fieldError);
            }
        }

        verifyPetSave(pet, existingPetEntry);

        String noteDescription = null;
        String noteText = null;
        PetNote petNote = null;
        if (pet.getNoteText() != null) {
            noteDescription = pet.getNoteDescription();
            noteText = pet.getNoteText();
            petNote = new PetNote();
            petNote.setDescription(noteDescription);
            petNote.setNote(noteText);
            petNote = petNote.validate();
        }
        pet = petRepository.save(pet);
        if (petNote != null) {
            petNote.setPet(pet);
            petNote = petNoteRepository.save(petNote);
            pet = pet.addPetNote(petNote);
            pet = petRepository.save(pet);
        }
        if (noteText != null) {
            pet.setNoteId(petNote.getId());
            pet.setNoteDescription(noteDescription);
            pet.setNoteText(noteText);
        }
        if (existingPetEntry == null) {
            customer.addPet(pet);
            customerService.save(customer);
        }
        if (existingCustomer != null) {
            List<Pet> savedPets = existingCustomer.getPets();
            savedPets.remove(existingPetEntry);
            existingCustomer.setPets(null);
            customer.addPet(pet);
            customerService.save(customer);
            for (Pet previousPet: savedPets) {
                existingCustomer.addPet(previousPet);
            }
        }
        return pet;
    }

    @Transactional
    public PetNote saveNote(PetNote petNote) {
        boolean addedNote = false;
        if (petNote.getId() != null && petNote.getId() != 0) {
            getPetNote(
                    petNote.getPet().getId(),
                    petNote.getId());
        } else {
            addedNote = true;
        }
        petNote = petNoteRepository.save(petNote);
        if (addedNote) {
            Pet pet = petNote.getPet();
            pet = pet.addPetNote(petNote);
            petRepository.save(pet);
        }
        return petNote;
    }

    public void verifyPetSave(Pet newPet, Pet oldPet) {
        List<FieldError> fieldErrors = new ArrayList<>(Collections.emptyList());

        boolean pendingSchedules = false;
        if (oldPet != null && oldPet.getSchedules() != null) {
            for (Schedule schedule: oldPet.getSchedules()) {
                if (schedule.getStatus() == ScheduleStatus.PENDING) {
                    pendingSchedules = true;
                    break;
                }
            }
        }

        if (pendingSchedules && newPet.isArchived()) {
            FieldError fieldError = new FieldError(
                    "pet",
                    "archived",
                    "Pet cannot be archived due to pending schedule.");
            fieldErrors.add(fieldError);
        }

        if (pendingSchedules && oldPet.getType() != newPet.getType()) {
            FieldError fieldError = new FieldError(
                    "pet",
                    "type",
                    "Pet type cannot be changed due to pending schedule.");
            fieldErrors.add(fieldError);
        }

        if (pendingSchedules && oldPet.getCustomer() != newPet.getCustomer()) {
            FieldError fieldError = new FieldError(
                    "pet",
                    "customer",
                    "Pet owner cannot be changed due to pending schedule.");
            fieldErrors.add(fieldError);
        }

        if (! fieldErrors.isEmpty()) {
            throw new CustomApiInvalidParameterException(fieldErrors);
        }
    }
}
