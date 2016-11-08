/*
 * Copyright (c) Région Hauts-de-France, Département 77, CGI, 2016.
 *
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
 *
 */

package fr.openent.viescolaire.service.impl;

import com.fasterxml.jackson.databind.util.JSONPObject;
import fr.openent.Viescolaire;
import fr.openent.viescolaire.service.ReferentielService;
import fr.wseduc.webutils.Either;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.neo4j.Neo4jResult;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

import java.util.List;

/**
 * Created by ledunoiss on 02/11/2016.
 */
public class DefaultReferentielService implements ReferentielService {


    @Override
    public void syncStructure(List<String> externaIds, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder()
                .append("MATCH (n:`Structure`) WHERE n.externalId IN  [");

        for (int i = 0; i < externaIds.size(); i++) {
            query.append("'" + externaIds.get(i) + "'");
            if (i != externaIds.size()-1) {
                query.append(",");
            }
        }

        query.append("] RETURN n");

        Neo4j.getInstance().execute(query.toString(), new JsonObject(), Neo4jResult.validResultHandler(handler));
    }

    @Override
    public void createStructure(JsonObject structure, Handler<Either<String, JsonObject>> handler) {
        StringBuilder query = new StringBuilder()
                .append("INSERT INTO " + Viescolaire.VSCO_SCHEMA + ".structure " +
                        "(uai, ville, telephone, siret, externalid, type, adresse, fk4j_structure_id) " +
                        "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?) RETURNING *");

        JsonArray params = new JsonArray().addString(structure.getString("UAI"))
                .addString(structure.getString("city"))
                .addString(structure.getString("phone"))
                .addString(structure.getString("SIRET"))
                .addString(structure.getString("externalId"))
                .addString(structure.getString("type"))
                .addString(structure.getString("address"))
                .addString(structure.getString("id"));

        Sql.getInstance().prepared(query.toString(), params, SqlResult.validUniqueResultHandler(handler));
    }

    @Override
    public void syncClassesStructure(String externalId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder()
                .append("MATCH (n:`Class`)-[DEPENDS]-(s:Structure {externalId : {externalid}}) RETURN n");

        Neo4j.getInstance().execute(query.toString(), new JsonObject().putString("externalid", externalId), Neo4jResult.validResultHandler(handler));
    }

    @Override
    public void createClasses(JsonArray classes, Integer idStructure, Integer idTypeClasse, Handler<Either<String, JsonObject>> handler) {
        if (classes.size() > 0) {
            JsonArray params = new JsonArray();
            StringBuilder query = new StringBuilder()
                    .append("INSERT INTO " + Viescolaire.VSCO_SCHEMA + ".classe " +
                            "(fk4j_classe_id, id_etablissement, externalid, libelle, id_type_classe) VALUES ");
            for (int i = 0; i < classes.size(); i++) {
                JsonObject classe = classes.get(i);
                query.append("( ?, ?, ?, ?, ?)");
                params.addString(classe.getString("id"))
                        .addNumber(idStructure)
                        .addString(classe.getString("externalId"))
                        .addString(classe.getString("name"))
                        .addNumber(idTypeClasse);
                if (i != classes.size() - 1) {
                    query.append(",");
                }
            }

            Sql.getInstance().prepared(query.toString(), params, SqlResult.validRowsResultHandler(handler));
        }
        else {
            JsonObject r = new JsonObject();
            handler.handle(new Either.Right<String, JsonObject>(r));
        }
    }

    @Override
    public void syncFunctionalsGroups(String externalId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder()
                .append("MATCH (n:`FunctionalGroup`)-[DEPENDS]-(s:Structure {externalId : {externalid}}) RETURN n");

        Neo4j.getInstance().execute(query.toString(), new JsonObject().putString("externalid", externalId), Neo4jResult.validResultHandler(handler));
    }

    @Override
    public void syncStudentsParents(String externalId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder()
                .append("MATCH (r:Structure {externalId : {externalid}})<-[ADMINISTRATIVE_ATTACHMENT]-(n:User)-[:`RELATED`]->(b:User) " +
                        "RETURN n as eleve,collect(b) as parents");

        Neo4j.getInstance().execute(query.toString(), new JsonObject().putString("externalid", externalId), Neo4jResult.validResultHandler(handler));
    }

