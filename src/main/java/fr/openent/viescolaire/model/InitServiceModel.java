package fr.openent.viescolaire.model;

import fr.openent.viescolaire.core.constants.*;
import fr.openent.viescolaire.helper.*;
import io.vertx.core.json.JsonObject;

import java.util.List;

public class InitServiceModel implements IModel<InitServiceModel> {
    private String idMatiere;
    private List<String> idGroupes;
    private String idEnseignant;
    private Integer coefficient;
    private String idEtablissement;
    private String modalite;
    private Boolean evaluable;
    private Boolean isVisible;

    public InitServiceModel(JsonObject service) {
        this.idMatiere = service.getString( Field.ID_MATIERE);
        this.idGroupes = service.getJsonArray(Field.ID_GROUPES).getList();
        this.idEnseignant = service.getString(Field.ID_ENSEIGNANT);
        this.idEtablissement = service.getString(Field.ID_ETABLISSEMENT);
        this.evaluable = false;
        this.isVisible = true;
        this.coefficient = 1;
    }

    public String getIdMatiere() {
        return idMatiere;
    }

    public void setIdMatiere(String id_matiere) {
        this.idMatiere = id_matiere;
    }

    public List<String> getIdGroupes() {
        return idGroupes;
    }

    public void setIdGroupes(List<String> id_groupes) {
        this.idGroupes = id_groupes;
    }

    public String getIdEnseignant() {
        return idEnseignant;
    }

    public void setIdEnseignant(String id_enseignant) {
        this.idEnseignant = id_enseignant;
    }

    public String getIdEtablissement() {
        return idEtablissement;
    }

    public void setIdEtablissement(String id_etablissement) {
        this.idEtablissement = id_etablissement;
    }

    public String getModalite() {
        return modalite;
    }

    public void setModalite(String modalite) {
        this.modalite = modalite;
    }

    public Boolean getIsVisible() {
        return isVisible;
    }

    public JsonObject toJson() {
        return IModelHelper.toJson(this, true, true);
    }
    @Override
    public boolean validate() {
        return true;
    }
}
