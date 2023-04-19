package com.udacity.jdnd.course3.critter.activity;

import com.google.common.collect.Lists;
import com.udacity.jdnd.course3.critter.exception.CustomApiInvalidParameterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.List;

@Service
public class ActivityVerification {

    @Autowired
    private ActivityService activityService;

    public List<Activity> verifyActivities(List<String> activities, String objectName) {
        List<FieldError> fieldErrors = new ArrayList<FieldError>();

        if (activities != null) {
            List<Activity> scheduledActivities = Lists.newArrayList();
            for (String scheduleActivity : activities) {
                Activity activity = activityService.findByName(scheduleActivity);
                if (activity == null) {
                    FieldError fieldError = new FieldError(
                            objectName,
                            "activities",
                            String.valueOf(scheduleActivity),
                            false,
                            null,
                            null,
                            "Unknown activity.");
                    fieldErrors.add(fieldError);
                } else if (!scheduledActivities.contains(activity)) {
                    scheduledActivities.add(activity);
                }
            }
            if (! fieldErrors.isEmpty()) {
                throw new CustomApiInvalidParameterException(fieldErrors);
            }
            if (scheduledActivities.size() > 0) {
                return scheduledActivities;
            } else {
                return null;
            }
        }
        return null;
    }
}
