package com.udacity.jdnd.course3.critter.schedule;

import java.util.List;

public interface CustomScheduleRepository {

    public List<Schedule> customScheduleSearch(ScheduleQuery scheduleQuery);
}
