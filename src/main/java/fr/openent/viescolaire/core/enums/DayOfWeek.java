package fr.openent.viescolaire.core.enums;

import java.util.*;

public enum DayOfWeek {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY;
    public static DayOfWeek getValue(String value) {
        return getValue(value, null);
    }
    public static DayOfWeek getValue(String value, DayOfWeek defaultValue) {
        return Arrays.stream(DayOfWeek.values())
                .filter(dayOfWeek -> dayOfWeek.name().equalsIgnoreCase(value))
                .findFirst()
                .orElse(defaultValue);
    }
}