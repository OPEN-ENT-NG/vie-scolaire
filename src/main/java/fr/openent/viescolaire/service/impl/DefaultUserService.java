/*
 * Copyright (c) Région Hauts-de-France, Département de la Seine-et-Marne, Région Nouvelle Aquitaine, Mairie de Paris, CGI, 2016.
 * This file is part of OPEN ENT NG. OPEN ENT NG is a versatile ENT Project based on the JVM and ENT Core Project.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation (version 3 of the License).
 * For the sake of explanation, any module that communicate over native
 * Web protocols, such as HTTP, with OPEN ENT NG is outside the scope of this
 * license and could be license under its own terms. This is merely considered
 * normal use of OPEN ENT NG, and does not fall under the heading of "covered work".
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package fr.openent.viescolaire.service.impl;

import fr.openent.Viescolaire;
import fr.openent.viescolaire.core.constants.*;
import fr.openent.viescolaire.service.UserService;
import fr.openent.viescolaire.service.UtilsService;
import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.neo4j.Neo4jResult;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.entcore.common.sql.SqlStatementsBuilder;
import org.entcore.common.user.UserInfos;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static fr.wseduc.webutils.Utils.handlerToAsyncHandler;

/**
 * Created by ledunoiss on 08/11/2016.
 */
public class DefaultUserService extends SqlCrudService implements UserService {

    private EventBus eb;
    private final Neo4j neo4j = Neo4j.getInstance();
    private final UtilsService utilsService;
    private static final Logger log = LoggerFactory.getLogger(DefaultUserService.class);

    public DefaultUserService() {
        super(Viescolaire.VSCO_SCHEMA, null);
        this.eb = eb;
        utilsService = new DefaultUtilsService();
    }

    public DefaultUserService(EventBus eb) {
        super(Viescolaire.VSCO_SCHEMA, null);
        this.eb = eb;
        utilsService = new DefaultUtilsService();
    }

    @Override
    public void getUserId(UserInfos user, Handler<Either<String, JsonObject>> handler) {
        StringBuilder query = new StringBuilder()
                .append("SELECT id FROM " + Viescolaire.VSCO_SCHEMA);
        switch (user.getType()) {
            case "Teacher":
            case "Personnel": {
                query.append(".personnel");
            }
            break;
            case "Relative": {
                query.append(".parent");
            }
            break;
            case "Student": {
                query.append(".eleve");
            }
            break;
        }
        query.append(" WHERE fk4j_user_id = ?;");

        Sql.getInstance().prepared(query.toString(), new fr.wseduc.webutils.collections.JsonArray().add(user.getUserId()),
                SqlResult.validUniqueResultHandler(handler));
    }

