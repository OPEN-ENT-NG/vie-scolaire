package fr.openent.viescolaire.model;

import fr.openent.viescolaire.core.constants.*;
import fr.openent.viescolaire.model.Person.*;
import io.vertx.core.json.*;

import java.util.*;
import java.util.stream.*;

public class InitTeachers implements IModel<InitTeachers> {
    private List<User> teachers;
    private Integer count;

    public InitTeachers() {
        this.teachers = new ArrayList<>();
        this.count = 0;
    }

    public InitTeachers(JsonObject json) {
        this.teachers = json.getJsonArray(Field.TEACHERS).stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .map(User::new)
                .collect(Collectors.toList());
        this.count = json.getInteger(Field.COUNT);
    }

    public List<User> getTeachers() {
        return teachers;
    }

    public InitTeachers setTeachers(List<User> teachers) {
        this.teachers = teachers;
        return this;
    }

    public Integer getCount() {
        return count;
    }

    public InitTeachers setCount(Integer count) {
        this.count = count;
        return this;
    }

    @Override
    public JsonObject toJson() {
        return new JsonObject()
                .put(Field.TEACHERS, new JsonArray(this.teachers.stream().map(User::toJSON).collect(Collectors.toList())))
                .put(Field.COUNT, this.count);
    }

    @Override
    public boolean validate() {
        return false;
    }
}