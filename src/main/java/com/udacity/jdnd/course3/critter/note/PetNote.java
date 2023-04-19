package com.udacity.jdnd.course3.critter.note;

import com.udacity.jdnd.course3.critter.pet.Pet;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class PetNote extends Note {

    @ManyToOne(targetEntity = Pet.class, optional = false)
    @JoinColumn(name = "pet_id")
    private Pet pet;

    public Pet getPet() {
        return pet;
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }

    public PetNote validate() {
        super.validate();
        return this;
    }
}