    @Override
    public void createStudentsParents(JsonArray students, Handler<Either<String, JsonObject>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray params = new JsonArray();
        JsonArray parentsIds = new JsonArray();

        for (int i = 0; i < students.size(); i++) {
            JsonObject eleve = students.get(i);
            query.append("INSERT INTO " + Viescolaire.VSCO_SCHEMA + ".eleve" +
                    " (fk4j_user_id, externalid, nom, prenom, login) VALUES (?, ?, ?, ?, ?);");
            params.addString(eleve.getString("id")).addString(eleve.getString("externalId"))
                    .addString(eleve.getString("lastName")).addString(eleve.getString("firstName"))
                    .addString(eleve.getString("login"));

            if (eleve.containsField("classes")) {
                JsonArray classes = eleve.getArray("classes");
                for (int y = 0; y < classes.size(); y++) {
                    String libelleClasse = classes.get(0);
                    libelleClasse = libelleClasse.split("\\$")[1];
                    query.append("INSERT INTO " + Viescolaire.VSCO_SCHEMA + ".rel_eleve_classe (id_eleve, id_classe) VALUES (" +
                            "(SELECT id FROM " + Viescolaire.VSCO_SCHEMA + ".eleve WHERE eleve.fk4j_user_id = ?)," +
                            "(SELECT id FROM " + Viescolaire.VSCO_SCHEMA + ".classe WHERE classe.libelle = ?));");
                    params.addString(eleve.getString("id")).addString(libelleClasse);
                }
            }

            if (eleve.containsField("groups")) {
                JsonArray groups = eleve.getArray("groups");
                for (int j = 0; j < groups.size(); j++) {
                    String libelleGroup = groups.get(j);
                    libelleGroup = libelleGroup.split("\\$")[1];
                    query.append("INSERT INTO " + Viescolaire.VSCO_SCHEMA + ".rel_eleve_classe (id_eleve, id_classe) VALUES (" +
                            "(SELECT id FROM " + Viescolaire.VSCO_SCHEMA + ".eleve WHERE eleve.fk4j_user_id = ?)," +
                            "(SELECT id FROM " + Viescolaire.VSCO_SCHEMA + ".classe WHERE classe.libelle = ?));");
                    params.addString(eleve.getString("id")).addString(libelleGroup);
                }
            }

            if (eleve.containsField("parents")) {
                JsonArray parents = eleve.getArray("parents");
                for (int y = 0; y < parents.size(); y++) {
                    JsonObject parent = parents.get(y);
                    if (!parentsIds.contains(parent.getString("id"))) {
                        query.append("INSERT INTO " + Viescolaire.VSCO_SCHEMA + ".parent " +
                                "(nom, prenom, login, email, telephone, mobile, fk4j_user_id) VALUES " +
                                " (?, ?, ?, ?, ?, ?, ?);");
                        params.addString(parent.getString("lastName")).addString(parent.getString("firstName"))
                                .addString(parent.getString("login")).addString(parent.getString("email"))
                                .addString(parent.getString("phone")).addString(parent.getString("mobile"))
                                .addString(parent.getString("id"));
                        parentsIds.addString(parent.getString("id"));
                    }
                }
            }
        }
        Sql.getInstance().prepared(query.toString(), params, SqlResult.validRowsResultHandler(handler));
    }

    @Override
    public void linkStudentsParents(JsonArray students, Handler<Either<String, JsonObject>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray params = new JsonArray();

        for (int i = 0; i < students.size(); i++) {
            JsonObject eleve = students.get(i);
            if (eleve.containsField("parents")) {
                JsonArray parents = eleve.getArray("parents");
                for (int y = 0; y < parents.size(); y++) {
                    JsonObject parent = parents.get(y);
                    query.append("INSERT INTO " + Viescolaire.VSCO_SCHEMA + ".rel_eleve_parent (fk_parent_id, fk_eleve_id) " +
                            "VALUES (" +
                            "(SELECT id FROM " + Viescolaire.VSCO_SCHEMA + ".parent WHERE parent.fk4j_user_id = ?)," +
                            "(SELECT id FROM " + Viescolaire.VSCO_SCHEMA + ".eleve WHERE eleve.fk4j_user_id = ?)" +
                            ");");
                    params.addString(parent.getString("id")).addString(eleve.getString("id"));
                }
            }
        }
        Sql.getInstance().prepared(query.toString(), params, SqlResult.validRowsResultHandler(handler));
    }

