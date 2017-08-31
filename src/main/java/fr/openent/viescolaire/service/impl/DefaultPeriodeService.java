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


import fr.openent.Viescolaire;
import fr.openent.viescolaire.service.ClasseService;
import fr.openent.viescolaire.service.GroupeService;
import fr.openent.viescolaire.service.PeriodeService;
import fr.openent.viescolaire.service.UtilsService;
import fr.wseduc.webutils.Either;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.entcore.common.sql.SqlResult.validResultHandler;

/**
 * Created by ledunoiss on 18/10/2016.
 */
public class DefaultPeriodeService extends SqlCrudService implements PeriodeService {

    private final UtilsService utilsService;
    private final GroupeService groupeService;
    private final ClasseService classeService;
    protected static final Logger log = LoggerFactory.getLogger(DefaultPeriodeService.class);

    public DefaultPeriodeService () {
        super(Viescolaire.VSCO_SCHEMA, Viescolaire.VSCO_PERIODE_TABLE);
        utilsService = new DefaultUtilsService();
        groupeService = new DefaultGroupeService();
        classeService = new DefaultClasseService();
    }

    @Override
    public void getPeriodes(final String idEtablissement, final String[] idGroupes, final Handler<Either<String, JsonArray>> handler) {
        if (idEtablissement != null && (idGroupes == null || idGroupes.length == 0)) {
            getPeriodesClasses(idEtablissement, null, handler);
        } else {
            utilsService.getTypeGroupe(idGroupes,
                    new Handler<Either<String, Map<Boolean, List<String>>>>() {
                        @Override
                        public void handle(Either<String, Map<Boolean, List<String>>> stringMapEither) {
                            if (stringMapEither.isRight() && !stringMapEither.right().getValue().isEmpty()) {
                                final List<String> groupes = stringMapEither.right().getValue().get(false);
                                final List<String> classes = stringMapEither.right().getValue().get(true);

                                final AtomicBoolean handled = new AtomicBoolean();
                                final AtomicBoolean groupesDone = new AtomicBoolean(groupes == null);
                                final AtomicBoolean classesDone = new AtomicBoolean(classes == null);
                                final JsonArray result = new JsonArray();

                                final Handler<Either<String, JsonArray>> finalHandler = new Handler<Either<String, JsonArray>>() {
                                    @Override
                                    public void handle(Either<String, JsonArray> stringEntryEither) {
                                        if(!handled.get()) {
                                            if(stringEntryEither.isRight()) {
                                                for(Object o : stringEntryEither.right().getValue()) {
                                                    result.add(o);
                                                }
                                                if(groupesDone.get() && classesDone.get()) {
                                                    handler.handle(new Either.Right<String, JsonArray>(result));
                                                    handled.set(true);
                                                }
                                            } else {
                                                handler.handle(new Either.Left<String, JsonArray>(stringEntryEither.left().getValue()));
                                                handled.set(true);
                                            }
                                        }
                                    }
                                };

                                if (groupes != null && !groupes.isEmpty()) {

                                    final AtomicInteger nbGroupe = new AtomicInteger(groupes.size());
                                    final JsonArray groupePeriodeResult = new JsonArray();
                                    final AtomicBoolean groupeHandled = new AtomicBoolean();

                                    final Handler<Either<String, JsonArray>> groupHandler = new Handler<Either<String, JsonArray>>() {
                                        @Override
                                        public void handle(Either<String, JsonArray> stringEntryEither) {
                                            if (!handled.get()) {
                                                if (stringEntryEither.isRight()) {
                                                    for (Object o : stringEntryEither.right().getValue()) {
                                                        groupePeriodeResult.add(o);
                                                    }
                                                    if (nbGroupe.decrementAndGet() == 0) {
                                                        groupesDone.set(true);
                                                        groupeHandled.set(true);
                                                        finalHandler.handle(new Either.Right<String, JsonArray>(groupePeriodeResult));
                                                    }
                                                } else {
                                                    groupeHandled.set(true);
                                                    finalHandler.handle(new Either.Left<String, JsonArray>(stringEntryEither.left().getValue()));
                                                }
                                            }
                                        }
                                    };

                                    Iterator<String> groupeIterator = groupes.iterator();

                                    while(groupeIterator.hasNext()) {
                                        final String idGroupe = groupeIterator.next();

                                        processGroupes(idEtablissement, idGroupe, new Handler<Either<String, List<String>>>() {
                                            @Override
                                            public void handle(Either<String, List<String>> stringListEither) {
                                                if(stringListEither.isRight()) {
                                                    final String[] idClasses = stringListEither.right().getValue().toArray(new String[0]);
                                                    getPeriodesGroupe(idEtablissement, idClasses,
                                                            new Handler<Either<String, JsonArray>>() {
                                                                @Override
                                                                public void handle(Either<String, JsonArray> stringJsonArrayEither) {
                                                                    if (stringJsonArrayEither.isRight()) {
                                                                        for (Object o : stringJsonArrayEither.right().getValue()) {
                                                                            JsonObject groupePeriode = (JsonObject) o;
                                                                            groupePeriode.putString("id_groupe", idGroupe);
                                                                        }
                                                                        groupHandler.handle(new Either.Right<String, JsonArray>(stringJsonArrayEither.right().getValue()));
                                                                    } else {
                                                                        groupHandler.handle(new Either.Left<String, JsonArray>(stringJsonArrayEither.left().getValue()));
                                                                    }
                                                                }
                                                            });
                                                } else {
                                                    groupHandler.handle(new Either.Left<String, JsonArray>(stringListEither.left().getValue()));
                                                }
                                            }
                                        });
                                    }
                                }

                                if (classes != null && !classes.isEmpty()) {
                                    getPeriodesClasses(idEtablissement, classes.toArray(new String[0]), new Handler<Either<String, JsonArray>>() {
                                        @Override
                                        public void handle(Either<String, JsonArray> stringJsonArrayEither) {
                                            if (stringJsonArrayEither.isRight()) {
                                                classesDone.set(true);
                                                finalHandler.handle(stringJsonArrayEither.right());
                                            } else {
                                                finalHandler.handle(stringJsonArrayEither.left());
                                            }
                                        }
                                    });
                                }

                            } else if (stringMapEither.isRight() && stringMapEither.right().getValue().isEmpty()) {
                                handler.handle(new Either.Left<String, JsonArray>("getPeriodes : no class or group found"));
                            } else {
                                handler.handle(new Either.Left<String, JsonArray>(stringMapEither.left().getValue()));
                            }
                        }
                    });
        }
    }

