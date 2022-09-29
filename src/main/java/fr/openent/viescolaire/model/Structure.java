package fr.openent.viescolaire.model;

import io.vertx.core.json.JsonObject;

public class Structure extends Model implements Cloneable, IModel<Structure> {

    private String id;
    private String name;
    private String UAI;

    public Structure(JsonObject structure) {
        this.id = structure.getString("id", null);
        this.name = structure.getString("name", null);
        this.UAI = structure.getString("UAI", null);
    }

    @Override
    public JsonObject toJsonObject() {
        return new JsonObject()
                .put("id", this.id)
                .put("name", this.name)
                .put("UAI", this.UAI);
    }

    public String getId() {
        return id;
    }

    public Structure setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUAI() {
        return UAI;
    }

    public void setUAI(String UAI) {
        this.UAI = UAI;
    }

    @Override
    public JsonObject toJson() {
        return this.toJsonObject();
    }

    @Override
    public boolean validate() {
        return false;
    }
}

