package com.udacity.jdnd.course3.critter.pet;

import com.udacity.jdnd.course3.critter.activity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PetActivityTypeRepository extends JpaRepository<PetActivityType, Long> {
    List<PetActivityType> findByPetType(PetType type);
    PetActivityType findByPetTypeAndActivity(PetType type, Activity activity);
}
