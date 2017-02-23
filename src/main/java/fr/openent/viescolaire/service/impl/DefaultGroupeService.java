package fr.openent.viescolaire.service.impl;

import fr.openent.Viescolaire;
import fr.openent.viescolaire.service.GroupeService;
import fr.wseduc.webutils.Either;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.neo4j.Neo4jResult;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.utils.StringUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Created by vogelmt on 13/02/2017.
 */
public class DefaultGroupeService extends SqlCrudService implements GroupeService {

    private final Neo4j neo4j = Neo4j.getInstance();

    public DefaultGroupeService () {
        super(Viescolaire.VSCO_SCHEMA, Viescolaire.VSCO_MATIERE_TABLE);
    }


    @Override
    public void listGroupesEnseignementsByUserId(String userId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonObject values = new JsonObject();

        query.append("MATCH (u:User {id :{userId}})-[DEPENDS]->(g:FunctionalGroup) return g");
        values.putString("userId", userId);

        neo4j.execute(query.toString(), values, Neo4jResult.validResultHandler(handler));
    }


    @Override
    public void listUsersByGroupeEnseignementId(String groupeEnseignementId,String profile, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonObject values = new JsonObject();
        query.append("MATCH (g:FunctionalGroup {id : {groupeEnseignementId}})<-[:IN]-(users) ");
        if(!StringUtils.isEmpty(profile)){
            query.append("where users.profiles =[{profile}] ");
            values.putString("profile", profile);
        }
        query.append( "RETURN users.lastName as lastName, users.firstName as firstName, users.id as id, users.login as login, users.activationCode as activationCode, users.birthDate as birthDate, users.blocked as blocked, users.source as source ORDER BY lastName");
        values.putString("groupeEnseignementId", groupeEnseignementId);

        neo4j.execute(query.toString(), values, Neo4jResult.validResultHandler(handler));
    }

}
