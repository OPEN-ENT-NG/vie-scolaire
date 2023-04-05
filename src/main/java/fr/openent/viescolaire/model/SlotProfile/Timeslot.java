package fr.openent.viescolaire.model.SlotProfile;
import fr.openent.viescolaire.core.constants.*;
import fr.openent.viescolaire.helper.*;
import fr.openent.viescolaire.model.IModel;
import io.vertx.core.json.JsonObject;

import java.util.*;

public class Timeslot implements IModel<Timeslot> {
    private String id;
    private String name;
    private String startHour;
    private String endHour;

    public Timeslot(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
    }

    public Timeslot(String name, String startHour, String endHour) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.startHour = startHour;
        this.endHour = endHour;
    }

    public Timeslot(JsonObject json) {
        this.id = json.getString(Field.ID);
        this.name = json.getString(Field.NAME);
        this.startHour = json.getString(Field.STARTHOUR);
        this.endHour = json.getString(Field.ENDHOUR);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStartHour() {
        return startHour;
    }

    public void setStartHour(String startHour) {
        this.startHour = startHour;
    }

    public String getEndHour() {
        return endHour;
    }

    public void setEndHour(String endHour) {
        this.endHour = endHour;
    }


    @Override
    public JsonObject toJson() {
        return IModelHelper.toJson(this, true, false);
    }

    @Override
    public boolean validate() {
        return this.id != null && !this.id.isEmpty()
                && this.name != null && !this.name.isEmpty()
                && this.startHour != null && !this.startHour.isEmpty()
                && this.endHour != null && !this.endHour.isEmpty();
    }
}