    @Override
    public void getTypePeriodes(Handler<Either<String, JsonArray>> handler) {
        JsonArray fields = new JsonArray();
        fields.addString("id").addString("type").addString("ordre");
        Sql.getInstance().select("viesco.rel_type_periode", fields, validResultHandler(handler));
    }

    @Override
    public void createPeriodes(final String idEtablissement, final String[] idClasses, final JsonObject[] periodes,
                               final Handler<Either<String, JsonArray>> handler) {

        checkGroupeEtab(idClasses, new Handler<Either<String, String>>() {
            @Override
            public void handle(Either<String, String> stringStringEither) {
                if (stringStringEither.isRight() && stringStringEither.right().getValue().equals(idEtablissement)) {

                    getTypePeriode(periodes, new Handler<Either<String, JsonObject[]>>() {
                        @Override
                        public void handle(Either<String, JsonObject[]> stringEither) {
                            if (stringEither.isRight()) {

                                JsonArray statement = new JsonArray();

                                for (JsonObject periode : periodes) {
                                    statement.addObject(createPeriodeStatement(idEtablissement, idClasses, periode));
                                }

                            } else {
                                handler.handle(new Either.Left<String, JsonArray>(stringEither.left().getValue()));
                            }
                        }
                    });

                } else if (stringStringEither.isRight()
                        && !stringStringEither.right().getValue().equals(idEtablissement)) {
                    handler.handle(new Either.Left<String, JsonArray>(
                            "createPeriodes : Groups aren't in the given structure."));
                } else {
                    handler.handle(new Either.Left<String, JsonArray>(stringStringEither.left().getValue()));
                }
            }
        });
    }

