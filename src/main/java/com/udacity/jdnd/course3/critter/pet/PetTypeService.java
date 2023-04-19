package com.udacity.jdnd.course3.critter.pet;

import com.udacity.jdnd.course3.critter.activity.Activity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PetTypeService {

    @Autowired
    private PetTypeRepository petTypeRepository;

    public PetType findByName(String petTypeName) {
        return petTypeRepository.findByName(petTypeName);
    }
}
