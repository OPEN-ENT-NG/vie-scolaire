package fr.openent.absences.service;

import fr.wseduc.webutils.Either;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Created by rollinq on 14/08/2017.
 */
public interface DeclarationService {

    /**
     * Recupere les declarations en fonction des parametres fournis
     * Si {@code psDateDebut} et {@code psDateFin} sont renseignes, filtre les declarations dont la periode
     * chevauche celle passee en parametre (dateFinDeclaration > psDateDebut et dateDebutDeclaration < psDateFin)
     * @param psEtablissementId     identifiant de l'etablissement
     * @param psOwnerId             identifiant du createur de la declaration
     * @param psStudentId           identifiant de l'etudiant concerne par la declaration
     * @param psDateDebut           date de debut de la periode de selection
     * @param psDateFin             date de fin de la periode de selection
     * @param pbTraitee             etat de la declaration
     * @param piNumber              nombre declaration a retourner
     * @param handler               handler portant le resultat de la requete
     */
    public void getDeclaration(String psEtablissementId, String psOwnerId, String psStudentId, String psDateDebut,
                               String psDateFin, Boolean pbTraitee, Integer piNumber,
                               Handler<Either<String, JsonArray>> handler);

    /**
     * Met a jour la declaration passee en parametre
     * @param poDeclaration     la declaration
     * @param handler           handler portant le resultat de la requete
     */
    public void updateDeclaration(JsonObject poDeclaration, Handler<Either<String, JsonObject>> handler);

    /**
     * Creer la declaration passee en parametre
     * @param poDeclaration     la declaration
     * @param user              l'utilisateur proprietaire de la declaration
     * @param handler           handler portant le resultat de la requete
     */
    public void createDeclaration(JsonObject poDeclaration, UserInfos user,
                                  Handler<Either<String, JsonObject>> handler);

    /**
     * Supprime la declaration dont l'id est passe en parametre
     * @param oDeclarationId    l'id de la declaration
     * @param handler           handler portant le resultat de la requete
     */
    public void deleteDeclaration(Number oDeclarationId, Handler<Either<String, JsonObject>> handler);
}
