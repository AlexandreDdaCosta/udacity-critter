package com.udacity.jdnd.course3.critter.pet;

import com.udacity.jdnd.course3.critter.exception.CustomApiInvalidParameterException;
import com.udacity.jdnd.course3.critter.exception.CustomApiNoSuchElementException;
import com.udacity.jdnd.course3.critter.note.Note;
import com.udacity.jdnd.course3.critter.note.PetNote;
import com.udacity.jdnd.course3.critter.user.Customer;
import com.udacity.jdnd.course3.critter.user.CustomerVerification;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles web requests related to Pets.
 * */

@RestController
@RequestMapping("/pet")
public class PetController {

    @Autowired
    private CustomerVerification customerVerification;

    @Autowired
    private PetService petService;

    @Autowired
    private PetVerification petVerification;

    @PostMapping
    public PetDTO createUpdatePet(@RequestBody PetDTO petDTO) {
        Pet pet = petService.save(petDTOToEntity(petDTO), petDTO.getOwnerId());
        return(petEntityToDTO(pet));
    }

    @GetMapping("/{petId}")
    public PetGetDTO getPet(@PathVariable long petId) {
        Pet pet = petService.findById(petId);
        return(petEntityToGetDTO(pet));
    }

    @GetMapping
    public List<PetGetDTO> getPets(){
        return petService.findAll()
                .stream()
                .map(PetController::petEntityToGetDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/owner/{ownerId}")
    public List<PetGetDTO> getPetsByOwner(@PathVariable long ownerId) {
        Customer customer = customerVerification.verifyCustomer(ownerId);
        return petService.findAllByCustomerId(ownerId)
                .stream()
                .map(PetController::petEntityToGetDTO)
                .collect(Collectors.toList());
    }

    @PostMapping("/note")
    public PetNoteDTO createUpdatePetNote(@RequestBody PetNoteDTO petNoteDTO) {
        PetNote petNote = petService.saveNote(
                petNoteDTOToEntity(petNoteDTO));
        return(petNoteEntityToDTO(petNote));
    }

    @GetMapping("/{petId}/note/{noteId}")
    public PetNoteDTO getPetNote(@PathVariable long petId, @PathVariable long noteId) {
        PetNote petNote = petService.getPetNote(petId, noteId);
        return(petNoteEntityToDTO(petNote));
    }

    @GetMapping("/{petId}/note")
    public List<Note> getPetNotes(@PathVariable long petId) {
        return petService.getPetNotes(petId);
    }

    @GetMapping("/{petId}/noteid")
    public List<Long> getPetNoteIds(@PathVariable long petId) {
        return petService.getPetNoteIds(petId);
    }

    // DTO <---> Entity conversions

    public Pet petDTOToEntity(PetDTO petDTO){
        List<FieldError> fieldErrors = new ArrayList<FieldError>();
        Pet pet = new Pet();
        BeanUtils.copyProperties(petDTO, pet);

        if (petDTO.isArchived()) {
            pet.setArchived(true);
        } else {
            pet.setArchived(false);
        }

        try {
            pet.setCustomer(customerVerification.verifyCustomer(petDTO.getOwnerId()));
        } catch (CustomApiInvalidParameterException e) {
            fieldErrors.addAll(e.getFieldErrors());
        }

        try {
            pet.setBirthDate(petVerification.verifyPetBirthDate(petDTO.getBirthDate()));
        } catch (CustomApiInvalidParameterException e) {
            fieldErrors.addAll(e.getFieldErrors());
        }

        try {
            pet.setType(petVerification.verifyPetType(petDTO.getType()));
        } catch (CustomApiInvalidParameterException e) {
            fieldErrors.addAll(e.getFieldErrors());
        }

        if (petDTO.getNoteDescription() != null || petDTO.getNoteText() != null) {
            PetNote petNote = new PetNote();
            petNote.setDescription(petDTO.getNoteDescription());
            petNote.setNote(petDTO.getNoteText());
            try {
                petNote = petNote.validate();
                pet.setNoteDescription(petDTO.getNoteDescription());
                pet.setNoteText(petDTO.getNoteText());
            } catch (CustomApiInvalidParameterException e) {
                fieldErrors.addAll(e.getFieldErrors());
            }
        }
        try {
            pet = pet.validate();
        } catch (CustomApiInvalidParameterException e) {
            fieldErrors.addAll(e.getFieldErrors());
        }
        if (! fieldErrors.isEmpty()) {
            throw new CustomApiInvalidParameterException(fieldErrors);
        }
        return pet;
    }

    private static PetDTO petEntityToDTO(Pet pet){
        PetDTO petDTO = new PetDTO();
        BeanUtils.copyProperties(pet, petDTO);
        petDTO.setOwnerId(pet.getCustomer().getId());
        if (pet.getNoteText() != null) {
            petDTO.setNoteDescription(pet.getNoteDescription());
            petDTO.setNoteText(pet.getNoteText());
            petDTO.setNoteId(pet.getNoteId());
        }
        if (pet.isArchived()) {
            petDTO.setArchived(true);
        } else {
            petDTO.setArchived(false);
        }
        if (pet.getBirthDate() != null) {
            petDTO.setRawBirthDate(pet.getBirthDate());
        }
        PetType petType = pet.getType();
        petDTO.setType(petType.getName());
        petDTO.setLastUpdateTime(pet.getLastUpdateTime());
        return petDTO;
    }

    private static PetGetDTO petEntityToGetDTO(Pet pet) {
        PetDTO petDTO = petEntityToDTO(pet);
        PetGetDTO petGetDTO = new PetGetDTO();
        BeanUtils.copyProperties(petDTO, petGetDTO);
        return petGetDTO;
    }

    private PetNote petNoteDTOToEntity (PetNoteDTO petNoteDTO) {
        List<FieldError> fieldErrors = new ArrayList<>(Collections.emptyList());
        PetNote petNote = new PetNote();
        BeanUtils.copyProperties(petNoteDTO, petNote);
        try {
            petNote.setPet(petVerification.verifyPet(
                    petNoteDTO.getPetId()));
        } catch (CustomApiNoSuchElementException e) {
            fieldErrors.add(e.getFieldError());
        }
        try {
            petNote = petNote.validate();
        } catch (CustomApiInvalidParameterException e) {
            fieldErrors.addAll(e.getFieldErrors());
        }
        if (! fieldErrors.isEmpty()) {
            throw new CustomApiInvalidParameterException(fieldErrors);
        }
        return petNote;
    }

    public PetNoteDTO petNoteEntityToDTO(PetNote petNote) {
        PetNoteDTO petNoteDTO = new PetNoteDTO();
        BeanUtils.copyProperties(petNote, petNoteDTO);
        petNoteDTO.setPetId(petNote.getPet().getId());
        return petNoteDTO;
    }
}
