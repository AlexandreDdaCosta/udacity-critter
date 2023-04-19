package com.udacity.jdnd.course3.critter.activity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Nationalized;

@Getter
@NoArgsConstructor
@Entity
public class Activity {

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
    private boolean concurrent;
    // Tells whether a given activity can be done at concurrently
    // for more than one of the same pet
}
