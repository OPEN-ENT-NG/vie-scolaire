package fr.openent.viescolaire.model;

import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.NotImplementedException;

public class Structure implements Cloneable, IModel<Structure> {

    private String id;
    private String name;
    private String UAI;

    public Structure(JsonObject structure) {
        this.id = structure.getString("id", null);
        this.name = structure.getString("name", null);
        this.UAI = structure.getString("UAI", null);
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

    public String getUAI() {
        return UAI;
    }

    public void setUAI(String UAI) {
        this.UAI = UAI;
    }

    @Override
    public JsonObject toJsonObject() {
        return new JsonObject()
                .put("id", this.id)
                .put("name", this.name)
                .put("UAI", this.UAI);
    }

    @Override
    public boolean validate() {
        throw new NotImplementedException();
    }
}

