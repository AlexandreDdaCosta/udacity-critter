package com.udacity.jdnd.course3.critter.schedule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OfficeScheduleService {

    @Autowired
    OfficeScheduleRepository officeScheduleRepository;


    public List<OfficeSchedule> findAll() {
        return officeScheduleRepository.findAll();
    }

    public OfficeSchedule findByDayOfWeek(String dayOfWeek) {
        return officeScheduleRepository.findByDayOfWeek(dayOfWeek);
    }
}
