package fr.openent.viescolaire.model;

import fr.openent.viescolaire.helper.ModelHelper;
import io.vertx.core.json.JsonObject;

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
    private List<MultiTeaching> coTeachersService;

    public ServiceModel() {
        super();
        this.evaluable = true;
        this.modalite = "S";
        this.coefficient = 1 ;
        this.isManual = false;
        this.coTeachersService = new ArrayList<>();
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

    public List<MultiTeaching> getCoteachersService () {
        return coTeachersService;
    }

    public void setCoteachersService (List<MultiTeaching> coteachersService) {

        this.coTeachersService = (coteachersService == null) ? new ArrayList<>() : coteachersService;
    }

    public void addCoteachers (List<MultiTeaching> multiTeachings ){

        if(multiTeachings!= null && !multiTeachings.isEmpty()){
            List<MultiTeaching> coteachersToAddService = multiTeachings.stream().filter(multiteaching ->
                        multiteaching.getClassOrGroupId().equals(this.idGroup) &&
                                multiteaching.getMainTeacherId().equals(this.idTeacher) &&
                                multiteaching.getSubjectId().equals(this.idTopic)

                ).collect(Collectors.toList());

            if(!coteachersToAddService.isEmpty()){
                this.coTeachersService = coteachersToAddService;
            }else{
                this.setCoteachersService(coteachersToAddService);
            }
        }else{
            this.setCoteachersService(multiTeachings);
        }
    }





    @Override
    public JsonObject toJsonObject() {
        return new JsonObject()
                .put("id_groupe",this.getIdGroup())
                .put("id_enseignant",this.getIdTeacher())
                .put("id_matiere",this.getIdTopic())
                .put("modalite",this.getModalite())
                .put("evaluable",this.isEvaluable())
                .put("coefficient",this.getCoefficient())
                .put("typeGroupe",this.getTypeGroup())
                .put("isManual",this.isManual())
                .put("coTeachers", ModelHelper.convetToJsonArray(this.coTeachersService));
    }

    public int compareTo( ServiceModel serviceModelB) {
        if (this.getIdTeacher().compareTo(serviceModelB.getIdTeacher()) == 0) {
            return this.getIdTopic().compareTo(serviceModelB.getIdTopic());
        }else{
            return this.getIdTeacher().compareTo(serviceModelB.getIdTeacher());
        }
    }
}
