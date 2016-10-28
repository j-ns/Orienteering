package com.jns.orienteering.model.repo;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TimeStampCreator {

    private TimeStampCreator() {
    }

    public static long timeStamp() {
        return timeStamp(ZonedDateTime.now(ZoneId.systemDefault()));
    }

    public static long timeStamp(LocalDate date) {
        return timeStamp(date.atStartOfDay(ZoneId.systemDefault()));
    }

    public static long timeStamp(ZonedDateTime dateTime) {
        long epochDay = dateTime.toLocalDate().toEpochDay();
        long secs = epochDay * 86400 + dateTime.toLocalTime().toSecondOfDay();
        secs -= dateTime.getOffset().getTotalSeconds();
        return secs;
    }

}
