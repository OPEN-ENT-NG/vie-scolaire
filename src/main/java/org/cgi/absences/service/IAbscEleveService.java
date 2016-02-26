package org.cgi.absences.service;

import fr.wseduc.webutils.Either;
import org.entcore.common.service.CrudService;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;

public interface IAbscEleveService extends CrudService {

    /**
     * Récupération des évenements (absence/retard) d'un élève pour une période donnée
     * @param psIdEleve identifiant de l'élève
     * @param psDateDebut date de debut
     * @param psDateFin date de fin
     * @param handler Handler de retour
     */
    public void getEvenements(String psIdEleve, String psDateDebut, String psDateFin,
                              Handler<Either<String, JsonArray>> handler);

    /**
     * Réccupération des absences prévisionnelles d'un élève
     * @param psIdEleve identifiant de l'élève
     * @param handler Handler de retour
     */
    public void getAbsencesPrev(String psIdEleve, Handler<Either<String, JsonArray>> handler);

    /**
     * Réccupération des absences prévisionnelles d'un élève pour une période donnée
     * @param psIdEleve identifiant de l'élève
     * @param psDateDebut date de debut
     * @param psDateFin date de fin
     * @param handler Handler de retour
     */
    public void getAbsencesPrev(String psIdEleve, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler);

    /**
     * Récupérationldes absences de tous les élèves sur une période donnée
     * @param psIdEtablissement identifiant de l'établissement concerné
     * @param psDateDebut date de début de la période
     * @param psDateFin date de fin de la période
     * @param handler handler portant le résultat de la requête
     */
    public void getAbsences(String psIdEtablissement, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler);

    /**
     * Récupération les absences sans motifs de tous les élèves sur une période donnée
     * @param psIdEtablissement identifiant de l'établissement concerné
     * @param psDateDebut date de début de la période
     * @param psDateFin date de fin de la période
     * @param handler handler portant le résultat de la requête
     */
    public void getAbsencesSansMotifs(String psIdEtablissement, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler);
}
