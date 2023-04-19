package com.udacity.jdnd.course3.critter.pet;

import com.google.common.collect.Lists;
import com.udacity.jdnd.course3.critter.activity.Activity;
import com.udacity.jdnd.course3.critter.date.DateVerification;
import com.udacity.jdnd.course3.critter.exception.CustomApiInvalidParameterException;
import com.udacity.jdnd.course3.critter.exception.CustomApiNoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.FieldError;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PetVerification {

    @Autowired
    private DateVerification dateVerification;

    @Autowired
    private PetActivityTypeService petActivityTypeService;

    @Autowired
    private PetService petService;

    @Autowired
    private PetTypeRepository petTypeRepository;

    public Pet verifyPet(Long petId) {
        Pet pet = petService.findById(petId);
        return pet;
    }

    public LocalDate verifyPetBirthDate(String birthDate) {
        if (birthDate != null) {
            LocalDate localDate;
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                        dateVerification.localDateFormat());
                localDate = LocalDate.parse(birthDate, formatter);
            } catch (DateTimeParseException e) {
                FieldError fieldError = new FieldError(
                        "pet",
                        "birthDate",
                        birthDate,
                        false,
                        null,
                        null,
                        "Invalid birth date.");
                throw new CustomApiInvalidParameterException(fieldError);
            }
            return localDate;
        }
        return null;
    }

    public PetType verifyPetType(String type) {
        PetType petType = petTypeRepository.findByName(type);
        if (petType == null) {
            FieldError fieldError = new FieldError(
                    "pet",
                    "type",
                    String.valueOf(type),
                    false,
                    null,
                    null,
                    "Unknown pet type.");
            throw new CustomApiInvalidParameterException(fieldError);
        } else if (! petType.isServiced()) {
            FieldError fieldError = new FieldError(
                    "pet",
                    "type",
                    String.valueOf(type),
                    false,
                    null,
                    null,
                    "Pet type currently not serviced.");
            throw new CustomApiInvalidParameterException(fieldError);
        }
        return petType;
    }

    public List<Pet> verifyPets(List<Long> pets) {
        List<FieldError> fieldErrors = new ArrayList<FieldError>();

        if (pets != null) {
            List<Pet> scheduledPets = Lists.newArrayList();
            for (Long schedulePet: pets) {
                try {
                    Pet pet = petService.findById(schedulePet);
                    scheduledPets.add(pet);
                } catch (CustomApiNoSuchElementException e) {
                    fieldErrors.add(e.getFieldError());
                }
            }
            if (! fieldErrors.isEmpty()) {
                throw new CustomApiInvalidParameterException(fieldErrors);
            }
            return scheduledPets;
        }
        return null;
    }

    public void verifyPetDetails(List<Pet> pets, List<Activity> activities, String objectName) {
        List<FieldError> fieldErrors = new ArrayList<FieldError>();

        if (pets != null) {
            List<String> activityNames = new ArrayList<String>();
            if (activities != null) {
                for (Activity activity: activities) {
                    activityNames.add(activity.getName());
                }
            }
            Long firstCustomerId = null;
            Long firstPetId = null;
            for (Pet scheduledPet : pets) {
                if (! scheduledPet.getType().isServiced()) {
                    FieldError fieldError = new FieldError(
                            "pet",
                            "type",
                            String.valueOf(scheduledPet.getType()),
                            false,
                            null,
                            null,
                            "Pet type currently not serviced.");
                    fieldErrors.add(fieldError);
                    continue;
                }
                if (firstCustomerId == null) {
                    firstCustomerId = scheduledPet.getCustomer().getId();
                    firstPetId = scheduledPet.getId();
                } else if (firstCustomerId != scheduledPet.getCustomer().getId()) {
                    FieldError fieldError = new FieldError(
                            objectName,
                            "pets",
                            "Pet ID " +
                                    String.valueOf(scheduledPet.getId()) +
                                    " : Customer ID " +
                                    String.valueOf(scheduledPet.getCustomer().getId()) +
                                    ", Pet ID " +
                                    String.valueOf(firstPetId) +
                                    " : Customer ID " +
                                    String.valueOf(firstCustomerId),                           false,
                            null,
                            null,
                            "Pets do not all belong to the same customer.");
                    fieldErrors.add(fieldError);
                }
                if (activities != null) {
                    List<PetActivityType> petActivityTypes =
                            petActivityTypeService.findByPetType(scheduledPet.getType());
                    List<String> petActivityTypeNames = new ArrayList<String>();
                    for (PetActivityType petActivityType : petActivityTypes) {
                        petActivityTypeNames.add(petActivityType.getActivity().getName());
                    }
                    for (String activityName : activityNames) {
                        if (!petActivityTypeNames.contains(activityName)) {
                            FieldError fieldError = new FieldError(
                                    objectName,
                                    "pets",
                                    "Pet Type " +
                                            String.valueOf(scheduledPet.getType().getName()) +
                                            " : Activity Type " +
                                            activityName,
                                    false,
                                    null,
                                    null,
                                    "Activity not available for indicated pet type.");
                            fieldErrors.add(fieldError);
                        }
                    }
                }
            }
            if (! fieldErrors.isEmpty()) {
                throw new CustomApiInvalidParameterException(fieldErrors);
            }
        }
    }
}
