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
}
