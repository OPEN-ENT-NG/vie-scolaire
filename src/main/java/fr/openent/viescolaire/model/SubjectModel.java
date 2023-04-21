package fr.openent.viescolaire.model;

import fr.openent.viescolaire.core.constants.*;
import fr.openent.viescolaire.helper.*;
import io.vertx.core.json.*;

public class SubjectModel implements IModel<SubjectModel> {

    private String id;
    private String label;
    private String code;
    private String source;

    public SubjectModel() {
    }

    public SubjectModel(JsonObject subject) {
        this.id = subject.getString(Field.ID);
        this.label = subject.getString(Field.LABEL);
        this.code = subject.getString(Field.CODE);
        this.source = subject.getString(Field.SOURCE);
    }

    public String getId() {
        return id;
    }

    public SubjectModel setId(String id) {
        this.id = id;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public SubjectModel setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getCode() {
        return code;
    }

    public SubjectModel setCode(String code) {
        this.code = code;
        return this;
    }

    public String getSource() {
        return source;
    }

    public SubjectModel setSource(String source) {
        this.source = source;
        return this;
    }

    @Override
    public JsonObject toJson() {
        return IModelHelper.toJson(this, true, false);
    }

    @Override
    public boolean validate() {
        return this.id != null && this.label != null && this.code != null && this.source != null;
    }
}
