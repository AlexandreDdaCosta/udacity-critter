package com.udacity.jdnd.course3.critter.pet;

import com.udacity.jdnd.course3.critter.activity.Activity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PetActivityTypeService {

    @Autowired
    private PetActivityTypeRepository petActivityTypeRepository;

    public List<PetActivityType> findByPetType(PetType petType) {
        return petActivityTypeRepository.findByPetType(petType);
    }

    public PetActivityType findByPetTypeAndActivity(PetType petType, Activity activity) {
        return petActivityTypeRepository.findByPetTypeAndActivity(petType, activity);
    }
}
