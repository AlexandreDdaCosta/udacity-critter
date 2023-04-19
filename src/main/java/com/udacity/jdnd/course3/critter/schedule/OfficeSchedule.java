package com.udacity.jdnd.course3.critter.schedule;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalTime;

@Data
@Entity
public class OfficeSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String dayOfWeek;

    @Column(nullable = false)
    private LocalTime officeOpens;

    @Column(nullable = false)
    private LocalTime officeCloses;

    @Column
    private LocalTime lunchHourRangeStart;

    @Column
    private LocalTime lunchHourRangeEnd;
}

