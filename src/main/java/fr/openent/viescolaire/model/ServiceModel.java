package fr.openent.viescolaire.model;

import fr.openent.viescolaire.helper.ModelHelper;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ServiceModel extends Model implements Cloneable{
    private String idTopic;
    private String idTeacher;
    private String idGroup;
    private String modalite;
    private boolean evaluable;
    private long coefficient;
    private boolean isManual;
    private String typeGroup;
    private boolean isVisible;
    private List<MultiTeaching> substituteTeachers;
    private List<MultiTeaching> coTeachers;
    private JsonArray id_groups;

    public ServiceModel() {
        super();
        this.evaluable = false;
        this.modalite = "S";
        this.coefficient = 1 ;
        this.isManual = false;
        this.coTeachers = new ArrayList<>();
        this.substituteTeachers = new ArrayList<>();
        this.isVisible = true;
    }

    @Override
    public ServiceModel clone(){
        try {
            return (ServiceModel) super.clone();
        } catch (CloneNotSupportedException e) {
            return this;
        }
    }

    public String getIdTopic() {
        return idTopic;
    }

    public void setIdTopic(String idTopic) {
        this.idTopic = idTopic;
    }

    public String getIdTeacher() {
        return idTeacher;
    }

    public void setIdTeacher(String idTeacher) {
        this.idTeacher = idTeacher;
    }

    public String getIdGroup() {
        return idGroup;
    }

    public void setIdGroup(String idGroup) {
        this.idGroup = idGroup;
    }

    public String getModalite() {
        return modalite;
    }

    public void setModalite(String modalite) {
        this.modalite = modalite;
    }

    public boolean isEvaluable() {
        return evaluable;
    }

    public void setEvaluable(boolean evaluable) {
        this.evaluable = evaluable;
    }

    public long getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(long coefficient) {
        this.coefficient = coefficient;
    }

    public boolean isManual() {
        return isManual;
    }

    public void setManual(boolean manual) {
        isManual = manual;
    }

    public String getTypeGroup() {
        return typeGroup;
    }

    public void setTypeGroup(String typeGroup) {
        this.typeGroup = typeGroup;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public List<MultiTeaching> getCoteachers () {
        return coTeachers;
    }

    public void setCoteachersService (List<MultiTeaching> coteachers) {
        this.coTeachers = (coteachers == null) ? new ArrayList<>() : coteachers;
    }

    public void setSubstituteTeachers(List<MultiTeaching> substituteTeachers) throws ParseException {
        this.substituteTeachers = (substituteTeachers == null) ? new ArrayList<>() : substituteTeachers;
    }

    public JsonArray getId_groups () {
        return id_groups;
    }

    public void setId_groups (Boolean compressed) {
       this.id_groups = (compressed) ? null : new JsonArray().add(this.idGroup);
    }

    public List<MultiTeaching> getSubstituteTeachers(){
        return substituteTeachers;
    }

    public List<ServiceModel> addCoteachers (List<MultiTeaching> multiTeachings) {
        List<ServiceModel> newServices = new ArrayList<>();
        if (multiTeachings != null && !multiTeachings.isEmpty()) {
            List<MultiTeaching> coteachersToAddService = multiTeachings.stream().filter(multiteaching ->
                    multiteaching.getClassOrGroupId().equals(this.idGroup) &&
                            multiteaching.getMainTeacherId().equals(this.idTeacher) &&
                            multiteaching.getSubjectId().equals(this.idTopic) &&
                            multiteaching.isCoteaching()
            ).collect(Collectors.toList());
            List<MultiTeaching> substituteTeacherToAddService =  multiTeachings.stream().filter(multiteaching ->
                    multiteaching.getClassOrGroupId().equals(this.idGroup) &&
                            multiteaching.getMainTeacherId().equals(this.idTeacher) &&
                            multiteaching.getSubjectId().equals(this.idTopic) &&
                            !multiteaching.isCoteaching()
            ).collect(Collectors.toList());

            if (!coteachersToAddService.isEmpty()) {
                this.coTeachers = coteachersToAddService;
            }

            if(!substituteTeacherToAddService.isEmpty()) {
                this.substituteTeachers = substituteTeacherToAddService;
                /*for(int i=0; i < substituteTeacherToAddService.size(); i++){
                    if(i == 0) {
                        this.substituteTeacher = substituteTeacherToAddService.get(i);
                    } else {
                        ServiceModel newService = this.clone();
                        newService.substituteTeacher = substituteTeacherToAddService.get(i);
                        newServices.add(newService);
                    }
                }*/
            }
        }
        return newServices;
    }

    @Override
    public JsonObject toJsonObject() {
        JsonObject oService = new JsonObject()
                .put("id_groupe", this.getIdGroup())
                .put("id_enseignant", this.getIdTeacher())
                .put("id_matiere", this.getIdTopic())
                .put("modalite", this.getModalite())
                .put("evaluable", this.isEvaluable())
                .put("coefficient", this.getCoefficient())
                .put("typeGroupe", this.getTypeGroup())
                .put("is_visible", this.isVisible())
                .put("is_manual", this.isManual())
                .put("coTeachers", ModelHelper.convertToJsonArray(this.coTeachers))
                .put("substituteTeachers", ModelHelper.convertToJsonArray(this.substituteTeachers));
        if(this.id_groups != null) oService.put("id_groups", this.getId_groups());
        return oService;
    }

    public int compareTo(ServiceModel serviceModelB) {
        if (this.getIdTeacher().compareTo(serviceModelB.getIdTeacher()) == 0) {
            return this.getIdTopic().compareTo(serviceModelB.getIdTopic());
        } else {
            return this.getIdTeacher().compareTo(serviceModelB.getIdTeacher());
        }
    }
}
