package com.udacity.jdnd.course3.critter.pet;

import com.udacity.jdnd.course3.critter.activity.Activity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@Entity
@Table(uniqueConstraints={
        @UniqueConstraint(columnNames = {"pet_type", "activity"})
})
public class PetActivityType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pet_type", referencedColumnName = "name")
    private PetType petType;

    @ManyToOne
    @JoinColumn(name = "activity", referencedColumnName = "name")
    private Activity activity;

    @Column
    private Integer minutes;
    // How long should be scheduled for a given activity

    @Column
    private BigDecimal costForFirstPet;

    @Column
    private BigDecimal costForAdditionalPet;
}