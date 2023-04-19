package com.udacity.jdnd.course3.critter.schedule;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OfficeScheduleRepository extends JpaRepository<OfficeSchedule, Long> {

    OfficeSchedule findByDayOfWeek(String dayOfWeek);
}
