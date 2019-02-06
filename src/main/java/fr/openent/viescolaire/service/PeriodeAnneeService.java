package fr.openent.viescolaire.service;

import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Date;

public interface PeriodeAnneeService {
    /**
     * Récupère la periode de l'année scolaire en cours
     * @param structure id de l'établissement
     * @param handler handler returning result
     */

    void getPeriodeAnnee(String structure, Handler<Either<String, JsonObject>> handler);

    /**
     * List all period exclusions in database based on structure id
     *
     * @param structureId structure id
     * @param handler handler returning query result
     */
    void listExclusion(String structureId, Handler<Either<String, JsonArray>> handler);

    /**
     * Create periode annee scolaire
     * @param periode  id de l'etablissement
     * @param isOpening inclusion or exclusion
     * @param handler handler returning result
     */
    void createPeriode(JsonObject periode, boolean isOpening, Handler<Either<String, JsonArray>> handler);

    /**
     * Update an exclusion based on id
     * @param id period id to update
     * @param periode period to update
     * @param isOpening inclusion or exclusion
     * @param handler handler returning result
     */
    void updatePeriode (Integer id, JsonObject periode, boolean isOpening, Handler<Either<String, JsonArray>> handler);

    /**
     * Delete provided exclusion
     * @param exclusionId exclusion to delete
     * @param result handler returning result
     */
    void deleteExclusion(Integer exclusionId, Handler<Either<String, JsonArray>> result);

}




