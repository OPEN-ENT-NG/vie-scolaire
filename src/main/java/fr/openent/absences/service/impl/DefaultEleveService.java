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

package fr.openent.absences.service.impl;

import fr.openent.Viescolaire;
import fr.wseduc.webutils.Either;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.json.impl.Json;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DefaultEleveService extends SqlCrudService implements fr.openent.absences.service.EleveService {

    private static final String SELECT = "SELECT ";
    private static final String FROM = "FROM ";
    private static final String FILTER_COURS_ID = "AND appel.id_cours = cours.id ";
    private static final String TABLE_ABSENCE = ".absence_prev ";

    public DefaultEleveService() {
        super(Viescolaire.VSCO_SCHEMA, Viescolaire.VSCO_ELEVE_TABLE);
    }

    

    @Override
    public void getEvenements(String psIdEleve, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT evenement.*, cours.id as id_cours ")
                .append("FROM presences.evenement, viesco.cours, presences.appel, presences.type_evt ")
                .append("WHERE evenement.id_eleve = ? ")
                .append("AND evenement.id_appel = appel.id ")
                .append("AND appel.id_cours = cours.id ")
                .append("AND evenement.id_type = type_evt.id ")
                .append("AND cours.timestamp_dt >= to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') ")
                .append("AND cours.timestamp_fn < to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS')");

        values.addString(psIdEleve);
        values.addString(psDateDebut);
        values.addString(psDateFin);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getAbsencesPrev(String psIdEleve, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append(SELECT+ Viescolaire.ABSC_SCHEMA +".absence_prev.* ")
                .append(FROM+ Viescolaire.ABSC_SCHEMA +TABLE_ABSENCE)
                .append("WHERE absence_prev.id_eleve = ? ");

        values.addString(psIdEleve);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getAbsencesPrev(String psIdEleve, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        // récupération de toutes les absences prévisionnelles dont la date de début ou la date de fin
        // est comprise entre la date de début et de fin passée en paramètre (exemple date début et date fin d'un cours)
        query.append(SELECT+ Viescolaire.ABSC_SCHEMA +".absence_prev.* ")
                .append(FROM+ Viescolaire.ABSC_SCHEMA +TABLE_ABSENCE)
                .append("WHERE absence_prev.id_eleve = ? ")
                .append("AND ( ")
                .append("(to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') <= absence_prev.timestamp_dt AND absence_prev.timestamp_dt < to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS')) ")
                .append("OR ")
                .append("(to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') <= absence_prev.timestamp_fn  AND absence_prev.timestamp_fn < to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS')) ")
                .append(")");

        values.addString(psIdEleve)
                .addString(psDateDebut)
                .addString(psDateFin)
                .addString(psDateDebut)
                .addString(psDateFin);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getAbsences(String psIdEtablissement, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT DISTINCT(evenement.id), cours.id_matiere, evenement.commentaire, evenement.saisie_cpe, ")
                .append("evenement.id_eleve, evenement.id_motif, cours.timestamp_dt, cours.timestamp_fn, cours.id_personnel, cours.id_classe, ")
                .append("evenement.id_appel, evenement.id_type ")
                .append(FROM+ Viescolaire.ABSC_SCHEMA +".appel, "+ Viescolaire.VSCO_SCHEMA +".cours, "+ Viescolaire.ABSC_SCHEMA +".evenement ")
                .append("LEFT OUTER JOIN "+ Viescolaire.ABSC_SCHEMA +".motif on (evenement.id_motif = motif.id) ")
                .append("WHERE evenement.id_appel = appel.id ")
                .append(FILTER_COURS_ID)
                .append("AND cours.timestamp_dt >= to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS')  ")
                .append("AND cours.timestamp_fn <= to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') ")
                .append("AND cours.id_etablissement = ? ")
                .append("AND evenement.id_type = 1");

        values.addString(psDateDebut).addString(psDateFin).addString(psIdEtablissement);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getAbsencesSansMotifs(String psIdEtablissement, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT evenement.id ,evenement.commentaire ,evenement.saisie_cpe ,evenement.id_eleve ,evenement.id_motif " )
                .append(",cours.timestamp_dt ,cours.timestamp_fn ,evenement.id_appel ,evenement.id_type ,cours.id_classe ,cours.id_personnel " )
                .append(",motif.id ,motif.libelle ,motif.justifiant " )
                .append(FROM).append(Viescolaire.ABSC_SCHEMA).append(".appel, ").append(Viescolaire.VSCO_SCHEMA).append(".cours, ").append(Viescolaire.ABSC_SCHEMA).append(".evenement " )
                .append("LEFT JOIN ").append(Viescolaire.ABSC_SCHEMA).append(".motif ON (evenement.id_motif = motif.id) " )
                .append("WHERE evenement.id_appel = appel.id " )
                    .append(FILTER_COURS_ID )
                    .append("AND cours.timestamp_dt >= to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') " )
                    .append("AND cours.timestamp_fn <= to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') " )
                    .append("AND evenement.id_type = 1 " )
                    .append("AND ( " )
                    .append("evenement.id_motif = 8 " )
                        .append("OR evenement.id_motif = 2 " )
                    .append(") " )
                    .append( "AND cours.id_etablissement = ? " )
                .append("ORDER BY cours.timestamp_dt DESC");

        values.addString(psDateDebut).addString(psDateFin).addString(psIdEtablissement);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getAbsencesPrevClassePeriode(List<String> idEleves, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT absence_prev.* ")
                .append(FROM+ Viescolaire.ABSC_SCHEMA +TABLE_ABSENCE )
                .append("WHERE absence_prev.timestamp_dt >= to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') ")
                .append("AND absence_prev.timestamp_fn <= to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') ")
                .append("AND id_eleve IN "+ Sql.listPrepared(idEleves.toArray()));

        values.addString(psDateDebut).addString(psDateFin);
        for(Integer i=0; i< idEleves.size(); i++){
            values.addString(idEleves.get(i));
        }

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getAllAbsenceEleve(String idEleve, boolean isAscending, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT evenement.*, cours.timestamp_dt as timestamp_dt, cours.timestamp_fn as timestamp_fn, cours.id as id_cours ")
                .append("FROM presences.evenement, viesco.cours, presences.appel, presences.type_evt ")
                .append("WHERE evenement.id_eleve = ? ")
                .append("AND evenement.id_type = 1 ")
                .append("AND evenement.id_appel = appel.id ")
                .append("AND appel.id_cours = cours.id ")
                .append("AND evenement.id_type = type_evt.id ")
                .append("ORDER BY cours.timestamp_dt ");

        // Si c'est dans l'ordre descendant
        if(!isAscending){
            query.append("DESC");
        }
        values.addString(idEleve);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }


    @Override
    public void saveZoneAbsence(final String idUser, final String idEleve, final Integer idMotif,
                                final JsonArray arrayAbscPrevToCreate,
                                final JsonArray arrayAbscPrevToUpdate,
                                final JsonArray arrayAbscPrevToDelete,
                                final List<Integer> listEventIdToUpdate,
                                final JsonArray arrayEventToCreate, final JsonArray arrayCoursToCreate, final Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();



        if (arrayCoursToCreate.toList().isEmpty()){
            gestionEventToCreate(idUser, idEleve, idMotif, arrayAbscPrevToCreate, arrayAbscPrevToUpdate,
                    arrayAbscPrevToDelete, listEventIdToUpdate, arrayEventToCreate,
                    arrayCoursToCreate, handler);
            /*JsonArray statements = createStatementSaveZoneAbsence(idUser, idEleve, idMotif, arrayAbscPrevToCreate, arrayAbscPrevToUpdate, arrayAbscPrevToDelete,
                    listEventIdToUpdate, null, null, arrayCoursToCreate);
            System.out.println("Execute query with handler");
            Sql.getInstance().transaction(statements, SqlResult.validResultHandler(handler));*/
        } else {
            // Création de la requête pour récupérer les Nextval des appels qui vont être INSERT.
            int nbrCoursToCreate = arrayCoursToCreate.size();
            query = new StringBuilder();
            if (nbrCoursToCreate > 0) {
                query.append("WITH ");
                for (int i = 0; i < nbrCoursToCreate; i++) {
                    query.append("r" + i + " AS ( SELECT nextval('" + Viescolaire.VSCO_SCHEMA + ".cours_id_seq') as id" + i + ") ");
                    if (i != nbrCoursToCreate - 1) {
                        query.append(",");
                    }
                }
                query.append("SELECT * FROM ");
                for (int i = 0; i < nbrCoursToCreate; i++) {
                    query.append("r" + i);
                    if (i != nbrCoursToCreate - 1) {
                        query.append(",");
                    }
                }
            }

            sql.raw(query.toString(), SqlResult.validUniqueResultHandler(new Handler<Either<String, JsonObject>>() {
                @Override
                public void handle(Either<String, JsonObject> result) {
                    if (result.isRight()) {

                        Map<String, Object> mapIdCours = result.right().getValue().toMap();

                        // On ajoute l'id du cours aux cours à créer.
                        List<Long> listIdCoursToCreate = new ArrayList<>();
                        for (Object value : mapIdCours.values()) {
                            listIdCoursToCreate.add((Long) value);
                        }
                        for (int i = 0; i < arrayCoursToCreate.size(); i++) {
                            long idCours = listIdCoursToCreate.get(i);
                            JsonObject coursToCreate = arrayCoursToCreate.get(i);
                            coursToCreate.putNumber("id", listIdCoursToCreate.get(i));

                            JsonObject eventToCreate = new JsonObject();
                            eventToCreate.putNumber("id_cours", idCours);
                            arrayEventToCreate.add(eventToCreate);
                        }

                        gestionEventToCreate(idUser, idEleve, idMotif, arrayAbscPrevToCreate, arrayAbscPrevToUpdate,
                                arrayAbscPrevToDelete, listEventIdToUpdate, arrayEventToCreate,
                                arrayCoursToCreate, handler);
                    }
                }
            }));
        }
    }

    private void gestionEventToCreate(final String idUser, final String idEleve, final Integer idMotif,
                                      final JsonArray arrayAbscPrevToCreate,
                                      final JsonArray arrayAbscPrevToUpdate,
                                      final JsonArray arrayAbscPrevToDelete,
                                      final List<Integer> listEventIdToUpdate, final JsonArray arrayEventToCreate,
                                      final JsonArray arrayCoursToCreate, final Handler<Either<String, JsonArray>> handler)
    {
        if (arrayEventToCreate.toList().isEmpty()) {
            JsonArray statements = createStatementSaveZoneAbsence(idUser, idEleve, idMotif, arrayAbscPrevToCreate, arrayAbscPrevToUpdate, arrayAbscPrevToDelete,
                    listEventIdToUpdate, null, null, arrayCoursToCreate);
            System.out.println("Execute query with handler");
            Sql.getInstance().transaction(statements, SqlResult.validResultHandler(handler));
        } else {
            StringBuilder query = new StringBuilder();
            JsonArray values = new JsonArray();
            // Requête pour récupérer les id_appels
            query.append("SELECT * FROM " + Viescolaire.ABSC_SCHEMA + ".appel ");
            query.append("WHERE appel.id_cours IN " + Sql.listPrepared(arrayEventToCreate.toArray()));
            for (int i = 0; i < arrayEventToCreate.size(); i++) {
                JsonObject eventToCreate = arrayEventToCreate.get(i);
                values.addNumber(eventToCreate.getInteger("id_cours"));
            }

            // On exécute la recherche des appels
            Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(new Handler<Either<String, JsonArray>>() {
                @Override
                public void handle(Either<String, JsonArray> result) {
                    if (result.isRight()) {
                        // On récupère les appels
                        JsonArray arrayAppels = result.right().getValue();

                        final JsonArray arrayEventToCreateWithAppel = new JsonArray();
                        final JsonArray arrayEventToCreateWithoutAppel = new JsonArray();

                        // On ajoute l'id_appel aux evenements à créer
                        for (int i = 0; i < arrayEventToCreate.size(); i++) {
                            JsonObject eventToCreate = arrayEventToCreate.get(i);
                            boolean appelFound = false;
                            for (int j = 0; j < arrayAppels.size(); j++) {
                                JsonObject appel = arrayAppels.get(j);
                                if (eventToCreate.getNumber("id_cours").intValue() == appel.getNumber("id_cours").intValue()) {
                                    eventToCreate.putValue("id_appel", appel.getValue("id"));
                                    appelFound = true;
                                    break;
                                }
                            }

                            // On sépare les evenements qui ont un id_appel associé à leur cours, et ceux qui en ont pas
                            if (appelFound) {
                                arrayEventToCreateWithAppel.add(eventToCreate);
                            } else {
                                arrayEventToCreateWithoutAppel.add(eventToCreate);
                            }
                        }

                        if (arrayEventToCreateWithoutAppel == null || arrayEventToCreateWithoutAppel.toList().isEmpty()) {
                            JsonArray statements = createStatementSaveZoneAbsence(idUser, idEleve, idMotif, arrayAbscPrevToCreate, arrayAbscPrevToUpdate,
                                    arrayAbscPrevToDelete, listEventIdToUpdate, arrayEventToCreateWithAppel, null, arrayCoursToCreate);
                            System.out.println("Execute query with handler");
                            Sql.getInstance().transaction(statements, SqlResult.validResultHandler(handler));
                        } else {
                            // Création de la requête pour récupérer les Nextval des appels qui vont être INSERT.
                            int nbrAppelToCreate = arrayEventToCreateWithoutAppel.size();
                            StringBuilder query = new StringBuilder();
                            if (nbrAppelToCreate > 0) {
                                query.append("WITH ");
                                for (int i = 0; i < nbrAppelToCreate; i++) {
                                    query.append("r" + i + " AS ( SELECT nextval('" + Viescolaire.ABSC_SCHEMA + ".appel_id_seq') as id" + i + ") ");
                                    if (i != nbrAppelToCreate - 1) {
                                        query.append(",");
                                    }
                                }
                                query.append("SELECT * FROM ");
                                for (int i = 0; i < nbrAppelToCreate; i++) {
                                    query.append("r" + i);
                                    if (i != nbrAppelToCreate - 1) {
                                        query.append(",");
                                    }
                                }
                            }

                            // Récupération des id des futurs nouveaux appels (Nextval)
                            sql.raw(query.toString(), SqlResult.validUniqueResultHandler(new Handler<Either<String, JsonObject>>() {
                                @Override
                                public void handle(Either<String, JsonObject> result) {
                                    if (result.isRight()) {
                                        JsonArray arrayAppelToCreate = new JsonArray();
                                        Map<String, Object> mapIdAppel = result.right().getValue().toMap();

                                        // On transforme la map en liste simple d'id
                                        List<Long> listIdAppelToCreate = new ArrayList<>();
                                        for (Object value : mapIdAppel.values()) {
                                            listIdAppelToCreate.add((Long) value);
                                        }

                                        // On ajoute l'id des futurs nouveaux appels, aux évènements associés.
                                        for (int i = 0; i < arrayEventToCreateWithoutAppel.size(); i++) {
                                            JsonObject eventToCreate = arrayEventToCreateWithoutAppel.get(i);
                                            eventToCreate.putNumber("id_appel", listIdAppelToCreate.get(i));
                                            arrayEventToCreateWithAppel.add(eventToCreate);

                                            arrayAppelToCreate.add(new JsonObject()
                                                    .putNumber("id", eventToCreate.getNumber("id_appel"))
                                                    .putNumber("id_cours", eventToCreate.getNumber("id_cours"))
                                            );
                                        }

                                        JsonArray statements = createStatementSaveZoneAbsence(idUser, idEleve, idMotif, arrayAbscPrevToCreate, arrayAbscPrevToUpdate, arrayAbscPrevToDelete,
                                                listEventIdToUpdate, arrayEventToCreateWithAppel, arrayAppelToCreate, arrayCoursToCreate);
                                        System.out.println("Execute query with handler");
                                        Sql.getInstance().transaction(statements, SqlResult.validResultHandler(handler));
                                    } else if (result.isLeft()) {
                                        System.out.println(result.left().getValue());
                                    }
                                }
                            }));
                        }
                    } else if (result.isLeft()) {
                        System.out.println(result.left().getValue());
                    }
                }
            }));
        }
    }

    private JsonArray createStatementSaveZoneAbsence(String idUser, String idEleve, Integer idMotif,
                                                     JsonArray arrayAbscPrevToCreate,
                                                     JsonArray arrayAbscPrevToUpdate,
                                                     JsonArray arrayAbscPrevToDelete,
                                                     List<Integer> listEventIdToUpdate, JsonArray arrayEventToCreate,
                                                     JsonArray arrayAppelToCreate, JsonArray arrayCoursToCreate){
        JsonArray statements = new JsonArray();
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        // #1 - Insert Absc to create
        if(arrayAbscPrevToCreate != null && !arrayAbscPrevToCreate.toList().isEmpty()) {
            values = new JsonArray();
            query = new StringBuilder();
            // Query & value
            query.append("INSERT INTO " + Viescolaire.ABSC_SCHEMA + ".absence_prev (timestamp_dt, timestamp_fn, id_eleve, id_motif)");
            query.append(" VALUES ");
            for (int i = 0; i < arrayAbscPrevToCreate.size(); i++) {
                JsonObject abscPrevToCreate = arrayAbscPrevToCreate.get(i);

                query.append("(to_timestamp(?,'YYYY-MM-DD HH24:MI'), to_timestamp(?,'YYYY-MM-DD HH24:MI'), ?, ?)");
                values.add(abscPrevToCreate.getString("dateDebutString"));
                values.add(abscPrevToCreate.getString("dateFinString"));
                values.add(idEleve);
                values.add(abscPrevToCreate.getNumber("id_motif"));

                if (i != arrayAbscPrevToCreate.size() - 1) {
                    query.append(",");
                }
            }
            // Ajout du statement
            statements.add(new JsonObject().putString("statement", query.toString())
                    .putArray("values", values).putString("action", "prepared"));
        }

        // #2 - Insert Absc to create
        if(arrayAbscPrevToUpdate != null && !arrayAbscPrevToUpdate.toList().isEmpty()) {
            // Query & value
            for (int i = 0; i < arrayAbscPrevToUpdate.size(); i++) {
                values = new JsonArray();
                query = new StringBuilder();

                query.append("UPDATE " + Viescolaire.ABSC_SCHEMA + ".absence_prev ");
                query.append("SET id_motif = ?, timestamp_dt = to_timestamp(?,'YYYY-MM-DD HH24:MI'), timestamp_fn = to_timestamp(?,'YYYY-MM-DD HH24:MI')");
                query.append("WHERE " + Viescolaire.ABSC_SCHEMA + ".absence_prev.id = ?");

                JsonObject abscPrevToUpdate = arrayAbscPrevToUpdate.get(i);

                values.add(abscPrevToUpdate.getNumber("id_motif"));
                values.add(abscPrevToUpdate.getString("dateDebutString"));
                values.add(abscPrevToUpdate.getString("dateFinString"));
                values.add(abscPrevToUpdate.getNumber("id"));

                // Ajout du statement
                statements.add(new JsonObject().putString("statement", query.toString())
                        .putArray("values", values).putString("action", "prepared"));
            }
        }

        // #3 - Delete Absc Prev
        if(arrayAbscPrevToDelete != null && !arrayAbscPrevToDelete.toList().isEmpty()) {
            values = new JsonArray();
            query = new StringBuilder();

            query.append("DELETE FROM " + Viescolaire.ABSC_SCHEMA + ".absence_prev ");
            query.append("WHERE " + Viescolaire.ABSC_SCHEMA + ".absence_prev.id IN " + Sql.listPrepared(arrayAbscPrevToDelete.toArray()));

            // Query & value
            for (int i = 0; i < arrayAbscPrevToDelete.size(); i++) {
                JsonObject abscPrevToDelete = arrayAbscPrevToDelete.get(i);
                values.add(abscPrevToDelete.getNumber("id"));
            }

            // Ajout du statement
            statements.add(new JsonObject().putString("statement", query.toString())
                    .putArray("values", values).putString("action", "prepared"));
        }

        //#4 - Insert cours
        if(arrayCoursToCreate != null && !arrayCoursToCreate.toList().isEmpty()) {
            values = new JsonArray();
            query = new StringBuilder();
            // Query & value
            query.append("INSERT INTO " + Viescolaire.VSCO_SCHEMA + ".cours ");
            query.append("(id, id_etablissement, timestamp_dt, timestamp_fn, salle, id_matiere, id_classe, id_personnel)");
            query.append(" VALUES ");
            for (int i = 0; i < arrayCoursToCreate.size(); i++) {
                JsonObject coursToCreate = arrayCoursToCreate.get(i);

                query.append("(?, ?, to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS'), to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS'), ?, ?, ?, ?)");
                values.add(coursToCreate.getNumber("id"));
                values.add(coursToCreate.getString("id_etablissement"));
                values.add(coursToCreate.getString("dateDebut"));
                values.add(coursToCreate.getString("dateFin"));
                values.add(coursToCreate.getString("salle"));
                values.add(coursToCreate.getString("id_matiere"));
                values.add(coursToCreate.getString("id_classe"));
                values.add(coursToCreate.getString("id_personnel"));

                if (i != arrayCoursToCreate.size() - 1) {
                    query.append(",");
                }
            }
            // Ajout du statement
            statements.add(new JsonObject().putString("statement", query.toString())
                    .putArray("values", values).putString("action", "prepared"));
        }

        // #5 - Insert appels manquants
        if(arrayAppelToCreate != null && !arrayAppelToCreate.toList().isEmpty()) {
            values = new JsonArray();
            query = new StringBuilder();
            // Query & value
            query.append("INSERT INTO " + Viescolaire.ABSC_SCHEMA + ".appel ");
            query.append("(id, id_cours, id_personnel, owner, saisie_cpe, id_etat)");
            query.append(" VALUES ");
            for (int i = 0; i < arrayAppelToCreate.size(); i++) {
                JsonObject appelToCreate = arrayAppelToCreate.get(i);

                query.append("(?, ?, ?, ?, ?, ?)");
                values.add(appelToCreate.getNumber("id"));
                values.add(appelToCreate.getNumber("id_cours"));
                values.add(appelToCreate.getString("id_personnel"));
                values.add(idUser);
                values.add(true);
                values.add(Viescolaire.ID_ETAT_APPEL_EN_COURS); // id_etat = En cours

                if (i != arrayAppelToCreate.size() - 1) {
                    query.append(",");
                }
            }
            // Ajout du statement
            statements.add(new JsonObject().putString("statement", query.toString())
                    .putArray("values", values).putString("action", "prepared"));
        }

        // #6 - Insert evenements manquants
        if(arrayEventToCreate != null && !arrayEventToCreate.toList().isEmpty()) {
            values = new JsonArray();
            query = new StringBuilder();
            // Query & value
            query.append("INSERT INTO " + Viescolaire.ABSC_SCHEMA + ".evenement ");
            query.append("(id_type, id_motif, id_appel, id_eleve, saisie_cpe, owner)");
            query.append(" VALUES ");
            for (int i = 0; i < arrayEventToCreate.size(); i++) {
                JsonObject eventToCreate = arrayEventToCreate.get(i);

                query.append("(?, ?, ?, ?, ?, ?)");
                values.add(1);
                values.add(idMotif);
                values.add(eventToCreate.getNumber("id_appel"));
                values.add(idEleve);
                values.addBoolean(true);
                values.add(idUser);

                if (i != arrayEventToCreate.size() - 1) {
                    query.append(",");
                }
            }
            // Ajout du statement
            statements.add(new JsonObject().putString("statement", query.toString())
                    .putArray("values", values).putString("action", "prepared"));
        }

        // #6 - Update des evenements
        if(listEventIdToUpdate != null && !listEventIdToUpdate.isEmpty()) {
            values = new JsonArray();
            query = new StringBuilder();
            // Query
            query.append("UPDATE " + Viescolaire.ABSC_SCHEMA + ".evenement SET id_motif = ? WHERE " + Viescolaire.ABSC_SCHEMA);
            query.append(".evenement.id IN " + Sql.listPrepared(listEventIdToUpdate.toArray()) + " RETURNING *");
            // Values
            values.addNumber(idMotif);
            for (Integer eventId : listEventIdToUpdate) {
                values.addNumber(eventId);
            }
            // Ajout du statement
            statements.add(new JsonObject().putString("statement", query.toString())
                    .putArray("values", values).putString("action", "prepared"));
        }

        System.out.println("user : " + idUser);
        System.out.println("Eleve : " + idEleve);
        System.out.println("listEventIdToUpdate : " + (listEventIdToUpdate != null ? listEventIdToUpdate.size() : "null"));
        System.out.println("arrayEventToCreateWithAppel : " + (arrayEventToCreate != null ? arrayEventToCreate.size() : "null"));
        System.out.println("arrayAppelToCreate : " + (arrayAppelToCreate != null ? arrayAppelToCreate.size() : "null"));
        System.out.println("arrayCoursToCreate : " + (arrayCoursToCreate != null ? arrayCoursToCreate.size() : "null"));
        return statements;
    }
}
