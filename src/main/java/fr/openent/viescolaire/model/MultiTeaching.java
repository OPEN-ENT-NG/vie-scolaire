package fr.openent.viescolaire.model;
import fr.openent.viescolaire.utils.ServicesHelper;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.math.BigInteger;
import java.text.ParseException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;


public class MultiTeaching extends Model implements Cloneable{

    private BigInteger id;
    private String structureId;
    private String mainTeacherId ;
    private String secondTeacherId;
    private String subjectId;
    private String classOrGroupId;
    private String startDate;
    private String endDate;
    private String enteredEndDate;
    private boolean isCoteaching;
    private boolean isVisible;
    private Map<BigInteger,String> idsAndidsGroups;

    private static Logger log =  LoggerFactory.getLogger(ServicesHelper.class);

    public MultiTeaching () {

    }

    public MultiTeaching(JsonObject o) {
        this.id = BigInteger.valueOf(o.getInteger("id"));
        this.structureId = o.getString("structure_id");
        this.mainTeacherId = o.getString("main_teacher_id");
        this.secondTeacherId = o.getString("second_teacher_id");
        this.subjectId = o.getString("subject_id");
        this.classOrGroupId = o.getString("class_or_group_id");
        this.isCoteaching = o.getBoolean("is_coteaching");
        this.isVisible = o.getBoolean("is_visible");
        this.idsAndidsGroups = new HashMap<>();
        this.idsAndidsGroups.put(this.id, this.classOrGroupId);
        if (o.getBoolean("is_coteaching").equals(Boolean.FALSE)) {
            try {
                this.startDate = o.getString("start_date");
                this.endDate = o.getString("end_date");
                this.enteredEndDate = o.getString("entered_end_date");

            } catch (DateTimeException e) {
                this.startDate = LocalDate.now().toString();
                this.endDate = LocalDate.now().toString();
                this.enteredEndDate = LocalDate.now().toString();
                log.error("error when casting date in MultiTeaching dates : startDate " + o.getString("start_date") +
                        " endDate " + o.getString("end_date") + "enteredEndDate " + o.getString("entered_end_date")
                        + " error Exception " + e);
            }

        }

    }

    public MultiTeaching clone (){

        try{
            return (MultiTeaching) super.clone();
        } catch (CloneNotSupportedException e ){
            return this;
        }
    }

    public BigInteger getId () {
        return id;
    }

    public void setId (BigInteger id) {
        this.id = id;
    }

    public String getMainTeacherId () {
        return mainTeacherId;
    }

    public void setMainTeacherId (String mainTeacherId) {
        this.mainTeacherId = mainTeacherId;
    }

    public String getSecondTeacherId () {
        return secondTeacherId;
    }

    public void setSecondTeacherId (String secondTeacherId) {
        this.secondTeacherId = secondTeacherId;
    }

    public String getStructureId () {
        return structureId;
    }

    public void setStructureId (String structureId) {
        this.structureId = structureId;
    }

    public String getSubjectId () {
        return subjectId;
    }

    public void setSubjectId (String subjectId) {
        this.subjectId = subjectId;
    }

    public String getClassOrGroupId () {
        return classOrGroupId;
    }

    public void setClassOrGroupId (String classOrGroupId) {
        this.classOrGroupId = classOrGroupId;
    }

    public String getStartDate () {
        return startDate;
    }

    public void setStartDate (String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate () {
        return endDate;
    }

    public void setEndDate (String endDate) {
        this.endDate = endDate;
    }

    public String getEnteredEndDate () {
        return enteredEndDate;
    }

    public void setEnteredEndDate (String enteredEndDate) {
        this.enteredEndDate = enteredEndDate;
    }

    public boolean isCoteaching () {
        return isCoteaching;
    }

    public void setCoteaching (boolean coteaching) {
        isCoteaching = coteaching;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public Map<BigInteger, String> getIdsAndidsGroups () {
        return idsAndidsGroups;
    }

    public void setIdsAndidsGroups ( Map<BigInteger, String> idsAndidsGroups) {
        this.idsAndidsGroups = idsAndidsGroups;
    }

    private JsonArray convertMapToJsonArray(Map<BigInteger,String> map, String key, String value){
        JsonArray jsonArray = new JsonArray();
        if(!map.isEmpty()){
            for(Map.Entry<BigInteger,String> item : map.entrySet()){
                jsonArray.add(new JsonObject().put(key,item.getKey()).put(value,item.getValue()));
            }
        }
        return jsonArray;
    }

    @Override
    public JsonObject toJsonObject() {
        JsonObject objectMultiTeaching = new JsonObject()
                .put("structure_id", this.structureId)
                .put("main_teacher_id",this.mainTeacherId)
                .put("second_teacher_id", this.secondTeacherId)
                .put("group_id", this.classOrGroupId)
                .put("subject_id", this.subjectId)
                .put("idsAndIdsGroups", this.convertMapToJsonArray(this.idsAndidsGroups,"id","idGroup"));

        if(!this.isCoteaching){
            objectMultiTeaching.put("start_date", this.startDate)
                    .put("end_date", this.endDate)
                    .put("entered_end_date", this.enteredEndDate);
        }
        objectMultiTeaching.put("isCoteaching", this.isCoteaching);
        objectMultiTeaching.put("is_visible", this.isVisible);

        return objectMultiTeaching;
    }
}
