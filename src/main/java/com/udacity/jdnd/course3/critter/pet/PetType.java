package com.udacity.jdnd.course3.critter.pet;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Nationalized;

@Getter
@NoArgsConstructor
@Entity
public class PetType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Nationalized
    @Column(length = 40, unique = true, nullable = false)
    private String name;

    @Nationalized
    @Column(length = 200, nullable = false)
    private String description;

    @Column
    private boolean serviced;
    // Set to "false" to stop adding or servicing this pet type
}