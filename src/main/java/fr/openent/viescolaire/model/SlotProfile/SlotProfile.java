package fr.openent.viescolaire.model.SlotProfile;

import fr.openent.viescolaire.core.constants.*;
import fr.openent.viescolaire.model.IModel;
import fr.openent.viescolaire.model.Person.*;
import io.vertx.core.json.JsonObject;


import java.util.*;
import java.util.stream.Collectors;

public class SlotProfile implements IModel<SlotProfile> {
    private String id;
    private String name;
    private String schoolId;
    private List<Timeslot> slots;
    private JsonObject created;
    private JsonObject modified;
    private User owner;

    public SlotProfile() {
    }

    public SlotProfile(JsonObject json) {
        this.id = json.getString(Field._ID);
        this.name = json.getString(Field.NAME);
        this.schoolId = json.getString(Field.SCHOOLID);
        this.slots = json.getJsonArray(Field.SLOTS).stream()
                .map(o -> new Timeslot((JsonObject) o))
                .collect(Collectors.toList());
        this.created = json.getJsonObject(Field.CREATED);
        this.modified = json.getJsonObject(Field.MODIFIED);
        this.owner = new User(json.getJsonObject(Field.OWNER));
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

    public String getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(String schoolId) {
        this.schoolId = schoolId;
    }

    public List<Timeslot> getSlots() {
        return slots;
    }

    public void setSlots(List<Timeslot> slots) {
        this.slots = slots;
    }

    public JsonObject getCreated() {
        return created;
    }

    public void setCreated(JsonObject created) {
        this.created = created;
    }

    public JsonObject getModified() {
        return modified;
    }

    public void setModified(JsonObject modified) {
        this.modified = modified;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    @Override
    public JsonObject toJson() {
        return new JsonObject()
                .put(Field._ID, this.id)
                .put(Field.NAME, this.name)
                .put(Field.SCHOOLID, this.schoolId)
                .put(Field.SLOTS, this.slots.stream().map(Timeslot::toJson).collect(Collectors.toList()))
                .put(Field.CREATED, this.created)
                .put(Field.MODIFIED, this.modified)
                .put(Field.OWNER, new JsonObject()
                        .put(Field.USERID, this.owner.getId())
                        .put(Field.DISPLAYNAME, this.owner.getName()));
    }

    @Override
    public boolean validate() {
        return this.id != null && !this.id.isEmpty()
                && this.name != null && !this.name.isEmpty()
                && this.schoolId != null && !this.schoolId.isEmpty()
                && this.slots != null && !this.slots.isEmpty()
                && this.owner != null;
    }
    public boolean isEquals(SlotProfile slotProfile) {
        return this.name.equals(slotProfile.name) &&
               this.schoolId.equals(slotProfile.schoolId) &&
                this.slots.stream().allMatch(slot -> slotProfile.slots.stream().anyMatch(slot::isEquals));
    }
}