    @Override
    public void syncPersonnels(String externalId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder()
                .append("MATCH (n:`User` {profiles : ['Personnel']})-[ADMINISTRATIVE_ATTACHMENT]-(r:Structure {externalId : {externalid}}) RETURN n");

        Neo4j.getInstance().execute(query.toString(), new JsonObject().putString("externalid", externalId), Neo4jResult.validResultHandler(handler));
    }

    @Override
    public void createPersonnels(JsonArray personnels, String externalId, Handler<Either<String, JsonObject>> handler) {
        StringBuilder query = new StringBuilder().append("INSERT INTO " + Viescolaire.VSCO_SCHEMA + ".personnel " +
                "(fk4j_user_id, externalid, nom, prenom, profil, enseigne, id_etablissement) VALUES ");
        JsonArray params = new JsonArray();

        for (int i = 0; i < personnels.size(); i++) {
            JsonObject personnel = personnels.get(i);
            query.append("(?, ?, ?, ?, ?, false, (SELECT id FROM " +  Viescolaire.VSCO_SCHEMA + ".structure WHERE structure.externalid = ?))");
            params.addString(personnel.getString("id")).addString(personnel.getString("externalId"))
                    .addString(personnel.getString("lastName")).addString(personnel.getString("firstName"))
                    .addString(personnel.getArray("profiles").get(0).toString()).addString(externalId);
            if (i < personnels.size() - 1) {
                query.append(",");
            }
        }
        Sql.getInstance().prepared(query.toString(), params, SqlResult.validRowsResultHandler(handler));
    }

    @Override
    public void createPersonnel(final Integer structureId, final JsonObject personnel, final Handler<Boolean> handler) {
        this.findPersonnel(personnel.getString("userId"), new Handler<Either<String, JsonObject>>() {
            @Override
            public void handle(Either<String, JsonObject> p) {
                if (p.isRight()) {
                    JsonObject _d = p.right().getValue();
                    if (!_d.containsField("id")) {
                        Boolean enseigne = false;
                        if (personnel.getBoolean("teaches") == true) {
                            enseigne = true;
                        }
                        createPersonnelDB(personnel, enseigne, new Handler<Either<String, JsonObject>>() {
                            @Override
                            public void handle(Either<String, JsonObject> event) {
                                if (event.isRight()) {
                                    JsonObject _p = event.right().getValue();
                                    linkPersonnelStructure(structureId, _p.getInteger("id"), new Handler<Either<String, JsonObject>>() {
                                        @Override
                                        public void handle(Either<String, JsonObject> event) {
                                            handler.handle(true);
                                        }
                                    });
                                } else {
                                    handler.handle(false);
                                }
                            }
                        });
                    } else {
                        linkPersonnelStructure(structureId, _d.getInteger("id"), new Handler<Either<String, JsonObject>>() {
                            @Override
                            public void handle(Either<String, JsonObject> event) {
                                handler.handle(true);
                            }
                        });
                    }
                } else {
                    handler.handle(false);
                }
            }
        });
    }

    @Override
    public void syncTeachers(String externalId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder()
                .append("MATCH (n:`User` {profiles : ['Teacher']})-[ADMINISTRATIVE_ATTACHMENT]-(r:Structure {externalId : {externalid}}) RETURN n ");

        Neo4j.getInstance().execute(query.toString(), new JsonObject().putString("externalid", externalId), Neo4jResult.validResultHandler(handler));
    }

    @Override
    public void syncMatieres(JsonArray matieres, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder()
                .append("MATCH (n:`FieldOfStudy`) WHERE n.externalId IN  [");

        for (int i = 0; i < matieres.size(); i++) {
            query.append("'" + matieres.get(i) + "'");
            if (i != matieres.size()-1) {
                query.append(",");
            }
        }

        query.append("] RETURN n");

        Neo4j.getInstance().execute(query.toString(), new JsonObject(), Neo4jResult.validResultHandler(handler));
    }

    @Override
    public void createMatiere(JsonArray matieres, Handler<Either<String, JsonObject>> handler) {
        StringBuilder query = new StringBuilder().append("INSERT INTO " + Viescolaire.VSCO_SCHEMA + ".matiere " +
                "(evaluable, matiere, id_etablissement, id_professeur, fk4j_matiere_id) VALUES ");
        JsonArray params = new JsonArray();
        for (int i = 0; i < matieres.size(); i++) {
            JsonObject matiere = matieres.get(i);
            query.append("(true, ?, ?, (SELECT id FROM " + Viescolaire.VSCO_SCHEMA + ".personnel WHERE personnel.fk4j_user_id = ?), ?)");
            params.addString(matiere.getString("name")).addNumber(matiere.getNumber("structureId"))
                    .addString(matiere.getString("userId")).addString(matiere.getString("fk4j_matiere_id"));
            if (i < matieres.size() - 1) {
                query.append(",");
            }
        }

        Sql.getInstance().prepared(query.toString(), params, SqlResult.validRowsResultHandler(handler));
    }

