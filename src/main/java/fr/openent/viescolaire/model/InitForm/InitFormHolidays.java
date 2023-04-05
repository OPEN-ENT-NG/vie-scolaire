package fr.openent.viescolaire.model.InitForm;
import fr.openent.viescolaire.core.constants.*;
import fr.openent.viescolaire.helper.*;
import fr.openent.viescolaire.model.IModel;
import io.vertx.core.json.JsonObject;

public class InitFormHolidays implements IModel<InitFormHolidays> {
    private String system;
    private String zone;

    public InitFormHolidays() {
    }

    public InitFormHolidays(JsonObject json) {
        this.system = json.getString(Field.SYSTEM);
        this.zone = json.getString(Field.ZONE);
    }

    public String getSystem() {
        return system;
    }

    public InitFormHolidays setSystem(String system) {
        this.system = system;
        return this;
    }

    public String getZone() {
        return zone;
    }

    public InitFormHolidays setZone(String zone) {
        this.zone = zone;
        return this;
    }

    @Override
    public JsonObject toJson() {
        return IModelHelper.toJson(this, true, false);
    }

    @Override
    public boolean validate() {
        return this.system != null && !this.system.isEmpty() && this.zone != null && !this.zone.isEmpty();
    }
}

