package fr.openent.viescolaire.model;

import fr.openent.viescolaire.core.constants.Field;
import fr.openent.viescolaire.helper.ModelHelper;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

public class TimeslotModel implements IModel<TimeslotModel> {
    private String id;
    private String name;
    private String structureId;
    private List<SlotModel> slots;

    public TimeslotModel() {
    }

    public TimeslotModel(JsonObject timeslot) {
        this.id = timeslot.getString(Field._ID);
        this.name = timeslot.getString(Field.NAME);
        this.structureId = timeslot.getString(Field.SCHOOLID);
        this.slots = ModelHelper.toList(timeslot.getJsonArray(Field.SLOTS, new JsonArray()), SlotModel.class);
    }

    public String getId() {
        return id;
    }

    public TimeslotModel setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public TimeslotModel setName(String name) {
        this.name = name;
        return this;
    }

    public String getStructureId() {
        return structureId;
    }

    public TimeslotModel setStructureId(String structureId) {
        this.structureId = structureId;
        return this;
    }

    public List<SlotModel> getSlots() {
        return slots;
    }

    public TimeslotModel setSlots(List<SlotModel> slots) {
        this.slots = slots;
        return this;
    }

    @Override
    public JsonObject toJson() {
        return new JsonObject()
                .put(Field._ID, this.id)
                .put(Field.NAME, this.name)
                .put(Field.SCHOOLID, this.structureId)
                .put(Field.SLOTS, ModelHelper.toJsonArray(this.slots));
    }

    @Override
    public boolean validate() {
        return false;
    }
}