    @Override
    public void createTeachers(JsonArray teachers, String externalId, Handler<Either<String, JsonObject>> handler) {
        StringBuilder query = new StringBuilder().append("INSERT INTO " + Viescolaire.VSCO_SCHEMA + ".personnel " +
                "(fk4j_user_id, externalid, nom, prenom, profil, enseigne, id_etablissement) VALUES ");
        JsonArray params = new JsonArray();

        for (int i = 0; i < teachers.size(); i++) {
            JsonObject teacher = teachers.get(i);
            query.append("(?, ?, ?, ?, ?, true, (SELECT id FROM " +  Viescolaire.VSCO_SCHEMA + ".structure WHERE structure.externalid = ?))");
            params.addString(teacher.getString("id")).addString(teacher.getString("externalId"))
                    .addString(teacher.getString("lastName")).addString(teacher.getString("firstName"))
                    .addString(teacher.getArray("profiles").get(0).toString()).addString(externalId);
            if (i < teachers.size() - 1) {
                query.append(",");
            }
        }
        Sql.getInstance().prepared(query.toString(), params, SqlResult.validRowsResultHandler(handler));
    }

    @Override
    public void createPersonnelDB(JsonObject personnel, Boolean enseigne, Handler<Either<String, JsonObject>> handler) {
        JsonArray params = new JsonArray();
        StringBuilder query = new StringBuilder()
                .append("INSERT INTO " + Viescolaire.VSCO_SCHEMA + ".personnel " +
                        "(fk4j_user_id, externalid, nom, prenom, profil, enseigne) VALUES " +
                        "(?, ?, ?, ?, ?, ?) RETURNING *");
        params.addString(personnel.getString("id")).addString(personnel.getString("externalId"))
                .addString(personnel.getString("lastName")).addString(personnel.getString("firstName"))
                .addString(personnel.getArray("profiles").get(0).toString()).addBoolean(enseigne);

        Sql.getInstance().prepared(query.toString(), params, SqlResult.validUniqueResultHandler(handler));
    }

    @Override
    public void findPersonnel(String userId, Handler<Either<String, JsonObject>> handler) {
        StringBuilder query = new StringBuilder()
                .append("SELECT id " +
                        "FROM " + Viescolaire.VSCO_SCHEMA + ".personnel " +
                        "WHERE personnel.fk4j_user_id = ?");

        Sql.getInstance().prepared(query.toString(), new JsonArray().addString(userId), SqlResult.validUniqueResultHandler(handler));
    }


    @Override
    public void linkPersonnelStructure(Integer structureId, Integer userId, Handler<Either<String, JsonObject>> handler) {
        StringBuilder query = new StringBuilder()
                .append("INSERT INTO " + Viescolaire.VSCO_SCHEMA + ".rel_personnel_structure " +
                        "(id_etablissement, id_personnel) VALUES (?,?);");
        JsonArray params = new JsonArray().addNumber(structureId).addNumber(userId);
        Sql.getInstance().prepared(query.toString(), params, SqlResult.validRowsResultHandler(handler));
    }

    @Override
    public void linkPersonnelClasses(String userId, JsonArray classes, Handler<Either<String, JsonObject>> handler) {
        JsonArray params = new JsonArray();
        StringBuilder query = new StringBuilder()
                .append("INSERT INTO " + Viescolaire.VSCO_SCHEMA + ".rel_personnel_classe " +
                        "(id_classe, id_personnel) VALUES ");

        for (int i = 0; i < classes.size(); i++) {
            query.append("((SELECT id FROM " + Viescolaire.VSCO_SCHEMA + ".classe WHERE classe.libelle = ?)," +
                    "(SELECT id FROM " + Viescolaire.VSCO_SCHEMA + ".personnel WHERE personnel.fk4j_user_id = ?))");
            String classe = classes.get(i);
            params.addString(classe).addString(userId);
            if (i < classes.size() -1) {
                query.append(",");
            }
        }
        Sql.getInstance().prepared(query.toString(), params, SqlResult.validRowsResultHandler(handler));
    }
}
