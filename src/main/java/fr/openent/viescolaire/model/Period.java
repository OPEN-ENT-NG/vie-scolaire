package fr.openent.viescolaire.model;

import fr.openent.viescolaire.core.constants.*;
import fr.openent.viescolaire.helper.*;
import io.vertx.core.json.*;

public class Period implements IModel<Period> {

    private Integer id;
    private String startDate;
    private String endDate;
    private String description;
    private String idStructure;
    private String code;

    public Period(JsonObject period) {
        this.id = period.getInteger(Field.ID, null);
        this.startDate = period.getString(Field.START_DATE, null);
        this.endDate = period.getString(Field.END_DATE, null);
        this.description = period.getString(Field.DESCRIPTION, "");
        this.idStructure = period.getString(Field.ID_STRUCTURE, null);
        this.code = period.getString(Field.CODE, null);
    }

    public Period() {
        this.description = "";
    }

    public Integer getId() {
        return id;
    }

    public Period setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getStartDate() {
        return startDate;
    }

    public Period setStartDate(String startDate) {
        this.startDate = startDate;
        return this;
    }

    public String getEndDate() {
        return endDate;
    }

    public Period setEndDate(String endDate) {
        this.endDate = endDate;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Period setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getIdStructure() {
        return idStructure;
    }

    public Period setIdStructure(String idStructure) {
        this.idStructure = idStructure;
        return this;
    }

    public String getCode() {
        return code;
    }

    public Period setCode(String code) {
        this.code = code;
        return this;
    }

    @Override
    public JsonObject toJson() {
        return IModelHelper.toJson(this, true, true);
    }

    @Override
    public boolean validate() {
        return this.startDate != null && this.endDate != null && this.idStructure != null;
    }

}
