package fr.openent.viescolaire.model.InitForm;
import fr.openent.viescolaire.core.constants.*;
import fr.openent.viescolaire.helper.*;
import fr.openent.viescolaire.model.IModel;
import io.vertx.core.json.JsonObject;

public class InitFormSchoolYear implements IModel<InitFormSchoolYear> {
    private String startDate;
    private String endDate;

    public InitFormSchoolYear() {
    }

    public InitFormSchoolYear(JsonObject json) {
        this.startDate = json.getString(Field.STARTDATE);
        this.endDate = json.getString(Field.ENDDATE);
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

    @Override
    public JsonObject toJson() {
        return IModelHelper.toJson(this, true, false);
    }

    @Override
    public boolean validate() {
        return this.startDate != null && !this.startDate.isEmpty() && this.endDate != null && !this.endDate.isEmpty();
    }
}

