package fr.openent.viescolaire.utils;

import fr.openent.viescolaire.model.InitForm.ZoneEnum;

import java.util.stream.Stream;

public class ZoneUtils {
    public static Boolean isValidZone(String zone) {
        return Stream.of(ZoneEnum.values()).anyMatch(z -> z.zone().equals(zone));
    }
}
