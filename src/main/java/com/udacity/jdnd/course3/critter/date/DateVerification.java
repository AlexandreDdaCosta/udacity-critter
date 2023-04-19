package com.udacity.jdnd.course3.critter.date;

import com.udacity.jdnd.course3.critter.exception.CustomApiInvalidParameterException;
import org.springframework.stereotype.Service;
import org.springframework.validation.FieldError;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DateVerification {

    public static String localDateFormat() {
        return("yyyy/MM/dd");
    }

    public static String localTimeFormat() {
        return("HH:mm");
    }

    public LocalDate verifyDateFormat(String date, String objectName) {
        if (date != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(localDateFormat());
            try {
                LocalDate localDate = LocalDate.parse(date, formatter);
                return localDate;
            } catch (DateTimeParseException e) {
                FieldError fieldError = new FieldError(
                        objectName,
                        "date",
                        String.valueOf(date),
                        false,
                        null,
                        null,
                        "Invalid date; correct format " + localDateFormat() + ".");
                throw new CustomApiInvalidParameterException(fieldError);
            }
        }
        return null;
    }

    public List<DayOfWeek> verifyDaysOfWeek(List<String> daysOfWeek, String objectName) {
        List<DayOfWeek> availableDays = new ArrayList<DayOfWeek>();
        List<FieldError> fieldErrors = new ArrayList<FieldError>();
        for (String availableDay : daysOfWeek) {
            availableDay = availableDay.toUpperCase();
            try {
                DayOfWeek dayOfWeek = DayOfWeek.valueOf(availableDay);
                if (! availableDays.contains(dayOfWeek)) {
                    availableDays.add(dayOfWeek);
                }
            } catch (IllegalArgumentException e) {
                FieldError fieldError = new FieldError(
                        objectName,
                        "daysAvailable",
                        availableDay,
                        false,
                        null,
                        null,
                        "Invalid day of week.");
                fieldErrors.add(fieldError);
            }
        }
        if (! fieldErrors.isEmpty()) {
            throw new CustomApiInvalidParameterException(fieldErrors);
        }
        return availableDays;
    }

    public LocalTime verifyTime(String time, String objectName, String field) {
        if (time != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(localTimeFormat());
            LocalTime localTime;
            try {
                localTime = LocalTime.parse(time, formatter);
            } catch (DateTimeParseException e) {
                FieldError fieldError = new FieldError(
                        objectName,
                        field,
                        String.valueOf(time),
                        false,
                        null,
                        null,
                        "Invalid time; correct format " + localTimeFormat() + ".");
                throw new CustomApiInvalidParameterException(fieldError);
            }
            if (! time.endsWith("0") && ! time.endsWith("5")) {
                FieldError fieldError = new FieldError(
                        objectName,
                        field,
                        String.valueOf(time),
                        false,
                        null,
                        null,
                        "Times must be specified in five-minute intervals.");
                throw new CustomApiInvalidParameterException(fieldError);
            }
            return localTime;
        }
        return null;
    }
}
