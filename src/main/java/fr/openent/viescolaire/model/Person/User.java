package fr.openent.viescolaire.model.Person;

import io.vertx.core.json.JsonObject;

public class User extends Person implements Cloneable {

    public User(String id) {
        super();
        this.id = id;
        this.firstName = null;
        this.lastName = null;
        this.displayName = null;
        this.info = null;
    }

    public User() {
        this.id =  null;
        this.firstName = null;
        this.lastName = null;
        this.displayName = null;
        this.info = null;
    }

    public User(JsonObject user) {
        this.id = user.getString("id", null);
        this.firstName = user.getString("firstName", null);
        this.lastName = user.getString("lastName", null);
        this.displayName = user.getString("displayName", null);
        this.info = user.getString("info", null);
    }

    public JsonObject toJSON() {
        return new JsonObject()
                .put("id", this.id)
                .put("firstName", this.firstName)
                .put("lastName", this.lastName)
                .put("displayName", this.displayName)
                .put("info", this.info);
    }

    @Override
    public User clone() {
        try {
            return (User) super.clone();
        } catch (CloneNotSupportedException e) {
            return this;
        }
    }
}