    @Override
    public void updatePeriodes(final String idEtablissement, final String[] idClasses, final JsonObject[] periodes,
                               final Handler<Either<String, JsonArray>> handler) {
        checkGroupeEtab(idClasses, new Handler<Either<String, String>>() {
            @Override
            public void handle(Either<String, String> stringStringEither) {
                if(stringStringEither.isRight() && stringStringEither.right().getValue().equals(idEtablissement)) {
                    utilsService.getTypeGroupe(idClasses, new Handler<Either<String, Map<Boolean, List<String>>>>() {
                        @Override
                        public void handle(Either<String, Map<Boolean, List<String>>> stringMapEither) {
                            if (stringMapEither.isRight() && !stringMapEither.right().getValue().keySet().contains(false)) {
                                getTypePeriode(periodes, new Handler<Either<String, JsonObject[]>>() {
                                    @Override
                                    public void handle(Either<String, JsonObject[]> stringEither) {
                                        if (stringEither.isRight()) {
                                            final JsonObject[] periodesWithType = stringEither.right().getValue();
                                            updatePeriodeTransaction(idEtablissement, idClasses, periodesWithType, handler);
                                        } else {
                                            handler.handle(new Either.Left<String, JsonArray>(stringEither.left().getValue()));
                                        }
                                    }
                                });
                            } else if (stringMapEither.right().getValue().keySet().contains(false)) {
                                handler.handle(new Either.Left<String, JsonArray>("updatePeriodes : One of the given id isn't " +
                                        "of a class"));
                            } else {
                                handler.handle(new Either.Left<String, JsonArray>(stringMapEither.left().getValue()));
                            }
                        }
                    });
                } else if (stringStringEither.isRight()
                        && !stringStringEither.right().getValue().equals(idEtablissement)) {
                    handler.handle(new Either.Left<String, JsonArray>(
                            "createPeriodes : Groups aren't in the given structure."));
                } else {
                    handler.handle(new Either.Left<String, JsonArray>(stringStringEither.left().getValue()));
                }
            }
        });
    }

    @Override
    public void deletePeriodes(final Long[] idPeriodes, final Handler<Either<String, JsonArray>> handler) {

        checkEvalOnPeriode(idPeriodes, new Handler<Either<String, JsonObject>>() {
            @Override
            public void handle(Either<String, JsonObject> stringJsonObjectEither) {
                if(stringJsonObjectEither.isRight()) {
                    Sql.getInstance().transaction(new JsonArray().addObject(deletePeriodeStatement(idPeriodes)),
                            validResultHandler(handler));
                } else {
                    handler.handle(new Either.Left<String, JsonArray>(stringJsonObjectEither.left().getValue()));
                }
            }
        });

    }

