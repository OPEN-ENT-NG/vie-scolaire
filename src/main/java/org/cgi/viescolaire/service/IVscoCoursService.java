package org.cgi.viescolaire.service;

import fr.wseduc.webutils.Either;
import org.entcore.common.service.CrudService;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;

/**
 * Created by ledunoiss on 10/02/2016.
 */
public interface IVscoCoursService extends CrudService{
    /**
     * Récupération des cours d'une classe en fonction d'une date de début et d'une date de fin.
     * @param pSDateDebut Date de début de la période
     * @param pSDateFin Date de fin de la période
     * @param pSIdClasse Identifiant de la classe
     * @param handler Handler de retour
     */
    public void getClasseCours(String pSDateDebut, String pSDateFin, String pSIdClasse, Handler<Either<String, JsonArray>> handler);

    /**
     * Récupération des cours d'un enseignant en fonction d'une date de début et d'une date de fin.
     * @param pSDateDebut Date de début de la période
     * @param pSDateFin Date de fin de la période
     * @param psUserId Identifiant de l'enseignant
     * @param handler Handler de retour
     */
    public void getCoursByUserId(String pSDateDebut, String pSDateFin, String psUserId, Handler<Either<String, JsonArray>> handler);
}
