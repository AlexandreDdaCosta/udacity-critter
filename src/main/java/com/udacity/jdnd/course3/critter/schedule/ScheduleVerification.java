package com.udacity.jdnd.course3.critter.schedule;

import com.udacity.jdnd.course3.critter.exception.CustomApiInvalidParameterException;
import org.springframework.stereotype.Service;
import org.springframework.validation.FieldError;

@Service
public class ScheduleVerification {

    public ScheduleStatus verifyStatus(String status, String objectName) {

        if (status == null) {
            return ScheduleStatus.PENDING;
        }
        else {
            try {
                ScheduleStatus scheduleStatus = ScheduleStatus.valueOf(status);
                return scheduleStatus;
            } catch (IllegalArgumentException e) {
                FieldError fieldError = new FieldError(
                        objectName,
                        "status",
                        String.valueOf(status),
                        false,
                        null,
                        null,
                        "Invalid status.");
                throw new CustomApiInvalidParameterException(fieldError);
            }
        }
    }
}
