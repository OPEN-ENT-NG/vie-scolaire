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
import fr.wseduc.webutils.I18n;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
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

import static fr.wseduc.webutils.http.Renders.getHost;
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
    public String getLibellePeriode(Integer type, Integer ordre, HttpServerRequest request){
        StringBuilder PeriodeLibelle = new StringBuilder();
        if(type != null & ordre != null){
            String periodeType =  I18n.getInstance().translate("viescolaire.periode."+type, getHost(request), I18n.acceptLanguage(request));

            PeriodeLibelle.append(ordre.toString());
            if(ordre == 1){
                PeriodeLibelle.append(" er "+periodeType);
            }else{
                PeriodeLibelle.append(" eme "+periodeType);
            }

        }else{
            log.error("Error replacing i18n variable");
        }
        return PeriodeLibelle.toString();
    }

    @Override
    public void getPeriodes(final String idEtablissement, final String[] idGroupes, final Handler<Either<String, JsonArray>> handler) {
        if (idEtablissement != null && (idGroupes == null || idGroupes.length == 0)) {
            getPeriodesClasses(idEtablissement, null, handler);
        } else {
            getTypeGroupe(idGroupes,
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
    public void updatePeriodes(final String idEtablissement, final String[] idClasses, final JsonObject[] periodes,
                               final Handler<Either<String, JsonArray>> handler) {
        checkEvalOnPeriode(idClasses, new Handler<Either<String, JsonObject>>() {
            @Override
            public void handle(Either<String, JsonObject> stringBooleanEither) {
                if (stringBooleanEither.isRight() && !stringBooleanEither.right().getValue().getBoolean("exist")) {
                    checkGroupeEtab(idClasses, new Handler<Either<String, String>>() {
                        @Override
                        public void handle(Either<String, String> stringStringEither) {
                            if (stringStringEither.isRight() && stringStringEither.right().getValue().equals(idEtablissement)) {
                                verifCoherencePeriodes(periodes, new Handler<Either<JsonObject, Boolean>>() {
                                    @Override
                                    public void handle(Either<JsonObject, Boolean> jsonObjectBooleanEither) {
                                        if (jsonObjectBooleanEither.isRight()) {
                                            getTypeGroupe(idClasses, new Handler<Either<String, Map<Boolean, List<String>>>>() {
                                                @Override
                                                public void handle(Either<String, Map<Boolean, List<String>>> stringMapEither) {
                                                    if (stringMapEither.isRight() && !stringMapEither.right().getValue().keySet().contains(false)) {
                                                        getTypePeriode(periodes, new Handler<Either<String, JsonObject[]>>() {
                                                            @Override
                                                            public void handle(Either<String, JsonObject[]> stringEither) {
                                                                if (stringEither.isRight()) {
                                                                    final JsonObject[] periodesWithType = stringEither.right().getValue();

                                                                    JsonArray statement = new JsonArray();

                                                                    statement.add(deletePeriodeStatement(idClasses));
                                                                    statement.add(createPeriodeStatement(idEtablissement, idClasses, periodesWithType));

                                                                    Sql.getInstance().transaction(statement,
                                                                            validResultHandler(handler));
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
                                        } else {
                                            handler.handle(new Either.Left<String, JsonArray>(jsonObjectBooleanEither.left().getValue().encode()));
                                        }
                                    }
                                });
                            } else if (stringStringEither.isRight()
                                    && !stringStringEither.right().getValue().equals(idEtablissement)) {
                                handler.handle(new Either.Left<String, JsonArray>(
                                        "updatePeriodes : Groups aren't in the given structure."));
                            } else {
                                handler.handle(new Either.Left<String, JsonArray>(stringStringEither.left().getValue()));
                            }
                        }
                    });
                } else if (stringBooleanEither.right().getValue().getBoolean("exist")) {
                    handler.handle(new Either.Left<String, JsonArray>(String.valueOf(stringBooleanEither.right().getValue().getBoolean("exist"))));
                } else {
                    handler.handle(new Either.Left<String, JsonArray>(stringBooleanEither.left().getValue()));
                }
            }
        });
    }

    @Override
    public void checkEvalOnPeriode(String[] idClasses, final Handler<Either<String, JsonObject>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();
        query.append("SELECT devoirs.id ")
                .append("FROM notes.devoirs ")
                .append("LEFT JOIN notes.rel_devoirs_groupes ")
                .append("ON rel_devoirs_groupes.id_devoir = devoirs.id ")
                .append("WHERE rel_devoirs_groupes.id_groupe IN " + Sql.listPrepared(idClasses));

        for (String id : idClasses) {
            values.addString(id);
        }

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> stringJsonArrayEither) {
                if (stringJsonArrayEither.isRight()) {
                    if(stringJsonArrayEither.right().getValue().size() == 0) {
                        handler.handle(new Either.Right<String, JsonObject>(new JsonObject().putBoolean("exist", false)));
                    } else {
                        handler.handle(new Either.Right<String, JsonObject>(new JsonObject().putBoolean("exist", true)));
                    }
                } else {
                    handler.handle(new Either.Left<String, JsonObject>(stringJsonArrayEither.left().getValue()));
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

    /**
     * Retourne les periodes decorees de l'id de leur type
     *
     * @param periodes periodes a completer
     * @param handler  handler portant le resultat de la requete
     */
    private void getTypePeriode(final JsonObject[] periodes, final Handler<Either<String, JsonObject[]>> handler) {

        //////////////// Sorting periods ////////////////
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
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

        Sql.getInstance().prepared("SELECT * FROM viesco.rel_type_periode WHERE type = ? ORDER BY ordre", values,
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

    private void verifCoherencePeriodes(JsonObject[] periodes, final Handler<Either<JsonObject, Boolean>> handler) {

        final List<Map<String, Object>> errorList = new ArrayList<>();
        for (int i = 0; i < periodes.length; i++) {
            Map<String, Object> temporaryMap = new HashMap<>();
            errorList.add(temporaryMap);
        }
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        for (int i = 0; i < periodes.length; i++) {
            JsonObject periode = periodes[i];
            try {
                Calendar timestamp_dt = Calendar.getInstance();
                timestamp_dt.setTime(dateFormat.parse(periode.getString("timestamp_dt")));
                Calendar timestamp_fn = Calendar.getInstance();
                timestamp_fn.setTime(dateFormat.parse(periode.getString("timestamp_fn")));
                Calendar date_fin_saisie = Calendar.getInstance();
                date_fin_saisie.setTime(dateFormat.parse(periode.getString("date_fin_saisie")));

                // Erreur date_debut posterieur date_fin
                if(timestamp_dt.after(timestamp_fn)
                        ||(timestamp_dt.get(Calendar.YEAR) == timestamp_fn.get(Calendar.YEAR)
                        && timestamp_dt.get(Calendar.DAY_OF_YEAR) == timestamp_fn.get(Calendar.DAY_OF_YEAR))) {
                    errorList.get(i).put("errorFn", "La date de fin ne peut être anterieur à la date de début.");
                }

                // Erreur date_debut posterieur date_fin_saisie
                if (timestamp_dt.after(date_fin_saisie)
                        || (timestamp_dt.get(Calendar.YEAR) == date_fin_saisie.get(Calendar.YEAR)
                        && timestamp_dt.get(Calendar.DAY_OF_YEAR) == date_fin_saisie.get(Calendar.DAY_OF_YEAR))) {
                    errorList.get(i).put("errorFnS", "La date de fin de saisie ne peut être anterieur à la date de début.");
                }

                // Erreur chevauchement des periodes
                for (int j = 0; i < periodes.length; i++) {
                    if(i == j) {
                        // On compare la période en cours avec les autres périodes
                        continue;
                    }
                    JsonObject periodeBis = periodes[j];
                    Calendar timestamp_dtBis = Calendar.getInstance();
                    timestamp_dt.setTime(dateFormat.parse(periodeBis.getString("timestamp_dt")));
                    Calendar timestamp_fnBis = Calendar.getInstance();
                    timestamp_fn.setTime(dateFormat.parse(periodeBis.getString("timestamp_fn")));

                    if((timestamp_dt.before(timestamp_dtBis) && timestamp_fn.after(timestamp_dtBis))
                    || (timestamp_dt.before(timestamp_fnBis) && timestamp_fn.after(timestamp_fnBis))) {
                        errorList.get(i).put("errorOver", "La periode chevauche une autre periode.");
                    }
                }

                // Erreur periodes non contigues && periodes non ordonnees
                if (i - 1 >= 0) {
                    Calendar timestamp_previous = Calendar.getInstance();
                    timestamp_previous.setTime(dateFormat.parse(periode.getString("timestamp_fn")));

                    if(timestamp_dt.get(Calendar.DAY_OF_YEAR) - timestamp_previous.get(Calendar.DAY_OF_YEAR) > 1) {
                        errorList.get(i).put("errorContigPrev", "La periode n'est pas contigue a la periode precedente.");
                    }
                }
                if (i + 1 <= periodes.length) {
                    Calendar timestamp_next = Calendar.getInstance();
                    timestamp_next.setTime(dateFormat.parse(periode.getString("timestamp_dt")));

                    if (timestamp_next.get(Calendar.DAY_OF_YEAR) - timestamp_dt.get(Calendar.DAY_OF_YEAR) > 1) {
                        errorList.get(i).put("errorContigNext", "La periode n'est pas contigue a la periode suivante.");
                    }
                }

            } catch(ParseException e){
                errorList.get(i).put("errorParsing", "Parsing Error");
            }
        }

        Boolean error = false;
        for (Map<String, Object> errorPeriode : errorList) {
            if (!errorPeriode.isEmpty()) {
                error = true;
            }
        }

        if(!error) {
            handler.handle(new Either.Right<JsonObject, Boolean>(Boolean.TRUE));
        } else {
            JsonObject result = new JsonObject();
            for (int i = 0; i < periodes.length; i++) {
                if(errorList.get(i) != null) {
                    result.putObject("periode " + String.valueOf(i), new JsonObject(errorList.get(i)));
                }
            }
            handler.handle(new Either.Left<JsonObject, Boolean>(result));
        }
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

    /**
     * Creer des periodes pour les classes passees en parametre
     *
     * @param idEtablissement identifiant de l'établissement pour lesquel les periode seront creees
     * @param idClasses       identifiants des classes pour lesquelles les periode seront creees
     * @param periodes        periodes a inserer en base
     */
    private JsonObject createPeriodeStatement(String idEtablissement, String[] idClasses, JsonObject[] periodes) {

        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("INSERT INTO viesco.periode ")
                .append("(id_etablissement, id_classe, timestamp_dt, timestamp_fn, date_fin_saisie,  id_type) ")
                .append("VALUES  ");

        for (int i = 0; i < periodes.length; i++) {
            JsonObject periode = periodes[i];
            for(String idClasse : idClasses) {
                query.append("( ?, ?, to_timestamp(?,'YYYY-MM-DD'), ");

                query.append("to_timestamp(?,'YYYY-MM-DD'), to_timestamp(?,'YYYY-MM-DD'), ?),");
                values.addString(idEtablissement);
                values.addString(idClasse);
                values.addString(periode.getString("timestamp_dt"));
                values.addString(periode.getString("timestamp_fn"));
                values.addString(periode.getString("date_fin_saisie"));
                values.addNumber(periode.getNumber("id_type"));
            }
        }
        query.deleteCharAt(query.length() - 1);
        return new JsonObject()
                .putString("statement", query.toString())
                .putArray("values", values)
                .putString("action", "prepared");
    }

    /**
     * Supprime les periodes passees en parametre
     *
     * @param idClasses    identifiants de classe des periodes a supprimer
     */
    private JsonObject deletePeriodeStatement(String[] idClasses) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("DELETE FROM viesco.periode WHERE id_classe IN " + Sql.listPrepared(idClasses));

        for(String id : idClasses) {
            values.addString(id);
        }

        return new JsonObject()
                .putString("statement", query.toString())
                .putArray("values", values)
                .putString("action", "prepared");
    }

    private void getTypeGroupe(String[] id_classes, final Handler<Either<String, Map<Boolean, List<String>>>> handler) {
        utilsService.getTypeGroupe(id_classes, new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> stringJsonArrayEither) {
                if (stringJsonArrayEither.isRight()) {
                    Map<Boolean, List<String>> result = new HashMap<>();

                    for (Object o : stringJsonArrayEither.right().getValue()) {
                        JsonObject classe = (JsonObject) o;
                        if(!result.containsKey(classe.getBoolean("isClass"))) {
                            result.put(classe.getBoolean("isClass"), new ArrayList<String>());
                        }
                        result.get(classe.getBoolean("isClass")).add(classe.getString("id"));
                    }

                    handler.handle(new Either.Right<String, Map<Boolean, List<String>>>(result));
                } else {
                    handler.handle(
                            new Either.Left<String, Map<Boolean, List<String>>>(stringJsonArrayEither.left().getValue()));
                }
            }
        });
    }
}
