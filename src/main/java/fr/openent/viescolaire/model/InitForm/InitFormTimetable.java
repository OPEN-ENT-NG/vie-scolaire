package fr.openent.viescolaire.model.InitForm;
import fr.openent.viescolaire.core.constants.*;
import fr.openent.viescolaire.core.enums.*;
import fr.openent.viescolaire.model.IModel;
import fr.openent.viescolaire.model.SlotProfile.*;
import fr.openent.viescolaire.utils.*;
import io.vertx.core.json.*;
import io.vertx.core.logging.*;

import java.util.*;
import java.util.stream.Collectors;

public class InitFormTimetable implements IModel<InitFormTimetable> {

    private static Logger log =  LoggerFactory.getLogger(InitFormTimetable.class);

    private JsonObject morning;
    private JsonObject afternoon;
    private List<DayOfWeek> fullDays;
    private List<DayOfWeek> halfDays;

    public InitFormTimetable() {
    }

    public InitFormTimetable(JsonObject json) {
        this.morning = json.getJsonObject(Field.MORNING);
        this.afternoon = json.getJsonObject(Field.AFTERNOON);
        this.fullDays = json.getJsonArray(Field.FULLDAYS).stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(DayOfWeek::getValue)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        this.halfDays = json.getJsonArray(Field.HALFDAYS).stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(DayOfWeek::getValue)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public JsonObject getMorning() {
        return morning;
    }

    public InitFormTimetable setMorning(JsonObject morning) {
        this.morning = morning;
        return this;
    }

    public JsonObject getAfternoon() {
        return afternoon;
    }

    public InitFormTimetable setAfternoon(JsonObject afternoon) {
        this.afternoon = afternoon;
        return this;
    }

    public List<DayOfWeek> getFullDays() {
        return fullDays;
    }

    public InitFormTimetable setFullDays(List<DayOfWeek> fullDays) {
        this.fullDays = fullDays;
        return this;
    }

    public List<DayOfWeek> getHalfDays() {
        return halfDays;
    }

    public InitFormTimetable setHalfDays(List<DayOfWeek> halfDays) {
        this.halfDays = halfDays;
        return this;
    }

    public List<Timeslot> getSlots(String morningPrefix, String afternoonPrefix, String lunchName) {
        List<Timeslot> slots = new ArrayList<>();

        Date start = DateHelper.parseDate(this.morning.getString(Field.STARTHOUR), DateHelper.HOUR_MINUTES);
        Date end = DateHelper.parseDate(this.afternoon.getString(Field.ENDHOUR), DateHelper.HOUR_MINUTES);
        double nbHours = Math.ceil(((double) end.getTime() - start.getTime()) / (60.0 * 60 * 1000));
        int afternoonIndex = 0;
        boolean lunchAdded = false;
        String lunchStart = null;

        String endOfMorning = null;
        String startOfAfternoon = null;

        for (int i = 0; i < nbHours; i++) {
            String currentTime = DateHelper.addHour(this.morning.getString(Field.STARTHOUR), i, DateHelper.HOUR_MINUTES);
            String nextTime = DateHelper.addHour(currentTime, 1, DateHelper.HOUR_MINUTES);

            // Add lunch timeslot
            if (!lunchAdded && DateHelper.isHourAfterOrEqual(currentTime, this.morning.getString(Field.ENDHOUR), DateHelper.HOUR_MINUTES)) {
                lunchStart = (lunchStart == null) ? currentTime : lunchStart;
                if (DateHelper.isHourAfterOrEqual(nextTime, this.afternoon.getString(Field.STARTHOUR), DateHelper.HOUR_MINUTES)) {
                    slots.add(new Timeslot(lunchName, lunchStart, nextTime));
                    lunchAdded = true;
                }
                continue;
            }

            if (!lunchAdded) {
                // Add morning timeslot
                slots.add(new Timeslot(morningPrefix + (i + 1), currentTime, nextTime));
                endOfMorning = nextTime;
            } else {
                // Add afternoon timeslot
                if (startOfAfternoon == null) {
                    startOfAfternoon = currentTime;
                }
                slots.add(new Timeslot(afternoonPrefix + (++afternoonIndex), currentTime, nextTime));
            }
        }

        // Adjust morning and afternoon start/end time in timetable from the generated slots
        this.getMorning().put(Field.ENDHOUR, endOfMorning);
        this.getAfternoon().put(Field.STARTHOUR, startOfAfternoon);

        return slots;
    }

    @Override
    public JsonObject toJson() {
        return new JsonObject()
                .put(Field.MORNING, this.morning)
                .put(Field.AFTERNOON, this.afternoon)
                .put(Field.FULLDAYS, new JsonArray(this.fullDays.stream().map(DayOfWeek::name).collect(Collectors.toList())))
                .put(Field.HALFDAYS, new JsonArray(this.halfDays.stream().map(DayOfWeek::name).collect(Collectors.toList())));
    }

    @Override
    public boolean validate() {
        return this.morning != null && this.afternoon != null && this.fullDays != null && this.halfDays != null;
    }
}

