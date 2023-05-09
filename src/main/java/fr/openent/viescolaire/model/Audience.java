package fr.openent.viescolaire.model;

import fr.openent.viescolaire.core.constants.*;
import fr.openent.viescolaire.helper.*;
import io.vertx.core.json.*;

public class Audience implements IModel<Audience> {

    private String id;
    private String externalId;
    private String name;

    public Audience() {
    }

    public Audience(JsonObject audience) {
        this.id = audience.getString(Field.ID, "");
        this.externalId = audience.getString(Field.EXTERNALID, "");
        this.name = audience.getString(Field.NAME, "");
    }

    public String getId() {
        return id;
    }

    public Audience setId(String id) {
        this.id = id;
        return this;
    }

    public String getExternalId() {
        return externalId;
    }

    public Audience setExternalId(String externalId) {
        this.externalId = externalId;
        return this;
    }

    public String getName() {
        return name;
    }

    public Audience setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public JsonObject toJson() {
        return IModelHelper.toJson(this, true, false);
    }

    @Override
    public boolean validate() {
        return this.id != null && this.externalId != null && this.name != null;
    }

}
