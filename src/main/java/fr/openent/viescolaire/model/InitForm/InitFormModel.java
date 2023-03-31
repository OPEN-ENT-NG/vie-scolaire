package fr.openent.viescolaire.model.InitForm;

import fr.openent.viescolaire.core.constants.*;
import fr.openent.viescolaire.helper.*;
import fr.openent.viescolaire.model.IModel;
import io.vertx.core.json.JsonObject;

public class InitFormModel implements IModel<InitFormModel> {
    private InitFormSchoolYear schoolYear;
    private InitFormTimetable timetable;
    private InitFormHolidays holidays;
    private String initType;

    public InitFormModel() {
    }

    public InitFormModel(JsonObject json) {
        this.schoolYear = new InitFormSchoolYear(json.getJsonObject(Field.SCHOOLYEAR));
        this.timetable = new InitFormTimetable(json.getJsonObject(Field.TIMETABLE));
        this.holidays = new InitFormHolidays(json.getJsonObject(Field.HOLIDAYS));
        this.initType = json.getString(Field.INITTYPE);
    }

    public InitFormSchoolYear getSchoolYear() {
        return schoolYear;
    }

    public InitFormModel setSchoolYear(InitFormSchoolYear schoolYear) {
        this.schoolYear = schoolYear;
        return this;
    }

    public InitFormTimetable getTimetable() {
        return timetable;
    }

    public InitFormModel setTimetable(InitFormTimetable timetable) {
        this.timetable = timetable;
        return this;
    }

    public InitFormHolidays getHolidays() {
        return holidays;
    }

    public InitFormModel setHolidays(InitFormHolidays holidays) {
        this.holidays = holidays;
        return this;
    }

    public String getInitType() {
        return initType;
    }

    public InitFormModel setInitType(String initType) {
        this.initType = initType;
        return this;
    }

    @Override
    public JsonObject toJson() {
        return IModelHelper.toJson(this, true, false);
    }

    @Override
    public boolean validate() {
        return this.schoolYear.validate()
                && this.timetable.validate()
                && this.holidays.validate()
                && this.initType != null && !this.initType.isEmpty();
    }
}

