package fr.openent.viescolaire.model.Trombinoscope;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

public class ReportException {

    private final String message;
    private final String path;
    private final List<String> audiencesConcerned;
    private final List<String> studentsConcerned;


    public ReportException(String message, String path, List<String> audiencesConcerned, List<String> studentsConcerned) {
        this.message = message;
        this.path = path;
        this.audiencesConcerned = audiencesConcerned;
        this.studentsConcerned = studentsConcerned;
    }

    public ReportException(String message, String path) {
        this(message, path, null, null);
    }

    public String message() {
        return message;
    }

    public String path() {
        return path;
    }

    public List<String> audiencesConcerned() {
        return audiencesConcerned;
    }

    public List<String> studentsConcerned() {
        return studentsConcerned;
    }

    public JsonObject toJSON() { //todo stop here
        return new JsonObject()
                .put("message", message())
                .put("path", path())
                .put("audiencesConcerned", audiencesConcerned() != null ? new JsonArray(audiencesConcerned()) : null)
                .put("studentsConcerned", studentsConcerned() != null ? new JsonArray(studentsConcerned()) : null);
    }
}