    @Override
    public void getStructures(UserInfos user, Handler<Either<String, JsonArray>> handler) {
        List<String> structures = user.getStructures();
        StringBuilder query = new StringBuilder()
                .append("SELECT * FROM " + Viescolaire.VSCO_SCHEMA + ".structure " +
                        "WHERE structure.fk4j_structure_id IN " + Sql.listPrepared(structures.toArray()));
        JsonArray params = new fr.wseduc.webutils.collections.JsonArray();
        for (int i = 0; i < structures.size(); i++) {
            params.add(structures.get(i));
        }
        Sql.getInstance().prepared(query.toString(), params, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getClasses(UserInfos user, Handler<Either<String, JsonArray>> handler) {
        List<String> classes = user.getClasses();
        JsonArray params = new fr.wseduc.webutils.collections.JsonArray();
        StringBuilder query = new StringBuilder()
                .append("SELECT * FROM " + Viescolaire.VSCO_SCHEMA + ".classe " +
                        "WHERE classe.fk4j_classe_id IN " + Sql.listPrepared(classes.toArray()));
        for (int i = 0; i < classes.size(); i++) {
            params.add(classes.get(i));
        }
        Sql.getInstance().prepared(query.toString(), params, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getMatiere(UserInfos user, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray params = new fr.wseduc.webutils.collections.JsonArray();
        switch (user.getType()) {
            case "Teacher" : {
                query.append("SELECT matiere.* " +
                        "FROM " + Viescolaire.VSCO_SCHEMA + ".matiere " +
                        "INNER JOIN " + Viescolaire.VSCO_SCHEMA + ".personnel ON (personnel.id = matiere.id_professeur)" +
                        "WHERE personnel.fk4j_user_id = ?");
                params.add(user.getUserId());
            }
            break;
            case "Eleve" : {
                List<String> classes = user.getClasses();
                query.append("SELECT matiere.* " +
                        "FROM " + Viescolaire.VSCO_SCHEMA + ".matiere INNER JOIN " + Viescolaire.VSCO_SCHEMA + ".personnel ON (matiere.id_professeur = personnel.id) " +
                        "INNER JOIN " + Viescolaire.VSCO_SCHEMA + ".rel_personnel_classe ON (personnel.id = rel_personnel_classe.id_personnel) " +
                        "INNER JOIN " + Viescolaire.VSCO_SCHEMA + ".classe ON (rel_personnel_classe.id_classe = classe.id) " +
                        "WHERE classe.externalid IN " + Sql.listPrepared(classes.toArray()));
                for (int i = 0; i < classes.size(); i++) {
                    params.add(classes.get(i));
                }
            }
            break;
            default : {
                handler.handle(new Either.Right<String, JsonArray>(new fr.wseduc.webutils.collections.JsonArray()));
            }
        }
        Sql.getInstance().prepared(query.toString(), params, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getMoyenne(String idEleve, Long[] idDevoirs, final Handler<Either<String, JsonObject>> handler) {
        JsonObject action = new JsonObject()
                .put("action", "note.getNotesParElevesParDevoirs")
                .put("idEleves", new fr.wseduc.webutils.collections.JsonArray().add(idEleve))
                .put("idDevoirs", new fr.wseduc.webutils.collections.JsonArray(Arrays.asList(idDevoirs)));

        eb.send(Viescolaire.COMPETENCES_BUS_ADDRESS, action, handlerToAsyncHandler(new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> message) {
                if ("ok".equals(message.body().getString("status"))) {
                    JsonArray notes = new fr.wseduc.webutils.collections.JsonArray();
                    JsonArray listNotes = message.body().getJsonArray("results");

                    for (int i = 0; i < listNotes.size(); i++) {

                        JsonObject note = listNotes.getJsonObject(i);

                        JsonObject noteDevoir = new JsonObject()
                                .put("valeur", Double.valueOf(note.getString("valeur")))
                                .put("diviseur", Double.valueOf(note.getString("diviseur")))
                                .put("ramenerSur", note.getBoolean("ramener_sur"))
                                .put("coefficient", Double.valueOf(note.getString("coefficient")));

                        notes.add(noteDevoir);
                    }

                    Either<String, JsonObject> result = null;

                    if (notes.size() > 0) {
                        JsonObject action = new JsonObject()
                                .put("action", "note.calculMoyenne")
                                .put("listeNoteDevoirs", notes)
                                .put("statistiques", false)
                                .put("diviseurM", 20);

                        eb.send(Viescolaire.COMPETENCES_BUS_ADDRESS, action, handlerToAsyncHandler(new Handler<Message<JsonObject>>() {
                            @Override
                            public void handle(Message<JsonObject> message) {
                                Either<String, JsonObject> result = null;
                                if ("ok".equals(message.body().getString("status"))) {
                                    result = new Either.Right<String, JsonObject>(message.body().getJsonObject("result"));
                                    handler.handle(result);
                                } else {
                                    result = new Either.Left<>(message.body().getString("message"));
                                    handler.handle(result);
                                }
                            }
                        }));
                    } else {
                        result = new Either.Right<>(new JsonObject());
                    }
                    handler.handle(result);
                } else {
                    handler.handle(new Either.Left<String, JsonObject>(message.body().getString("message")));
                }
            }
        }));
    }

    @Override
    public void parseUsersData(JsonArray users, Handler<Either<String, JsonArray>> handler){
        List<String> idUsers = new LinkedList<String>();
        List<String> externalIdGroups = new LinkedList<String>();
        JsonObject newClasses = new JsonObject();

        for (Object u : users) {
            newClasses.put(((JsonObject) u).getString("userId"), ((JsonObject) u).getJsonArray("classes"));
            idUsers.add(((JsonObject) u).getString("userId"));
            JsonArray allClasses = new JsonArray();
            // On ajoute les classes de l'utilisateur si non null
            JsonArray jsonArrayClasses = ((JsonObject) u).getJsonArray("classes");
            if(null != jsonArrayClasses
                    && jsonArrayClasses.size() > 0){
                allClasses.addAll(jsonArrayClasses);
            }
            // On ajoute les anciennes classes de l'utilisateur si non null
            JsonArray jsonArrayOldClasses = ((JsonObject) u).getJsonArray("oldClasses");
            if(null != jsonArrayOldClasses
                    && jsonArrayOldClasses.size() > 0){
                allClasses.addAll(jsonArrayOldClasses);
            }
            for(Object c : allClasses){
                if(!externalIdGroups.contains((String) c))
                    externalIdGroups.add((String) c);
            }
        }
        log.info("getUsers START");
        getUsers(idUsers, new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> event) {
                log.info("getUsers END");
                if (event.isRight()) {

                    Map<String, JsonObject> usersMap = new HashMap<String, JsonObject>();
                    JsonArray infosUsers = event.right().getValue();
                    for (Object u : infosUsers) {
                        JsonObject user = (JsonObject) u;
                        usersMap.put(user.getString("id"), user); // ajoute un user dans userMap idUser -> user
                        JsonArray classeEnTrop = new JsonArray();

                        //on verifie que les nouvelles classes de l'utilisateur envoyées par l'evenement
                        // existent bien sur l'utilisateur dans Neo4J
                        for (Object nouvelleClasse : newClasses.getJsonArray(user.getString("id"))) {

                            if(!user.getJsonArray("currentGroupExternalIds").contains(nouvelleClasse) && !user.getJsonArray("currentClassExternalIds").contains(nouvelleClasse)){
                                classeEnTrop.add(nouvelleClasse);
                            }
                        }
                        for (Object d : classeEnTrop) {
                            ((JsonArray) newClasses.getJsonArray(user.getString("id"))).remove((String)d);
                            // TODO message erreur /enregistrer erreur en bdd
                        }
                    }
                    utilsService.getIdGroupByExternalId(externalIdGroups, new Handler<Either<String, JsonArray>>() {
                        @Override
                        public void handle(Either<String, JsonArray> event) {
                            if (event.isRight()) {
                                Map<String, Object> classIdsMap = new HashMap<String, Object>();
                                JsonArray arrayIdExternalIdClasses = event.right().getValue();
                                // on stock dans une map l''association id/externalId
                                for (Object c : arrayIdExternalIdClasses) {
                                    final JsonObject classId = (JsonObject) c;
                                    classIdsMap.put(classId.getString("externalId"),
                                            classId.getString("id"));
                                }

                                JsonArray dataUsers = new JsonArray();

                                for (Object o : users) {
                                    final JsonObject u = (JsonObject) o;
                                    String userId = u.getString("userId");

                                    JsonArray oldClassesExternalIds = u.getJsonArray("oldClasses");
                                    JsonArray newClassesExternalIds = newClasses.getJsonArray(userId);

                                    //classes à supprimer (qui n'existent pas dans neo4j)
                                    JsonArray classesEnTrop = new JsonArray();

                                    // listes d'ids neo4j des anciennes et nouvelles classes
                                    JsonArray oldClassesIds = new JsonArray();
                                    JsonArray newClassesIds = new JsonArray();

                                    for(Object classe : newClassesExternalIds){
                                        //si la nouvelle classe était déjà dans les anciennes
                                        // ce n'est pas ré
                                        if(null != oldClassesExternalIds && oldClassesExternalIds.contains(classe)) {
                                            oldClassesExternalIds.remove(classe);
                                            classesEnTrop.add(classe);
                                        } else {
                                            if(classIdsMap.containsKey(classe))
                                                newClassesIds.add(classIdsMap.get(classe));
                                        }
                                    }


                                    for (Object classeSupp : classesEnTrop) {
                                        newClassesExternalIds.remove(classeSupp);
                                    }

                                    if(null != oldClassesExternalIds){
                                        for(Object classeExternalId : oldClassesExternalIds){
                                            if(classIdsMap.containsKey(classeExternalId))
                                                oldClassesIds.add(classIdsMap.get(classeExternalId));
                                        }
                                    }


                                    if(null != newClassesIds && newClassesIds.size() > 0){
                                        JsonObject user = usersMap.get(userId);
                                        String type = user.getJsonArray("type").getString(0);
                                        user.remove("type");
                                        user.put("type", type);
                                        user.put("classIds", oldClassesIds);
                                        user.put("newClassIds", newClassesIds);
                                        user.put("deleteDate", u.getLong("timestamp"));
                                        dataUsers.add(user);
                                    }
                                }
                                handler.handle(new Either.Right<String, JsonArray>(dataUsers));
                            } else {
                                handler.handle(new Either.Left<>("Error message"));
                            }
                        }
                    });
                } else {
                    handler.handle(new Either.Left<>("Error message"));
                }
            }
        });
    }

    @Override
    public void createPersonnesSupp(JsonArray users, Handler<Either<String, JsonObject>> handler) {
        log.info("createPersonnesSupp START");
        SqlStatementsBuilder statements = new SqlStatementsBuilder();
        final AtomicInteger oNbUsers = new AtomicInteger(users.size());
        for (Object u : users) {
            final JsonObject user = (JsonObject) u;
            boolean hasClassIds = user.containsKey("classIds") && user.getJsonArray("classIds").size() > 0;
            boolean hasFunctionalGroupsIds = user.containsKey("functionalGroupsIds") && user.getJsonArray("functionalGroupsIds").size() > 0;
            boolean hasManualGroupsIds = user.containsKey("manualGroupsIds") && user.getJsonArray("manualGroupsIds").size() > 0;

            // on ne supprime l'eleve qui s'il avait des anciennes classes
            if (!validProfile((JsonObject) u) || (!hasClassIds && !hasFunctionalGroupsIds && !hasManualGroupsIds)) {
                oNbUsers.decrementAndGet();
                // execute transaction when statements of all user are build
                if(oNbUsers.intValue() == 0) {
                    if(statements.build().isEmpty()) {
                        log.info("0 user supp");
                        handler.handle(new Either.Right<>(null));
                        return;
                    } else {
                        Sql.getInstance().transaction(statements.build(), SqlResult.validRowsResultHandler(handler));
                    }
                    log.info("createPersonnesSupp END");
                } else {
                    continue;
                }
            }

            // Insert user in the right table
            final String queryNewCours =
                    "SELECT nextval('" + Viescolaire.VSCO_SCHEMA + ".personnes_supp_id_seq') as id";

            sql.raw(queryNewCours, SqlResult.validUniqueResultHandler(new Handler<Either<String, JsonObject>>() {
                @Override
                public void handle(Either<String, JsonObject> event) {
                    oNbUsers.decrementAndGet();
                    log.info("oNbUsers : " + oNbUsers.get());
                    if (event.isRight()) {
                        Long idPersonneSupp = event.right().getValue().getLong("id");


                        if (hasClassIds || hasFunctionalGroupsIds || hasManualGroupsIds) {
                            String uQuery =
                                    "INSERT INTO " + Viescolaire.VSCO_SCHEMA + ".personnes_supp(id, id_user, " +
                                            "display_name, user_type, " +
                                            "first_name, last_name, delete_date, birth_date) " +
                                            "VALUES (?, ?, ?, ?, ?, ?, to_timestamp(?), ? ) " +
                                            "ON CONFLICT (id) DO NOTHING; ";
                            JsonArray uParams = new fr.wseduc.webutils.collections.JsonArray()
                                    .add(idPersonneSupp)
                                    .add(user.getString("id"))
                                    .add(user.getString("displayName"))
                                    .add(user.getString("type"))
                                    .add(user.getString("firstName"))
                                    .add(user.getString("lastName"))
                                    .add((float) user.getLong("deleteDate") / 1000)
                                    .add(user.getString("birthDate"));

                            statements.prepared(uQuery, uParams);
                        }

                        if(user.getString("type").equals("Teacher")){
                            String query = "DELETE FROM " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.SERVICES_TABLE +
                                    " WHERE id_enseignant=?";
                            JsonArray uParams = new fr.wseduc.webutils.collections.JsonArray()
                                    .add(user.getString("id"));
                            statements.prepared(query, uParams);
                        }

                        if (hasClassIds) {
                            formatGroups(user.getJsonArray("classIds"),idPersonneSupp, statements,
                                    Viescolaire.CLASSE_TYPE);
                        }

                        if (hasFunctionalGroupsIds) {
                            formatGroups(user.getJsonArray("functionalGroupsIds"), idPersonneSupp, statements,
                                    Viescolaire.GROUPE_TYPE);
                        }
                        if (hasManualGroupsIds) {
                            formatGroups(user.getJsonArray("manualGroupsIds"), idPersonneSupp, statements,
                                    Viescolaire.GROUPE_MANUEL_TYPE);
                        }

                        JsonArray structureIds = user.getJsonArray("structureIds");
                        if(structureIds.size() == 0) {
                            log.info("no structureIds for user : " + idPersonneSupp);
                        } else {
                            log.info("structureIds : " + structureIds.toString());
                            formatStructure(structureIds, idPersonneSupp, statements);
                        }

                        // execute transaction when statements of all user are build
                        if(oNbUsers.intValue() == 0) {
                            if(statements.build().isEmpty()) {
                                log.info("0 user supp");
                                handler.handle(new Either.Right<>(null));
                            } else {
                                Sql.getInstance().transaction(statements.build(), SqlResult.validRowsResultHandler(handler));
                            }
                            log.info("createPersonnesSupp END");
                        }
                    }
                }
            }));

        }
    }
    @Override
    public void insertAnnotationsNewClasses(JsonArray users, Handler<Either<String, JsonObject>> handler){
        log.info("insertAnnotationsNewClasses START");
//        if(users != null) {
//            log.info("users : " + users.toString());
//        }
        StringBuilder query ;
        JsonArray statements = new fr.wseduc.webutils.collections.JsonArray();
        JsonArray params;

        for(Object u : users){
            JsonObject user = (JsonObject) u;
            List<String> newClassIds = user.getJsonArray("newClassIds").getList();
            String idUser = user.getString("id");

            boolean isStudent = "Student".equals(user.getString("type"));
            JsonArray structureIds = user.getJsonArray("structureIds");

            if(structureIds.size() == 0) {
                log.info("no structures for user : " + idUser);
            }


            if(isStudent && newClassIds != null && newClassIds.size() > 0 && structureIds.size() > 0) {
                query = new StringBuilder();
                params = new fr.wseduc.webutils.collections.JsonArray();
                query.append("INSERT INTO " + Viescolaire.EVAL_SCHEMA + ".rel_annotations_devoirs (id_devoir, id_annotation, id_eleve) " +
                        "(SELECT " + Viescolaire.EVAL_SCHEMA + ".rel_devoirs_groupes.id_devoir, " + // Récupère les ids des devoirs sur la classe/groupe
                        "(SELECT " + Viescolaire.EVAL_SCHEMA + ".annotations.id " + // Récupère les ids de l'annotaion NN de l'étab
                        "FROM " + Viescolaire.EVAL_SCHEMA + ".annotations " +
                        "WHERE libelle_court = 'NN' " +
                        "AND id_etablissement = ?), ? " +
                        "FROM " + Viescolaire.EVAL_SCHEMA + ".rel_devoirs_groupes " +
                        "WHERE id_groupe IN " + Sql.listPrepared(newClassIds) +
                        " AND NOT EXISTS (SELECT 1 " +
                        "FROM " + Viescolaire.EVAL_SCHEMA + ".notes " +
                        "WHERE notes.id_eleve = ? AND notes.id_devoir = rel_devoirs_groupes.id_devoir) " + // Vérifie que l'élève n'a pas de note sur le devoir
                        " AND NOT EXISTS (SELECT 1" +
                        "FROM " + Viescolaire.EVAL_SCHEMA + ".competences_notes " +
                        "WHERE competences_notes.id_eleve = ? AND competences_notes.id_devoir = rel_devoirs_groupes.id_devoir) " + // Vérifie que l'élève n'a pas de compétences notes sur le devoir
                        " AND EXISTS (SELECT 1" +
                        "FROM " + Viescolaire.EVAL_SCHEMA + ".annotations " +
                        "WHERE libelle_court = 'NN' AND id_etablissement = ?) " + // Vérifie que l'établissement est bien actif
                        " ) ON CONFLICT (id_eleve, id_devoir) DO NOTHING " ); // Vérifie que l'élève n'a pas d'annotation sur le devoir

                params.add(structureIds.getValue(0));
                params.add(idUser);
                for (Object idGroup : user.getJsonArray("newClassIds")) {
                    params.add(idGroup);
                }
                params.add(idUser);
                params.add(idUser);
                params.add(structureIds.getValue(0));
                statements.add(new JsonObject()
                        .put("statement", query.toString())
                        .put("values", params)
                        .put("action", "prepared"));
                log.debug(query);
                log.debug("idUser : "+ idUser);
                log.debug("structureIds.getValue(0) : "+ structureIds.getValue(0));
                log.debug(params.toString());
            }
        }

        if(statements.isEmpty()) {
            log.info("0 insertAnnotationsNewClasses");
            handler.handle(new Either.Right<>(new JsonObject()));
        } else {
            Sql.getInstance().transaction(statements,new DeliveryOptions().setSendTimeout(Viescolaire.UPDATE_CLASSES_CONFIG.getInteger("timeout-transaction") * 1000L), SqlResult.validRowsResultHandler(handler));
        }
    }

    /**
     * Inject creation request in SqlStatementBuilder for every class in ids
     * @param ids class ids list
     * @param userId user id
     * @param statements Sql statement builder
     * @param type Group type
     */
    private static void formatGroups (JsonArray ids, Long userId, SqlStatementsBuilder statements,
                                      Integer type) {
        for (int i = 0; i < ids.size(); i++) {
            String query =
                    "INSERT INTO " + Viescolaire.VSCO_SCHEMA +
                            ".rel_groupes_personne_supp(id_groupe, id, type_groupe) " +
                            "VALUES (?, ?, ?);";
            JsonArray params = new fr.wseduc.webutils.collections.JsonArray()
                    .add(ids.getString(i))
                    .add(userId)
                    .add(type);
            statements.prepared(query, params);
        }
    }

    /**
     * Return if the user is a valid profile user
     * @param user user object
     * @return true | false if the profile is a valid profile
     */
    private static boolean validProfile (JsonObject user) {
        return "Teacher".equals(user.getString("type")) || "Student".equals(user.getString("type"));
    }

    /**
     * Inject creation request in SqlStatementBuilder for every stucture in ids
     * @param ids structure ids list
     * @param userId user id
     * @param statements Sql statement builder
     */
    private static void formatStructure (JsonArray ids, Long userId, SqlStatementsBuilder statements) {
        for (int i = 0; i < ids.size(); i++) {
            String query =
                    "INSERT INTO " + Viescolaire.VSCO_SCHEMA + ".rel_structures_personne_supp(id_structure, id) " +
                            "VALUES (?, ?);";
            JsonArray params = new fr.wseduc.webutils.collections.JsonArray()
                    .add(ids.getString(i))
                    .add(userId);
            statements.prepared(query, params);
        }
    }

    /**
     * Recupere les établissements actifs de l'utilisateur connecté
     * @param userInfos : utilisateur connecté
     * @param handler handler comportant le resultat
     */
    @Override
    public void getActivesIDsStructures(UserInfos userInfos, String module,
                                        Handler<Either<String, JsonArray>> handler) {
        StringBuilder query =new StringBuilder();
        JsonArray params = new fr.wseduc.webutils.collections.JsonArray();

        query.append("SELECT id_etablissement ")
                .append("FROM "+ module +".etablissements_actifs  ")
                .append("WHERE id_etablissement IN " + Sql.listPrepared(userInfos.getStructures().toArray()))
                .append(" AND actif = TRUE");

        for(String idStructure :  userInfos.getStructures()){
            params.add(idStructure);
        }
        Sql.getInstance().prepared(query.toString(), params, SqlResult.validResultHandler(handler));
    }

    /**
     * Recupere les établissements inactifs de l'utilisateur connecté
     * @param handler handler comportant le resultat
     */
    @Override
    public void getActivesIDsStructures( String module,
                                         Handler<Either<String, JsonArray>> handler) {
        StringBuilder query =new StringBuilder();
        JsonArray params = new fr.wseduc.webutils.collections.JsonArray();

        query.append("SELECT id_etablissement ")
                .append("FROM "+ module +".etablissements_actifs  ")
                .append("WHERE actif = TRUE");

        Sql.getInstance().prepared(query.toString(), params,
                new DeliveryOptions().setSendTimeout(Viescolaire.UPDATE_CLASSES_CONFIG.getInteger("timeout-transaction") * 1000L),
                SqlResult.validResultHandler(handler));
    }

    /**
     * Active un établissement
     * @param id : établissement
     * @param handler handler comportant le resultat
     */
    @Override
    public void createActiveStructure (String id, String module, UserInfos user,
                                       Handler<Either<String, JsonArray>> handler) {
        SqlStatementsBuilder s = new SqlStatementsBuilder();
        JsonObject data = new JsonObject();
        String userQuery = "SELECT " + module + ".merge_users(?,?)";
        s.prepared(userQuery, (new fr.wseduc.webutils.collections.JsonArray()).add(user.getUserId()).add(user.getUsername()));
        data.put("id_etablissement", id);
        data.put("actif", true);
        s.insert(module + ".etablissements_actifs ", data, "id_etablissement");
        Sql.getInstance().transaction(s.build(), SqlResult.validResultHandler(handler));

    }

    /**
     * Supprime un établissement actif
     * @param id : utilisateur connecté
     * @param handler handler comportant le resultat
     */
    @Override
    public void deleteActiveStructure(String id, String module, Handler<Either<String, JsonArray>> handler) {
        String query = "DELETE FROM " + module + ".etablissements_actifs WHERE id_etablissement = ?";
        Sql.getInstance().prepared(query, (new fr.wseduc.webutils.collections.JsonArray()).add(Sql.parseId(id)), SqlResult.validResultHandler(handler));
    }

    @Override
    public void getUAI(String idEtabl, Handler<Either<String,JsonObject>> handler){
        StringBuilder query= new StringBuilder();
        query.append("MATCH(s:Structure) WHERE s.id={id} RETURN s.UAI as uai");
        Neo4j.getInstance().execute(query.toString(), new JsonObject().put("id", idEtabl), Neo4jResult.validUniqueResultHandler(handler));
    }

    @Override
    public void getUsers(List<String> idUsers, Handler<Either<String, JsonArray>> handler){
        StringBuilder query=new StringBuilder();
        query.append("MATCH (u:User) WHERE u.id IN {id} " +
                "OPTIONAL MATCH (s:Structure) " +
                "WHERE s.externalId IN u.structures " +
                "OPTIONAL MATCH (g:Group) " +
                "WHERE g.externalId IN u.groups " +
                "OPTIONAL MATCH (c:Class) " +
                "WHERE c.externalId IN u.classes " +
                "RETURN u.externalId AS externalId, u.id AS id, u.displayName AS displayName, u.firstName AS firstName, u.lastName AS lastName, u.profiles as type, u.birthDate AS birthDate, " +
                "COLLECT(DISTINCT s.id) AS structureIds, COLLECT(DISTINCT g.id) AS currentGroupIds, COLLECT(DISTINCT g.externalId) AS currentGroupExternalIds, " +
                "COLLECT(DISTINCT c.id) AS currentClassIds, COLLECT(DISTINCT c.externalId) AS currentClassExternalIds");

        fr.wseduc.webutils.collections.JsonArray usersArr = new fr.wseduc.webutils.collections.JsonArray(idUsers);
        log.debug("usersArr : " + usersArr.toString());
//        log.info("getUsers : " + query.toString());
        Neo4j.getInstance().execute(query.toString(), new JsonObject().put("id",usersArr), Neo4jResult.validResultHandler(handler));
    }

    @Override
    public void getElevesRelatives(List<String> idsClass,Handler<Either<String,JsonArray>> handler){
        StringBuilder query = new StringBuilder();
        JsonObject param = new JsonObject();
        query.append("MATCH (c:Class)<-[:DEPENDS]-(:ProfileGroup)<-[:IN]-(u:User {profiles:['Student']})-[:RELATED]-(r:User{profiles:['Relative']}) WHERE c.id IN {idClass}");
        query.append(" RETURN u.id as idNeo4j, u.externalId as externalId,u.attachmentId as attachmentId,u.lastName as lastName,u.level as level,u.firstName as firstName,u.relative as relative,");
        query.append("r.externalId as externalIdRelative, r.title as civilite, r.lastName as lastNameRelative, r.firstName as firstNameRelative, r.address as address, r.zipCode as zipCode, r.city as city,");
        query.append("c.id as idClass, c.name as nameClass, c.externalId as externalIdClass ORDER BY nameClass, lastName");
        param.put("idClass", new fr.wseduc.webutils.collections.JsonArray(idsClass));
        // TODO PUT ExternalId of deleted students and store deleted parents
        /*
        Neo4j.getInstance().execute(query.toString(), param, new DefaultUtilsService()
                .getEleveWithClasseName((String[])idsClass.toArray(),null,null,handler));
        */
        Neo4j.getInstance().execute(query.toString(), param, Neo4jResult.validResultHandler(handler));
    }

    @Override
    public void getAllElevesWithTheirRelatives(String idStructure,
                                               List<String> idsClass,
                                               List<String> deletedStudentsPostegre,
                                               Handler<Either<String, JsonArray>> handler) {
        JsonObject params = new JsonObject();
        String query = "MATCH (s:Structure{id:{structureId}})<-[:BELONGS]-(c:Class)"+
                "<-[:DEPENDS]-(:ProfileGroup)<-[:IN]-(u:User {profiles:['Student']})-[:RELATED]-(r:User{profiles:['Relative']})"+
                " WHERE c.id IN {classIds}"+
                " RETURN c.id as idClass, c.name as nameClass, c.externalId as externalIdClass, u.id as idEleve, "+
                "u.created as createdDate,u.deleteDate as deleteDate, u.externalId as externalId,u.attachmentId as attachmentId, "+
                "u.lastName as lastName,u.level as level,u.firstName as firstName,u.relative as relative, " +
                "r.externalId as externalIdRelative, r.title as civilite, r.lastName as lastNameRelative, "+
                "r.firstName as firstNameRelative, r.address as address, r.zipCode as zipCode, r.city as city " +
                "ORDER BY nameClass, lastName "+
                "UNION MATCH (u:User {profiles: ['Student']})-[:HAS_RELATIONSHIPS]->(b:Backup), " +
                "(s:Structure{id:{structureId}})<-[:BELONGS]-(c:Class) WHERE HAS(u.deleteDate) AND "+
                "(c.id IN {classIds} AND c.externalId IN u.classes) " +
                "RETURN c.id as idClass, c.name as nameClass, c.externalId as externalIdClass, " +
                "u.id as idEleve, u.created as createdDate, u.deleteDate as deleteDate,u.externalId as externalId, " +
                "u.attachmentId as attachmentId, u.lastName as lastName, u.level as level, u.firstName as firstName, " +
                "u.relative as relative, null as externalIdRelative, null as civilite, null as lastNameRelative, " +
                "null as firstNameRelative, null as address, null as zipCode, null as city "+
                "ORDER BY nameClass, lastName";

        params.put("structureId", idStructure).put("classIds",idsClass);

        if(deletedStudentsPostegre != null && deletedStudentsPostegre.size() > 0){
            query +=" UNION MATCH (s:Structure{id:{structureId}})<-[:BELONGS]-(c:Class)<-[:DEPENDS]-(:ProfileGroup)"+
                    "<-[:IN]-(u:User {profiles:['Student']})-[:RELATED]-(r:User{profiles:['Relative']}) " +
                    "WHERE u.id IN {idsDeletedStudent}" +
                    "RETURN c.id as idClass, c.name as nameClass, c.externalId as externalIdClass, u.id as idEleve, " +
                    "u.created as createdDate, u.deleteDate as deleteDate, u.externalId as externalId, "+
                    "u.attachmentId as attachmentId, u.lastName as lastName, u.level as level, u.firstName as firstName, " +
                    "u.relative as relative, r.externalId as externalIdRelative, r.title as civilite, r.lastName as lastNameRelative, " +
                    "r.firstName as firstNameRelative, r.address as address, r.zipCode as zipCode, r.city as city " +
                    "ORDER BY nameClass, lastName";
            params.put("idsDeletedStudent", deletedStudentsPostegre);
        }

        Neo4j.getInstance().execute(query,params, Neo4jResult.validResultHandler(handler));
    }


    @Override
    public void getCodeDomaine(String idClass,Handler<Either<String,JsonArray>> handler){
        StringBuilder query = new StringBuilder();
        query.append("SELECT id_groupe,id as id_domaine, code_domaine as code_domaine ");
        query.append("FROM notes.domaines INNER JOIN notes.rel_groupe_cycle ");
        query.append("ON notes.domaines.id_cycle= notes.rel_groupe_cycle.id_cycle ");
        query.append("WHERE notes.rel_groupe_cycle.id_groupe = ? AND code_domaine IS NOT NULL");
        JsonArray params = new fr.wseduc.webutils.collections.JsonArray();

        params.add(idClass);
        Sql.getInstance().prepared(query.toString(), params, SqlResult.validResultHandler(handler));

    }

    @Override
    public void getResponsablesDirection(String idStructure, Handler<Either<String, JsonArray>> handler) {
        String query = "MATCH (u:User{profiles:['Personnel']})-[IN]->(g:Group)-[DEPENDS]->(s:Structure {id:{structureId}}) " +
                "WHERE ANY(function IN u.functions WHERE function =~ (s.externalId+'\\\\$DIR\\\\$.*'))" +
                " RETURN DISTINCT u.id as id, u.displayName as displayName, u.externalId as externalId";
        JsonObject param = new JsonObject();
        param.put("structureId",idStructure);
        Neo4j.getInstance().execute(query,param,Neo4jResult.validResultHandler(handler));
    }


    /**
     * Retourne la liste des enfants pour un utilisateur donné
     * @param idUser    Id de l'utilisateur
     * @param handler   Handler comportant le resultat de la requete
     */
    @Override
    public void getEnfants(String idUser, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        query.append("MATCH (m:User {id: {id}})<-[:RELATED]-(n:User)")
                .append("RETURN n.id as id, n.firstName as firstName, n.lastName as lastName,  n.level as level, n.classes as classes, n.birthDate as birthDate ORDER BY lastName");
        neo4j.execute(query.toString(), new JsonObject().put("id", idUser), Neo4jResult.validResultHandler(handler));
    }

    /**
     * Retourne la liste des personnels pour une liste d'id donnée
     *
     * @param idPersonnels ids des personnels
     * @param handler      Handler comportant le resultat de la requete
     */
    public void getPersonnels(List<String> idPersonnels, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        query.append("MATCH (u:User)")
                .append("WHERE ANY (x IN u.profiles WHERE x IN ['Teacher', 'Personnel']) AND u.id IN {idPersonnel}")
                .append("RETURN u.id as id, u.lastName as lastName, u.firstName as firstName, u.emailAcademy as emailAcademy");
        neo4j.execute(query.toString(), new JsonObject().put("idPersonnel", new fr.wseduc.webutils.collections.JsonArray(idPersonnels)), Neo4jResult.validResultHandler(handler));
    }

    /**
     * Retourne la liste des enseignants pour une identifiant d'etablissement donne
     *
     * @param idEtablissement id de l'etablissement
     * @param handler      Handler comportant le resultat de la requete
     */
    public void getTeachers(String idEtablissement, Handler<Either<String, JsonArray>> handler) {
        //String query = "MATCH (:Structure {id: {idEtablissement}})-[:ADMINISTRATIVE_ATTACHMENT]-(u:User {profiles: ['Teacher']}) RETURN u";
        String query = "MATCH (u:User {profiles: ['Teacher']})-[:IN]->(:ProfileGroup)-[:DEPENDS*]->(s:Structure {id: {idEtablissement}}) " +
                "RETURN DISTINCT u.classes as classes, u.displayName as displayName, u.displayNameSearchField as displayNameSearchField , " +
                "u.externalId as externalId, u.firstName as firstName, u.firstNameSearchField as firstNameSearchField, " +
                "u.functions as functions, u.groups as groups, u.headTeacher as headTeacher, u.id as id, u.isTeacher as isTeacher," +
                " u.joinKey as joinKey, u.lastName as lastName, u.lastNameSearchField as lastNameSearchField, " +
                "u.oldClasses as oldClasses, u.profiles as profiles, u.structures as structures, " +
                "u.subjectTaught as subjectTaught, u.surname as surname, u.teaches as teaches, u.title as title";
        neo4j.execute(query, new JsonObject().put("idEtablissement", idEtablissement), Neo4jResult.validResultHandler(handler));
    }

    @Override
    public void list(String structureId, String profile, Handler<Either<String, JsonArray>> results) {
        JsonObject params = new JsonObject();
        String filter = "";
        String filterProfile = "WHERE 1=1 AND p.name <> 'Student' AND p.name <> 'Relative' ";
        String optionalMatch =
                "OPTIONAL MATCH u-[:IN]->(:ProfileGroup)-[:DEPENDS]->(class:Class)-[:BELONGS]->(s) " +
                        "OPTIONAL MATCH u-[:RELATED]->(parent: User) " +
                        "OPTIONAL MATCH (child: User)-[:RELATED]->u " +
                        "OPTIONAL MATCH u-[rf:HAS_FUNCTION]->fg-[:CONTAINS_FUNCTION*0..1]->(f:Function) ";
        if (profile != null && !profile.trim().isEmpty()) {
            filterProfile += "AND p.name = {profile} ";
            params.put(Field.PROFILE, profile);
        }
        if (structureId != null && !structureId.trim().isEmpty()) {
            filter = "(n:Structure {id : {structureId}})<-[:DEPENDS]-(g:ProfileGroup)<-[:IN]-";
            params.put(Field.STRUCTUREID, structureId);
        }
        String condition = "";
        String functionMatch = "WITH u MATCH (s:Structure)<-[:DEPENDS]-(pg:ProfileGroup)-[:HAS_PROFILE]->(p:Profile), u-[:IN]->pg ";

        String query =
                "MATCH " + filter + "(u:User) " +
                        functionMatch + filterProfile + condition + optionalMatch +
                        "RETURN DISTINCT u.id as id, p.name as type, u.externalId as externalId, " +
                        "u.firstName as firstName, u.lastName as lastName, u.displayName as displayName " +
                        "ORDER BY type DESC, displayName ASC ";
        neo4j.execute(query, params,  Neo4jResult.validResultHandler(results));
    }

    private String getFieldName(String field) {
        String fieldName;
        switch(field) {
            case Field.LASTNAME:
                fieldName = "u.lastName";
                break;
            case Field.FIRSTNAME:
                fieldName = "u.firstName";
                break;
            default:
                fieldName = "u.displayName";
        }
        return fieldName;
    }

    @Override
    public void search(String structureId, String userId, String query, List<String> fields, String profile, Handler<Either<String, JsonArray>> handler) {
        if (fields.isEmpty()) {
            handler.handle(new Either.Right<>(new JsonArray()));
            return;
        }

        final StringBuilder filter = new StringBuilder();
        fields.forEach(field -> filter.append("OR toLower(").append(getFieldName(field)).append(") CONTAINS {query} "));

        String neo4jquery = "MATCH (u:User)-[:IN]->(p:ProfileGroup)-[:DEPENDS*]->(s:Structure) ";

        if (!Objects.equals(profile, "Personnel"))
            neo4jquery += ",(p)-[:DEPENDS]->(c:Class) ";

        neo4jquery += "WHERE s.id = {structureId} AND u.profiles = {profiles} " +
                "AND (" + filter.toString().replaceFirst("OR ", "") + ") " +
                "RETURN distinct u.id as id, (u.lastName + ' ' + u.firstName) as displayName, u.lastName as lastName," +
                " u.firstName as firstName, u.classes as idClasse ";

        if (!Objects.equals(profile, "Personnel"))
            neo4jquery += ",collect(c.name) as classesNames ";

        neo4jquery += "ORDER BY displayName;";


        if (userId != null) {
            neo4jquery = "MATCH (u:User)-[:IN]->(:ProfileGroup)-[:DEPENDS]->(c:Class)-[:BELONGS]->(s:Structure {id:{structureId}}), " +
                    "(t:User {id: {userId}})-[:IN]->(:ProfileGroup)-[:DEPENDS]->(c) " +
                    "WHERE u.profiles = {profiles} " +
                    "AND (" + filter.toString().replaceFirst("OR ", "") + ") " +
                    "RETURN distinct u.id as id, (u.lastName + ' ' + u.firstName) as displayName, u.lastName as lastName, " +
                    "u.firstName as firstName, u.classes as idClasse, collect(c.name) as classesNames " +
                    "ORDER BY displayName;";
        }

        JsonObject params = new JsonObject()
                .put(Field.STRUCTUREID, structureId)
                .put(Field.USERID, userId)
                .put(Field.QUERY, query)
                .put(Field.PROFILES, new JsonArray().add(profile));

        neo4j.execute(neo4jquery, params, Neo4jResult.validResultHandler(handler));
    }

    /**
     * get lastName and first name of deleted teachers
     *
     * @param idsTeacher ids of teacher
     * @param handler    response
     */
    @Override
    public void getDeletedTeachers (List<String> idsTeacher, Handler<Either<String, JsonArray>> handler) {
        String query = "SELECT * FROM "+ Viescolaire.VSCO_SCHEMA + ".personnes_supp WHERE user_type = 'Teacher' " +
                "AND id_user IN "+Sql.listPrepared(idsTeacher);

        JsonArray values = new fr.wseduc.webutils.collections.JsonArray();
        for (String idTeacher : idsTeacher) values.add(idTeacher);

        Sql.getInstance().prepared(query, values, SqlResult.validResultHandler(handler));

    }

}