    @Override
    public void checkEvalOnPeriode(Long[] idPeriodes, final Handler<Either<String, JsonObject>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();
        query.append("SELECT devoirs.id")
                .append("FROM notes.devoirs ")
                .append("LEFT JOIN notes.rel_devoirs_groupes ")
                .append("ON rel_devoirs_groupes.id_devoir = devoirs.id ")
                .append("LEFT JOIN viesco.periode ")
                .append("ON devoirs.id_periode = periode.id_type")
                .append("AND rel_devoirs_groupe.id_groupe = periode.id_classe")
                .append("AND devoirs.id_etablissement = periode.id_etablissement")
                .append("WHERE periode.id IN " + Sql.listPrepared(idPeriodes));

        for (Long id : idPeriodes) {
            values.addNumber(id);
        }

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> stringJsonArrayEither) {
                if (stringJsonArrayEither.isRight()) {
                    if(stringJsonArrayEither.right().getValue().size() == 0) {
                        handler.handle(new Either.Right<String, JsonObject>(new JsonObject()));
                    } else {
                        handler.handle(new Either.Left<String, JsonObject>("checkEvalOnPeriode : Given periods contain" +
                                "assignements."));
                    }
                } else {
                    handler.handle(new Either.Left<String, JsonObject>(stringJsonArrayEither.left().getValue()));
                }
            }
        }));
    }

    @Override
    public void getTypePeriode(final JsonObject[] periodes, final Handler<Either<String, JsonObject[]>> handler) {

        //////////////// Sorting periods ////////////////
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final AtomicBoolean sortError = new AtomicBoolean();

        Collections.sort(Arrays.asList(periodes), new Comparator<JsonObject>() {
            @Override
            public int compare(JsonObject o1, JsonObject o2) {
                try {
                    Date o1_timestamp_dt = dateFormat.parse(o1.getString("timestamp_dt"));
                    Date o2_timestamp_dt = dateFormat.parse(o2.getString("timestamp_dt"));

                    return o1_timestamp_dt.compareTo(o2_timestamp_dt);
                } catch (ParseException e) {
                    sortError.set(true);
                    e.printStackTrace();
                    return 0;
                }
            }
        });

        if (sortError.get()) {
            handler.handle(new Either.Left<String, JsonObject[]>("getTypePeriode : An error occured while sorting periods"));
        }

        //////////////// Setting id type by period ////////////////
        final JsonArray values = new JsonArray();
        values.addNumber(periodes.length);

        Sql.getInstance().prepared("SELECT * FROM viesco.rel_type_periode WHERE type = ? ORDRE BY ordre", values,
                validResultHandler(new Handler<Either<String, JsonArray>>() {
                    @Override
                    public void handle(Either<String, JsonArray> stringJsonArrayEither) {
                        if (stringJsonArrayEither.isRight()) {
                            JsonArray result = stringJsonArrayEither.right().getValue();

                            // Si aucun type ne correspond au nombre de période à définir, la table de type est incomplète
                            if (result.size() != periodes.length) {
                                handler.handle(new Either.Left<String, JsonObject[]>("updatePeriodesType :  No periode type " +
                                        "defined for the given number of periode"));
                            }

                            for (int i = 0; i < result.size(); i++) {
                                periodes[i].putNumber("id_type", ((JsonObject) result.get(i)).getLong("id"));
                            }

                            handler.handle(new Either.Right<String, JsonObject[]>(periodes));
                        } else {
                            handler.handle(new Either.Left<String, JsonObject[]>(stringJsonArrayEither.left().getValue()));
                        }
                    }
                }));
    }

    @Override
    public void getPeriodesGroupe(String idEtablissement, String[] idClasses, final Handler<Either<String, JsonArray>> handler) {

        if(idEtablissement == null && (idClasses == null || idClasses.length == 0)) {
            handler.handle(new Either.Left<String, JsonArray>("getPeriodesGroupe : No parameter given."));
        }

        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT periode.id_type, MAX(periode.timestamp_dt) AS timestamp_dt, ")
                .append("MIN(timestamp_fn) AS timestamp_fn, MIN(date_fin_saisie) AS date_fin_saisie ")
                .append("FROM viesco.periode ")
                .append("WHERE ");

        if(idEtablissement != null) {
            query.append("periode.id_etablissement = ? ");
            values.addString(idEtablissement);
        }
        if(idEtablissement != null && idClasses != null && idClasses.length > 0) {
            query.append(" AND ");
        }
        if(idClasses != null && idClasses.length > 0) {
            query.append("periode.id_classe IN ").append(Sql.listPrepared(idClasses));

            for (String id_groupe : idClasses) {
                values.addString(id_groupe);
            }
        }
        query.append(" GROUP BY periode.id_type");

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }

    @Override
    public void getPeriodesClasses(String idEtablissement, String[] idClasses,
                                   final Handler<Either<String, JsonArray>> handler) {

        if (idEtablissement == null && (idClasses == null || idClasses.length == 0)) {
            handler.handle(new Either.Left<String, JsonArray>("getPeriodesClasses : No parameter given."));
        }

        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT periode.* ")
                .append("FROM " + Viescolaire.VSCO_SCHEMA + ".periode WHERE ");

        if (idEtablissement != null) {
            query.append("periode.id_etablissement = ? ");
            values.addString(idEtablissement);
        }
        if (idEtablissement != null && idClasses != null && idClasses.length > 0) {
            query.append(" AND ");
        }
        if (idClasses != null && idClasses.length > 0) {
            query.append("id_classe IN ").append(Sql.listPrepared(idClasses));
            for (String id_classe : idClasses) {
                values.addString(id_classe);
            }
        }

        query.append(" ORDER BY id_etablissement, id_classe, timestamp_dt ");

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }

    /**
     * S'assure de la coherence des periodes pour les classes liees au groupe passe en parametre
     * @param idEtablissement   identifiant de l'etablissement du groupe
     * @param idGroupe          identifiant du groupe
     * @param handler           handler portant le resultat
     */
    private void processGroupes(final String idEtablissement, String idGroupe, final Handler<Either<String, List<String>>> handler) {

        groupeService.getClasseGroupe(new String[]{idGroupe},
                new Handler<Either<String, JsonArray>>() {
                    @Override
                    public void handle(final Either<String, JsonArray> stringJsonArrayEither) {
                        if (stringJsonArrayEither.isRight()) {
                            List<String> idClasses = new ArrayList<>();
                            for(Object o : ((JsonObject) stringJsonArrayEither.right().getValue().iterator().next()).getArray("id_classes")) {
                                idClasses.add((String) o);
                            }

                            // On compte le nombre de periode, pour chaque groupe de classe. Si l'une d'elles a
                            // un nombre different de periode, une erreur survient.
                            getPeriodesClasses(idEtablissement, idClasses.toArray(new String[0]), new Handler<Either<String, JsonArray>>() {
                                @Override
                                public void handle(Either<String, JsonArray> stringJsonArrayEither) {
                                    if (stringJsonArrayEither.isRight()) {

                                        Map<String, AtomicInteger> nbPeriodeClasse = new HashMap<>();

                                        for (Object o : stringJsonArrayEither.right().getValue()) {
                                            JsonObject periodeClasse = (JsonObject) o;
                                            String id_classe = periodeClasse.getString("id_classe");
                                            if (!nbPeriodeClasse.containsKey(id_classe)) {
                                                nbPeriodeClasse.put(id_classe, new AtomicInteger());
                                            }
                                            nbPeriodeClasse.get(id_classe).incrementAndGet();
                                        }

                                        Iterator<AtomicInteger> iter = nbPeriodeClasse.values().iterator();
                                        AtomicInteger first = iter.next();

                                        while (iter.hasNext()) {
                                            if (first.get() != iter.next().get()) {
                                                handler.handle(new Either.Left<String, List<String>>("getPeriodesGroupe : Given classes have different type of periods"));
                                            }
                                        }

                                        handler.handle(new Either.Right<String, List<String>>(new ArrayList<>(nbPeriodeClasse.keySet())));

                                    } else {
                                        handler.handle(new Either.Left<String, List<String>>(stringJsonArrayEither.left().getValue()));
                                    }
                                }
                            });
                        } else {
                            handler.handle(new Either.Left<String, List<String>>(stringJsonArrayEither.left().getValue()));
                        }
                    }
                });
    }

    private void checkGroupeEtab(final String[] idGroupes, final Handler<Either<String, String>> handler) {
        classeService.getEtabClasses(idGroupes, new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> stringJsonArrayEither) {
                if (stringJsonArrayEither.isRight()) {
                    final Map<String, List<String>> mapEtab = new HashMap<>();

                    for (Object o : stringJsonArrayEither.right().getValue()) {
                        JsonObject etab = (JsonObject) o;
                        mapEtab.put(etab.getString("idStructure"),
                                etab.getArray("idClasses").toList());
                    }

                    if (mapEtab.keySet().size() > 1) {
                        handler.handle(new Either.Left<String, String>(
                                "checkGroupeEtab : Groups aren't in the same structure."));
                    } else {
                        List<String> error = new ArrayList<>();
                        String idEtablissement = mapEtab.keySet().iterator().next();
                        for (String idGroupe : idGroupes) {
                            if (!mapEtab.get(idEtablissement).contains(idGroupe)) {
                                error.add(idGroupe);
                            }
                        }
                        if (!error.isEmpty()) {
                            handler.handle(new Either.Left<String, String>(
                                    "checkGroupeEtab : Groups not recognised " + error.toString()));
                        } else {
                            handler.handle(new Either.Right<String, String>(idEtablissement));
                        }
                    }
                } else {
                    handler.handle(new Either.Left<String, String>(stringJsonArrayEither.left().getValue()));
                }
            }
        });
    }

    private void updatePeriodeTransaction(final String idEtablissement, final String[] idClasses,
                                          final JsonObject[] periodes,
                                          final Handler<Either<String, JsonArray>> handler) {

        final AtomicBoolean notHandled = new AtomicBoolean();
        final Map<String, Boolean> operationDone = new HashMap<>();
        operationDone.put("update", false);
        operationDone.put("create", false);
        operationDone.put("delete", false);
        final JsonArray statement = new JsonArray();

        final Handler<Either<String, JsonArray>> resultHandler = new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> stringAtomicIntegerEither) {
                if (notHandled.get()) {
                    if (stringAtomicIntegerEither.isRight()) {
                        if (!operationDone.values().contains(false)) {
                            notHandled.set(false);
                            handler.handle(stringAtomicIntegerEither.right());
                        }
                    } else {
                        notHandled.set(false);
                        handler.handle(stringAtomicIntegerEither.left());
                    }
                }
            }
        };

        getPeriodesClasses(idEtablissement, idClasses, new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(final Either<String, JsonArray> stringJsonArrayEither) {
                if (stringJsonArrayEither.isRight()) {

                    final List<List<Long>> toUpdate = new ArrayList<>();
                    final List<Long> flattenedToUpdate = new ArrayList<>();

                    for (Object o : stringJsonArrayEither.right().getValue()) {
                        JsonObject periode = (JsonObject) o;
                        utilsService.addToList(periode.getLong("id"),
                                periode.getInteger("ordre"), toUpdate);
                        flattenedToUpdate.add(periode.getLong("id"));
                    }

                    checkEvalOnPeriode(flattenedToUpdate.toArray(new Long[0]), new Handler<Either<String, JsonObject>>() {
                        @Override
                        public void handle(Either<String, JsonObject> stringBooleanEither) {
                            if (stringBooleanEither.isRight()) {

                                for (int i = 0; i < periodes.length; i++) {
                                    statement.add(updatePeriodeStatement(toUpdate.get(i).toArray(new Long[0]), periodes[i]));
                                }

                                resultHandler.handle(new Either.Right<String, JsonArray>(statement));
                            } else {
                                handler.handle(new Either.Left<String, JsonArray>(stringBooleanEither.left().getValue()));
                            }
                        }
                    });

                } else {
                    resultHandler.handle(new Either.Left<String, JsonArray>(
                            stringJsonArrayEither.left().getValue()));
                }
            }
        });

        findPeriodeToAdd(idClasses, (long) periodes.length, new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> stringJsonArrayEither) {
                if (stringJsonArrayEither.isRight()) {
                    List<List<String>> toAdd = new ArrayList<>();

                    for (Object o : stringJsonArrayEither.right().getValue()) {
                        JsonObject classe = (JsonObject) o;
                        for (int i = 0; i < classe.getNumber("nbtoadd").longValue(); i++) {
                            utilsService.addToList(classe.getString("id_classe"),
                                    periodes.length - i, toAdd);
                        }
                    }

                    for (JsonObject periode : periodes) {
                        statement.add(createPeriodeStatement(idEtablissement, idClasses, periode));
                    }

                    resultHandler.handle(new Either.Right<String, JsonArray>(statement));
                } else {
                    resultHandler.handle(new Either.Left<String, JsonArray>(stringJsonArrayEither.left().getValue()));
                }
            }
        });

        findPeriodeToDelete(idClasses, (long) periodes.length, new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> stringJsonArrayEither) {
                if (stringJsonArrayEither.isRight()) {
                    ArrayList<Long> toDelete = new ArrayList<>();

                    for (Object o : stringJsonArrayEither.right().getValue()) {
                        toDelete.add(((JsonObject) o).getNumber("id_periode").longValue());
                    }

                    statement.add(deletePeriodeStatement(toDelete.toArray(new Long[0])));

                    resultHandler.handle(new Either.Right<String, JsonArray>(statement));
                } else {
                    resultHandler.handle(new Either.Left<String, JsonArray>(stringJsonArrayEither.left().getValue()));
                }
            }
        });
    }

    /**
     * repertorie les periodes surnuméraires pour une liste de classe en fonction du type de periode
     *
     * @param idClasses la liste de classe a selectionner
     * @param type      le type de periode a comparer
     * @param handler   handler portant le resultat de la requete
     */
    private void findPeriodeToDelete(String[] idClasses, Long type, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT periode.id AS id_periode FROM viesco.periode ")
                .append("LEFT JOIN viesco.rel_type_periode ON periode.id_type = rel_type_periode.id ")
                .append("WHERE periode.id_classe IN " + Sql.listPrepared(idClasses))
                .append("AND rel_type_periode.type > ?");

        for (String idClasse : idClasses) {
            values.addString(idClasse);
        }
        values.addNumber(type);

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }

    /**
     * repertorie les periodes a ajouter pour une liste de classe en fonction du type de periode
     *
     * @param idClasses la liste de classe a selectionner
     * @param type      le type de periode a comparer
     * @param handler   handler portant le resultat de la requete
     */
    private void findPeriodeToAdd(String[] idClasses, Long type, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT * FROM ")
                .append("(SELECT id_classe, ? - COUNT(id) as nbToAdd FROM viesco.periode ")
                .append("WHERE periode.id_classe IN " + Sql.listPrepared(idClasses))
                .append(" GROUP BY id_classe) ")
                .append("AS subQuery WHERE nbToAdd != 0 ");

        values.addNumber(type);
        for (String idClasse : idClasses) {
            values.addString(idClasse);
        }

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }

    /**
     * met à jour les dates d'une liste de periodes
     *
     * @param idPeriodes les identifiants des periodes a mettre a jour
     * @param periode    donnees utilisees pour la mise a jour
     */
    private JsonObject updatePeriodeStatement(Long[] idPeriodes, JsonObject periode) {

        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();


        query.append("UPDATE viesco.periode ")
                .append("SET  timestamp_dt = to_timestamp(?,'YYYY-MM-DD'), ")
                .append("timestamp_fn = to_timestamp(?,'YYYY-MM-DD'), ")
                .append("date_fin_saisie = to_timestamp(?,'YYYY-MM-DD'), ")
                .append("id_type = ? ")
                .append("WHERE id IN " + Sql.listPrepared(idPeriodes) + ";");

        values.addString(periode.getString("timestamp_dt"))
                .addString(periode.getString("timestamp_fn"))
                .addString(periode.getString("date_fin_saisie"))
                .addNumber(periode.getNumber("id_type"));

        for (Long idPeriode : idPeriodes) {
            values.addNumber(idPeriode);
        }

        return new JsonObject()
                .putString("statement", query.toString())
                .putArray("values", values)
                .putString("action", "prepared");
    }

    /**
     * Creer des periodes pour les classes passees en parametre
     *
     * @param idEtablissement identifiant de l'établissement pour lesquel les periode seront creees
     * @param idClasses       identifiants des classes pour lesquelles les periode seront creees
     * @param periode          periode a inserer en base
     */
    private JsonObject createPeriodeStatement(String idEtablissement, String[] idClasses, JsonObject periode) {

        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("INSERT INTO viesco.periode ")
                .append("(id_etablissement, id_classe, timestamp_dt, timestamp_fn, date_fin_saisie,  id_type) ")
                .append("VALUES  ");

        query.append("( ?, UNNEST(" + Sql.arrayPrepared(idClasses) + "), to_timestamp(?,'YYYY-MM-DD'), ");
        query.append("to_timestamp(?,'YYYY-MM-DD'), to_timestamp(?,'YYYY-MM-DD'), ?)");
        values.addString(idEtablissement);
        for (String idClasse : idClasses) {
            values.addString(idClasse);
        }
        values.addString(periode.getString("timestamp_dt"));
        values.addString(periode.getString("timestamp_fn"));
        values.addString(periode.getString("date_fin_saisie"));
        values.addString(periode.getString("id_type"));

        return new JsonObject()
                .putString("statement", query.toString())
                .putArray("values", values)
                .putString("action", "prepared");
    }

    /**
     * Supprime les periodes passees en parametre
     *
     * @param idPeriodes    identifiants des periodes a supprimer
     */
    private JsonObject deletePeriodeStatement(Long[] idPeriodes) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("DELETE FROM viesco.periode WHERE id IN " + Sql.listPrepared(idPeriodes));

        for(Long id : idPeriodes) {
            values.addNumber(id);
        }

        return new JsonObject()
                .putString("statement", query.toString())
                .putArray("values", values)
                .putString("action", "prepared");
    }
}
