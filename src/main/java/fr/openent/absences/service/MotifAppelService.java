package fr.openent.absences.service;

import fr.wseduc.webutils.Either;
import org.entcore.common.service.CrudService;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Created by anabah on 06/06/2017.
 */

public interface MotifAppelService extends CrudService {

    /**
     * Recupere tous les motifs d'absences en fonction de l'id de l'établissement.
     * @param psIdEtablissement identifiant de l'établissement.
     * @param handler handler portant le résultat de la requête.
     */
    public void getAbscMotifsAppelEtbablissement(String psIdEtablissement, Handler<Either<String, JsonArray>> handler);

    /**
     * Recupere toutes les catégories de motifs d'absences en fonction de l'id de l'établissement.
     * @param psIdEtablissement identifiant de l'établissement.
     * @param handler handler portant le résultat de la requête.
     */
    public void getCategorieAbscMotifsAppelEtbablissement(String psIdEtablissement, Handler<Either<String, JsonArray>> handler);

    /**
     * Créé un motif d'absence
     * @param motif données pour la création du motif
     * @param handler
     */
    public void createMotifAppel(JsonObject motif, final Handler<Either<String, JsonObject>> handler);


    /**
     * Met à jour un motif d'absence
     * @param motif données pour la création du motif
     * @param handler
     */
    public void updateMotifAppel(JsonObject motif, Handler<Either<String, JsonObject>> handler);

    /**
     * Met à jour une catégorie de motif d'absence
     * @param categorie données pour la création de la catégorie
     * @param handler
     */
    public void updateCategorieMotifAppel(JsonObject categorie, Handler<Either<String, JsonObject>> handler);


    /**
     * Créé une catégorie de motif d'absence
     * @param categorie données pour la création de la catégorie
     * @param handler
     */
    public void createCategorieMotifAppel(JsonObject categorie, final Handler<Either<String, JsonObject>> handler);

}

