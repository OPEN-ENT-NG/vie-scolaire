package fr.openent.viescolaire.service.impl;

import fr.openent.Viescolaire;
import fr.openent.viescolaire.service.GroupeService;
import fr.wseduc.webutils.Either;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.neo4j.Neo4jResult;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.utils.StringUtils;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Arrays;

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
        query.append("MATCH (u:User {id :{userId}})-[DEPENDS]->(g:FunctionalGroup) return g ")
                .append(" UNION MATCH (u:User {id :{userId}})-[DEPENDS]->(g:ManualGroup) return g ");
        values.put("userId", userId);

        neo4j.execute(query.toString(), values, Neo4jResult.validResultHandler(handler));
    }

    @Override
    public void getClasseGroupe(String[] idGroupe, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonObject params = new JsonObject();

        query.append(" MATCH (n:FunctionalGroup)-[:IN]-(u:User{profiles:['Student']}) ")
                .append(" WHERE n.id IN {idGroupe} WITH  n, u ")
                .append(" MATCH (c:Class) WHERE c.externalId IN u.classes RETURN n.id as id_groupe, ")
                .append(" COLLECT(DISTINCT c.id) AS id_classes")
                .append(" UNION ")
                .append(" MATCH (n:ManualGroup)-[:IN]-(u:User{profiles:['Student']}) ")
                .append(" WHERE n.id IN {idGroupe} WITH  n, u ")
                .append(" MATCH (c:Class) WHERE c.externalId IN u.classes RETURN n.id as id_groupe, ")
                .append(" COLLECT(DISTINCT c.id) AS id_classes")
        ;
        params.put("idGroupe", new fr.wseduc.webutils.collections.JsonArray(Arrays.asList(idGroupe)))
                .put("idGroupe", new fr.wseduc.webutils.collections.JsonArray(Arrays.asList(idGroupe)));

        neo4j.execute(query.toString(), params, Neo4jResult.validResultHandler(handler));
    }

    @Override
    public void listUsersByGroupeEnseignementId(String groupeEnseignementId,String profile,
                                                Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        StringBuilder queryGetClass = new StringBuilder();
        JsonObject values = new JsonObject();
        query.append("MATCH (g:Group {id : {groupeEnseignementId}})<-[:IN]-(users)-[:IN]-(:ProfileGroup)-")
                .append("[DEPENDS]->(c:Class) ");
        queryGetClass.append("MATCH (users)-[:IN]-(:ProfileGroup)-")
                .append("[DEPENDS]->(c:Class {id : {groupeEnseignementId}}) ");
        if(!StringUtils.isEmpty(profile)){
            query.append(" where users.profiles =[{profile}] ");
            queryGetClass.append(" where users.profiles =[{profile}] ");
            values.put("profile", profile);
        }
        StringBuilder queryReturn = new StringBuilder().append( " RETURN users.lastName as lastName, ")
                .append(" users.firstName as firstName, users.id as id, ")
                .append(" users.login as login, users.activationCode as activationCode, users.birthDate as birthDate, ")
                .append("users.blocked as blocked, users.source as source, c.name as className, c.id as classId ORDER ")
                .append(" BY lastName, firstName ");

        query.append(queryReturn.toString());
        queryGetClass.append(queryReturn.toString());
        query.append(" UNION ")
                .append(queryGetClass.toString());
        values.put("groupeEnseignementId", groupeEnseignementId);

        neo4j.execute(query.toString(), values, Neo4jResult.validResultHandler(handler));
    }
    @Override
    public void getNameOfGroupeClasse(String idGroupe, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonObject values = new JsonObject();
        query.append("MATCH (c:`Class` {id: {groupeId} }) RETURN c.id as id,  c.name as name ");
        query.append("UNION ");
        query.append("MATCH (g:`FunctionalGroup` {id: {groupeId}}) return g.id as id, g.name as name ");
        query.append("UNION ");
        query.append("MATCH (g:`ManualGroup` {id: {groupeId}}) return g.id as id, g.name as name ");
        values.put("groupeId", idGroupe);

        neo4j.execute(query.toString(), values, Neo4jResult.validResultHandler(handler));
    }


}
