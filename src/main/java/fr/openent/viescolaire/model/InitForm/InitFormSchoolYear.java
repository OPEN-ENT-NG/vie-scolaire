package fr.openent.viescolaire.model.InitForm;
import fr.openent.viescolaire.core.constants.*;
import fr.openent.viescolaire.helper.*;
import fr.openent.viescolaire.model.*;
import fr.openent.viescolaire.utils.*;
import io.vertx.core.json.JsonObject;

import static fr.openent.viescolaire.utils.DateHelper.*;

public class InitFormSchoolYear implements IModel<InitFormSchoolYear> {
    private String startDate;
    private String endDate;

    public InitFormSchoolYear() {
    }

    public InitFormSchoolYear(JsonObject json) {
        this.startDate = DateHelper.getDateString(json.getString(Field.STARTDATE), MONGO_FORMAT_START_DAY);
        this.endDate = DateHelper.getDateString(json.getString(Field.ENDDATE), MONGO_FORMAT_END_DAY);
    }

    public String getStartDate() {
        return startDate;
    }

    public InitFormSchoolYear setStartDate(String startDate) {
        this.startDate = startDate;
        return this;
    }

    public String getEndDate() {
        return endDate;
    }

    public InitFormSchoolYear setEndDate(String endDate) {
        this.endDate = endDate;
        return this;
    }

    public Period getPeriod(String structureId) {
        return new Period()
                .setStartDate(this.startDate)
                .setEndDate(this.endDate)
                .setCode(PeriodeCode.YEAR)
                .setIdStructure(structureId);
    }

    @Override
    public JsonObject toJson() {
        return IModelHelper.toJson(this, true, false);
    }

    @Override
    public boolean validate() {
        return this.startDate != null && !this.startDate.isEmpty() && this.endDate != null && !this.endDate.isEmpty();
    }
}

