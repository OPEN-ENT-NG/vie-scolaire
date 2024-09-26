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
import fr.openent.viescolaire.core.constants.Field;
import fr.openent.viescolaire.service.ClasseService;
import fr.openent.viescolaire.service.GroupeService;
import fr.openent.viescolaire.service.PeriodeService;
import fr.openent.viescolaire.service.UtilsService;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.I18n;
import io.vertx.core.eventbus.DeliveryOptions;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static fr.wseduc.webutils.http.Renders.getHost;
import static org.entcore.common.sql.SqlResult.*;

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
    public void getLibellePeriode(Long idTypePeriode, final HttpServerRequest request, final Handler<Either<String, String>> handler){
        StringBuilder query = new StringBuilder();
        JsonArray params = new JsonArray();

        query.append("SELECT type, ordre FROM viesco.rel_type_periode WHERE id = ?");
        params.add(idTypePeriode);

        Sql.getInstance().prepared(query.toString(), params,
                new DeliveryOptions().setSendTimeout(Viescolaire.TIME_OUT_HANDLER),
                SqlResult.validResultHandler(new Handler<Either<String, JsonArray>>() {
                    @Override
                    public void handle(Either<String, JsonArray> stringJsonArrayEither) {
                        if(stringJsonArrayEither.isRight()) {
                            String result;
                            if(stringJsonArrayEither.right().getValue().size() > 0) {
                                String type = String.valueOf(((JsonObject) stringJsonArrayEither.right().getValue().getJsonObject(0)).getInteger("type"));
                                String ordre = String.valueOf(((JsonObject) stringJsonArrayEither.right().getValue().getJsonObject(0)).getInteger("ordre"));
                                result = I18n.getInstance().translate("viescolaire.periode." + type, getHost(request),
                                        I18n.acceptLanguage(request)) + " " + ordre;
                            } else {
                                result = I18n.getInstance().translate("viescolaire.utils.annee", getHost(request), I18n.acceptLanguage(request));
                            }
                            handler.handle(new Either.Right<String, String>(result));
                        } else {
                            handler.handle(new Either.Left<String, String>(stringJsonArrayEither.left().getValue()));
                        }
                    }
                }));
    }

    @Override
    public void getPeriodes(final String idEtablissement, final String[] idGroupes,
                            final Handler<Either<String, JsonArray>> handler) {
        if (idEtablissement != null && (idGroupes == null || idGroupes.length == 0)) {
            getPeriodesClasses(idEtablissement, null, handler);
        } else {
            getTypeGroupe(idGroupes, stringMapEither -> {

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
                                        handler.handle(new Either.Right<>(result));
                                        handled.set(true);
                                    }
                                } else {
                                    handler.handle(new Either.Left<>(stringEntryEither.left().getValue()));
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
                                            finalHandler.handle(new Either.Right<>(groupePeriodeResult));
                                        }
                                    } else {
                                        groupeHandled.set(true);
                                        finalHandler.handle(new Either.Left<>(stringEntryEither.left().getValue()));
                                    }
                                }
                            }
                        };

                        Iterator<String> groupeIterator = groupes.iterator();

                        while(groupeIterator.hasNext()) {
                            final String idGroupe = groupeIterator.next();
                            processGroupes(idEtablissement,
                                    idGroupe,
                                    stringListEither -> {

                                if(stringListEither.isRight()) {
                                    //if the groupe is not linked to classes
                                    if (stringListEither.right().getValue().isEmpty())  {
                                        groupHandler.handle(new Either.Right<>(new JsonArray()));
                                    } else {
                                        final String[] idClasses = stringListEither.right().getValue().toArray(new String[0]);
                                        getPeriodesGroupe(idEtablissement,
                                                idClasses,
                                                stringJsonArrayEither -> {
                                            if (stringJsonArrayEither.isRight()) {
                                                for (JsonObject o : (List<JsonObject>) stringJsonArrayEither.right().getValue().getList()) {
                                                    JsonObject groupePeriode = o;
                                                    groupePeriode.put(Field.ID_GROUP, idGroupe);
                                                }
                                                groupHandler.handle(new Either.Right<>(stringJsonArrayEither.right().getValue()));
                                            } else {
                                                log.error(String.format("[Viescolaire@%s::getPeriodesGroup in getPeriodes] error sql request : %s",
                                                        this.getClass().getSimpleName(), stringJsonArrayEither.left().getValue()));
                                                groupHandler.handle(new Either.Left<>(stringJsonArrayEither.left().getValue()));
                                            }
                                        });
                                    }
                                } else {
                                    log.error(String.format("[Viescolaire@%s::processGroupes in getPeriodes] %s groupId : %s.",
                                            this.getClass().getSimpleName(), stringListEither.left().getValue(), idGroupe));
                                    groupHandler.handle(new Either.Left<>(stringListEither.left().getValue()));
                                }
                            });
                        }
                    }

                    if (classes != null && !classes.isEmpty()) {
                        getPeriodesClasses(idEtablissement, classes.toArray(new String[0]), jsonArrayEvent -> {
                            if (jsonArrayEvent.isRight()) {
                                classesDone.set(true);
                                finalHandler.handle(jsonArrayEvent.right());
                            } else {
                                log.error(String.format("[Viecolaire@%s::getPeriodesClasses in getPeriodes] error sql resquest : %s",
                                        this.getClass().getSimpleName(), jsonArrayEvent.left()));
                                finalHandler.handle(jsonArrayEvent.left());
                            }

                        });
                    }

                } else if (stringMapEither.isRight() && stringMapEither.right().getValue().isEmpty()) {
                    log.error(String.format("[Viecolaire@%s::getTypeGroupe in getPeriodes] no class or group found ",
                            this.getClass().getSimpleName()));
                    handler.handle(new Either.Left<>("getPeriodes : no class or group found"));
                } else {
                    log.error(String.format("[Viecolaire@%s::getTypeGroupe in getPeriodes] error sql resquest : %s ",
                            this.getClass().getSimpleName(), stringMapEither.left()));
                    handler.handle(new Either.Left<>(stringMapEither.left().getValue()));
                }
            });
        }
    }

    @Override
    public void getTypePeriodes(Handler<Either<String, JsonArray>> handler) {
        JsonArray fields = new JsonArray();
        fields.add("id").add("type").add("ordre");
        Sql.getInstance().select("viesco.rel_type_periode", fields, validResultHandler(handler));
    }

    @Override
    public void updatePeriodes(final String idEtablissement, final String[] idClasses, final JsonObject[] periodes,
                               final Handler<Either<String, JsonArray>> handler) {
        checkGroupeEtab(idClasses, stringStringEither -> {
            if(stringStringEither.isRight() && stringStringEither.right().getValue().equals(idEtablissement)) {
                verifCoherencePeriodes(periodes, jsonObjectBooleanEither -> {
                    if(jsonObjectBooleanEither.isRight()) {
                        getTypeGroupe(idClasses, stringMapEither -> {
                            if(stringMapEither.isRight() && !stringMapEither.right().getValue().keySet().contains(false)) {
                                getTypePeriode(periodes, stringEither -> {
                                    if(stringEither.isRight()) {
                                        final JsonObject[] periodesWithType = stringEither.right().getValue();

                                        JsonArray statement = new JsonArray();
                                        statement.add(deletePeriodeStatement(idClasses));
                                        statement.add(createPeriodeStatement(idEtablissement, idClasses, periodesWithType));
                                        statement.add(updatePeriodesDevoir(idClasses));
                                        Sql.getInstance().transaction(statement, validResultHandler(handler));
                                    } else {
                                        handler.handle(new Either.Left<>(stringEither.left().getValue()));
                                    }
                                });
                            } else if (stringMapEither.right().getValue().keySet().contains(false)) {
                                handler.handle(new Either.Left<>("updatePeriodes : One of the given id isn't " +
                                        "of a class"));
                            } else {
                                handler.handle(new Either.Left<>(stringMapEither.left().getValue()));
                            }
                        });
                    } else {
                        handler.handle(new Either.Left<>(jsonObjectBooleanEither.left().getValue().encode()));
                    }
                });
            } else if (stringStringEither.isRight()
                    && !stringStringEither.right().getValue().equals(idEtablissement)) {
                handler.handle(new Either.Left<>("updatePeriodes : Groups aren't in the given structure."));
            } else {
                handler.handle(new Either.Left<>(stringStringEither.left().getValue()));
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
            values.add(id);
        }

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> stringJsonArrayEither) {
                if (stringJsonArrayEither.isRight()) {
                    if(stringJsonArrayEither.right().getValue().size() == 0) {
                        handler.handle(new Either.Right<String, JsonObject>(new JsonObject().put("exist", false)));
                    } else {
                        handler.handle(new Either.Right<String, JsonObject>(new JsonObject().put("exist", true)));
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
                .append("MIN(timestamp_fn) AS timestamp_fn, MIN(date_fin_saisie) AS date_fin_saisie, ")
                .append("rel_type_periode.ordre, rel_type_periode.type ")
                .append("FROM viesco.periode ")
                .append(" INNER JOIN ")
                .append( Viescolaire.VSCO_SCHEMA + ".rel_type_periode ON id_type = rel_type_periode.id ")
                .append("WHERE ");

        if(idEtablissement != null) {
            query.append("periode.id_etablissement = ? ");
            values.add(idEtablissement);
        }
        if(idEtablissement != null && idClasses != null && idClasses.length > 0) {
            query.append(" AND ");
        }
        if(idClasses != null && idClasses.length > 0) {
            query.append("periode.id_classe IN ").append(Sql.listPrepared(idClasses));

            for (String id_groupe : idClasses) {
                values.add(id_groupe);
            }
        }
        query.append(" GROUP BY periode.id_type, rel_type_periode.ordre, rel_type_periode.type ");

        Sql.getInstance().prepared(query.toString(), values,
                new DeliveryOptions().setSendTimeout(Viescolaire.TIME_OUT_HANDLER),
                validResultHandler(handler));
    }

    @Override
    public void getPeriodesClasses(String idEtablissement, String[] idClasses,
                                   final Handler<Either<String, JsonArray>> handler) {
        getPeriodesClasses(idEtablissement, idClasses, null, handler);
    }

    /**
     * get classes periods
     *
     * @param idEtablissement   structureId
     * @param idClasses         list of class id
     * @param idPeriode         period id
     * @param handler           Handler replying with Either of {@link JsonArray}
     *                          Containing response of
     *                          [
     *                              {
     *                                  "id": Number,
     *                                  "id_etablissement": String,
     *                                  "libelle": String,
     *                                  "timestamp_dt": String,
     *                                  "timestamp_fn": String",
     *                                  "date_fin_saisie": String,
     *                                  "id_classe": String,
     *                                  "id_type": Number,
     *                                  "date_conseil_classe": String,
     *                                  "publication_bulletin": Boolean,
     *                                  "type": Number,
     *                                  "ordre": Number
     *                              }
     *                          ]
     */
    public void getPeriodesClasses(String idEtablissement, String[] idClasses,
                                   Long idPeriode, final Handler<Either<String, JsonArray>> handler) {

        if (idEtablissement == null && (idClasses == null || idClasses.length == 0)) {
            handler.handle(new Either.Left<>("getPeriodesClasses : No parameter given."));
        }

        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT periode.* , type , ordre ")
                .append(" FROM " + Viescolaire.VSCO_SCHEMA + ".periode ")
                .append(" INNER JOIN ")
                .append( Viescolaire.VSCO_SCHEMA + ".rel_type_periode ON id_type = rel_type_periode.id ")
                .append(" WHERE ");

        if (idEtablissement != null) {
            query.append("periode.id_etablissement = ? ");
            values.add(idEtablissement);
        }
        if (idEtablissement != null && idClasses != null && idClasses.length > 0) {
            query.append(" AND ");
        }
        if (idClasses != null && idClasses.length > 0) {
            query.append("id_classe IN ").append(Sql.listPrepared(idClasses));
            for (String id_classe : idClasses) {
                values.add(id_classe);
            }
        }
        if(idPeriode != null && (idEtablissement != null || (idClasses != null && idClasses.length > 0))) {
            query.append(" AND ");
        }
        if(idPeriode != null)
        {
            query.append("periode.id_type = ? ");
            values.add(idPeriode);
        }
        query.append(" ORDER BY id_etablissement, id_classe, timestamp_dt ");

        Sql.getInstance().prepared(query.toString(), values,
                new DeliveryOptions().setSendTimeout(Viescolaire.TIME_OUT_HANDLER),
                validResultHandler(handler));
    }

    @Override
    public void getDatesDtFnAnneeByClasse(String idEtablissement, List<String> idClasses, Handler<Either<String, JsonArray>> handler) {
        JsonArray values = new JsonArray();
        String query = "SELECT id_etablissement, id_classe,MIN( timestamp_dt) as date_debut, MAX(timestamp_fn)as date_fin FROM " +
                "(SELECT id, id_etablissement,timestamp_dt, timestamp_fn , id_classe, id_type FROM "+ Viescolaire.VSCO_SCHEMA+ ".periode "+
                "WHERE id_etablissement = ? AND id_classe IN "+ Sql.listPrepared(idClasses) +") date "+
                "GROUP BY id_classe, id_etablissement";

        values.add(idEtablissement);
        for(String id : idClasses){
            values.add(id);
        }

        Sql.getInstance().prepared(query, values, validResultHandler(handler));

    }

    /**
     * S'assure de la coherence des periodes pour les classes liees au groupe passe en parametre
     * @param idEtablissement   identifiant de l'etablissement du groupe
     * @param idGroupe          identifiant du groupe
     * @param handler           handler portant le resultat
     */
    private void processGroupes(final String idEtablissement, String idGroupe, final Handler<Either<String, List<String>>> handler) {

        groupeService.getClasseGroupe(new String[]{idGroupe}, stringJsonArrayEither -> {
            if (stringJsonArrayEither.isRight()) {
                List<String> idClasses = new ArrayList<>();
                if (stringJsonArrayEither.right().getValue().isEmpty()) {
                    handler.handle(new Either.Right<>(idClasses));
                } else {
                    // find first class group in array
                    JsonObject classGroup = stringJsonArrayEither.right().getValue().stream()
                            .filter(JsonObject.class::isInstance)
                            .map(JsonObject.class::cast)
                            .findFirst()
                            .orElse(null);

                    if (classGroup != null) {
                        // fetching my key JsonArray "id_classes" which is supposed to be a list of string map to list
                        idClasses = classGroup.getJsonArray(Field.ID_CLASSES, new JsonArray()).stream()
                                .filter(String.class::isInstance)
                                .map(String.class::cast)
                                .collect(Collectors.toList());
                    }

                    // On compte le nombre de periode, pour chaque groupe de classe. Si l'une d'elles a
                    // un nombre different de periode, une erreur survient.
                    getPeriodesClasses(idEtablissement, idClasses.toArray(new String[0]), stringJsonArrayEvent -> {
                        if (stringJsonArrayEvent.isRight()) {

                            final Map<String, Integer> idClassPeriodClassNumberMap = stringJsonArrayEvent.right().getValue().stream()
                                    .filter(JsonObject.class::isInstance)
                                    .map(JsonObject.class::cast)
                                    .collect(Collectors.groupingBy(periodClass -> periodClass.getString("id_classe", "")))
                                    .entrySet()
                                    .stream()
                                    .collect(Collectors.toMap(Map.Entry::getKey, stringListEntry -> stringListEntry.getValue().size()));
                            idClassPeriodClassNumberMap.remove("");

                            boolean hasDifferentValues = idClassPeriodClassNumberMap.values().stream().distinct().count() > 1;
                            if (hasDifferentValues) {
                                log.error(String
                                        .format("[Viescolaire@%s::getPeriodesClasses] error : getPeriodesGroupe : Given classes have different type of periods. Classes id: %s",
                                                this.getClass().getSimpleName(), new JsonArray(new ArrayList<>(idClassPeriodClassNumberMap.keySet()))));
                                handler.handle(new Either.Left<>("getPeriodesGroupe : Given classes have different type of periods"));
                            } else {
                                handler.handle(new Either.Right<>(new ArrayList<>(idClassPeriodClassNumberMap.keySet())));
                            }

                        } else {
                            log.error(String
                                    .format("[Viescolaire@%s::getPeriodesClasses in processGroups] error sql request : %s.",
                                                    this.getClass().getSimpleName(), stringJsonArrayEvent.left().getValue()));
                            handler.handle(new Either.Left<>(stringJsonArrayEvent.left().getValue()));
                        }
                    });
                }

            } else {
                log.error(String
                        .format("[Viescolaire@%s::getClasseGroupe in processGroups] error sql request : %s idGroup : %s.",
                                this.getClass().getSimpleName(), stringJsonArrayEither.left().getValue(),
                        idGroupe));
                handler.handle(new Either.Left<>(stringJsonArrayEither.left().getValue()));
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
            handler.handle(new Either.Left<>("getTypePeriode : An error occured while sorting periods"));
        }

        //////////////// Setting id type by period ////////////////
        final JsonArray values = new JsonArray();
        values.add(periodes.length);

        String query = "SELECT * FROM viesco.rel_type_periode WHERE type = ? ORDER BY ordre";
        Sql.getInstance().prepared(query, values, validResultHandler(stringJsonArrayEither -> {
            if (stringJsonArrayEither.isRight()) {
                JsonArray result = stringJsonArrayEither.right().getValue();

                // Si aucun type ne correspond au nombre de période à définir, la table de type est incomplète
                if (result.size() != periodes.length) {
                    handler.handle(new Either.Left<>("updatePeriodesType :  No periode type defined for the given number of periode"));
                }

                for (int i = 0; i < result.size(); i++) {
                    periodes[i].put("id_type", (result.getJsonObject(i)).getLong("id"));
                }

                handler.handle(new Either.Right<>(periodes));
            } else {
                handler.handle(new Either.Left<>(stringJsonArrayEither.left().getValue()));
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
        Calendar timestamp_dt = Calendar.getInstance();
        Calendar timestamp_fn = Calendar.getInstance();
        Calendar date_fin_saisie = Calendar.getInstance();
        Calendar timestamp_dtBis = Calendar.getInstance();
        Calendar timestamp_fnBis = Calendar.getInstance();
        Calendar timestamp_previous = Calendar.getInstance();
        Calendar timestamp_next = Calendar.getInstance();
        Calendar date_conseil_classe = Calendar.getInstance();
        JsonObject periode;
        JsonObject periodeBis;

        for (int i = 0; i < periodes.length; i++) {
            periode = periodes[i];
            try {
                timestamp_dt.setTime(dateFormat.parse(periode.getString("timestamp_dt")));
                timestamp_fn.setTime(dateFormat.parse(periode.getString("timestamp_fn")));
                date_fin_saisie.setTime(dateFormat.parse(periode.getString("date_fin_saisie")));
                date_conseil_classe.setTime(dateFormat.parse(periode.getString("date_conseil_classe")));

                // Erreur date_debut posterieur date_fin
                if(timestamp_dt.after(timestamp_fn)
                        ||(timestamp_dt.get(Calendar.YEAR) == timestamp_fn.get(Calendar.YEAR)
                        && timestamp_dt.get(Calendar.DAY_OF_YEAR) == timestamp_fn.get(Calendar.DAY_OF_YEAR))) {
                    errorList.get(i).put("errorFn", "La date de fin ne peut etre anterieur a la date de debut.");
                }

                // Erreur date_debut posterieur date_fin_saisie
                if (timestamp_dt.after(date_fin_saisie)
                        || (timestamp_dt.get(Calendar.YEAR) == date_fin_saisie.get(Calendar.YEAR)
                        && timestamp_dt.get(Calendar.DAY_OF_YEAR) == date_fin_saisie.get(Calendar.DAY_OF_YEAR))) {
                    errorList.get(i).put("errorFnS", "La date de fin de saisie ne peut etre anterieur a la date de debut.");
                }

                // Erreur chevauchement des periodes
                for (int j = 0; j < periodes.length; j++) {
                    if(i == j) {
                        // On compare la période en cours avec les autres périodes
                        continue;
                    }
                    periodeBis = periodes[j];
                    timestamp_dtBis.setTime(dateFormat.parse(periodeBis.getString("timestamp_dt")));
                    timestamp_fnBis.setTime(dateFormat.parse(periodeBis.getString("timestamp_fn")));

                    if(!(timestamp_dt.before(timestamp_dtBis) && timestamp_fn.before(timestamp_dtBis))
                            && !(timestamp_dt.after(timestamp_fnBis) && timestamp_fn.after(timestamp_fnBis))) {
                        errorList.get(i).put("errorOver", "La periode chevauche une autre periode.");
                    }
                }

                // Erreur periodes non contigues && periodes non ordonnees
                if (i - 1 > 0) {
                    timestamp_previous.setTime(dateFormat.parse(periodes[i - 1].getString("timestamp_fn")));

                    if(timestamp_dt.get(Calendar.DAY_OF_YEAR) - timestamp_previous.get(Calendar.DAY_OF_YEAR) > 1) {
                        errorList.get(i).put("errorContigPrev", "La periode n'est pas contigue a la periode precedente.");
                    }
                }
                if (i + 1 < periodes.length) {
                    timestamp_next.setTime(dateFormat.parse(periodes[i + 1].getString("timestamp_dt")));

                    if (timestamp_next.get(Calendar.DAY_OF_YEAR) - timestamp_fn.get(Calendar.DAY_OF_YEAR) > 1) {
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
                    result.put("periode " + String.valueOf(i), new JsonObject(errorList.get(i)));
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
                        mapEtab.put(etab.getString("idStructure"), etab.getJsonArray("idClasses").getList());
                    }

                    if (mapEtab.keySet().size() > 1) {
                        handler.handle(new Either.Left<>("checkGroupeEtab : Groups aren't in the same structure."));
                    } else {
                        List<String> error = new ArrayList<>();
                        String idEtablissement = mapEtab.keySet().iterator().next();
                        for (String idGroupe : idGroupes) {
                            if (!mapEtab.get(idEtablissement).contains(idGroupe)) {
                                error.add(idGroupe);
                            }
                        }
                        if (!error.isEmpty()) {
                            handler.handle(new Either.Left<>("checkGroupeEtab : Groups not recognised " + error));
                        } else {
                            handler.handle(new Either.Right<>(idEtablissement));
                        }
                    }
                } else {
                    handler.handle(new Either.Left<>(stringJsonArrayEither.left().getValue()));
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

        for (int i = 0; i < periodes.length; i++) {
            JsonObject periode = periodes[i];
            for(String idClasse : idClasses) {
                query.append("INSERT INTO viesco.periode (id_etablissement, id_classe, id_type, timestamp_dt,")
                        .append(" timestamp_fn, date_fin_saisie, date_conseil_classe, publication_bulletin)")
                        .append(" VALUES (?, ?, ?, to_timestamp(?, 'YYYY-MM-DD'), to_timestamp(?, 'YYYY-MM-DD'),")
                        .append(" to_timestamp(?, 'YYYY-MM-DD'), to_timestamp(? ,'YYYY-MM-DD'), ?)")
                        .append(" ON CONFLICT (id_etablissement, id_classe, id_type) DO UPDATE SET")
                        .append(" timestamp_dt = to_timestamp(?, 'YYYY-MM-DD'), timestamp_fn = to_timestamp(?, 'YYYY-MM-DD'),")
                        .append(" date_fin_saisie = to_timestamp(?, 'YYYY-MM-DD'), date_conseil_classe = to_timestamp(?, 'YYYY-MM-DD'),")
                        .append(" publication_bulletin = ?;");

                values.add(idEtablissement).add(idClasse).add(periode.getInteger("id_type"))
                        .add(periode.getString("timestamp_dt")).add(periode.getString("timestamp_fn"))
                        .add(periode.getString("date_fin_saisie")).add(periode.getString("date_conseil_classe"))
                        .add(periode.getBoolean("publication_bulletin"))
                        .add(periode.getString("timestamp_dt")).add(periode.getString("timestamp_fn"))
                        .add(periode.getString("date_fin_saisie")).add(periode.getString("date_conseil_classe"))
                        .add(periode.getBoolean("publication_bulletin"));
            }
        }

        return new JsonObject()
                .put("statement", query.toString())
                .put("values", values)
                .put("action", "prepared");
    }

    /**
     * Supprime les periodes passees en parametre
     *
     * @param idClasses    identifiants de classe des periodes a supprimer
     */
    private JsonObject deletePeriodeStatement(String[] idClasses) {
        StringBuilder query = new StringBuilder()
                .append("DELETE FROM viesco.periode WHERE id_classe IN " + Sql.listPrepared(idClasses));

        JsonArray values = new JsonArray();
        for(String id : idClasses) {
            values.add(id);
        }

        return new JsonObject()
                .put("statement", query.toString())
                .put("values", values)
                .put("action", "prepared");
    }
    /**
     * Supprime les periodes passees en parametre
     *
     * @param idClasses    identifiants de classe des periodes a supprimer
     */
    private JsonObject updatePeriodesDevoir(String[] idClasses) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("UPDATE notes.devoirs " )
                .append(" SET id_periode = subquery.id_type" )
                .append(" FROM (SELECT * FROM viesco.periode WHERE periode.id_classe IN " + Sql.listPrepared(idClasses))
                .append(" ) AS subquery ")
                .append(" WHERE devoirs.date BETWEEN subquery.timestamp_dt AND subquery.timestamp_fn ")
                .append(" AND  devoirs.id IN (SELECT devoirs.id ")
                .append("                FROM notes.devoirs ")
                .append("                LEFT JOIN notes.rel_devoirs_groupes ")
                .append("                ON rel_devoirs_groupes.id_devoir = devoirs.id ")
                .append("                WHERE rel_devoirs_groupes.id_groupe IN  " + Sql.listPrepared(idClasses) + ")");

        for(String id : idClasses) {
            values.add(id);
        }
        for(String id : idClasses) {
            values.add(id);
        }
        return new JsonObject()
                .put("statement", query.toString())
                .put("values", values)
                .put("action", "prepared");
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

    @Override
    public void updatePublicationBulletin(Integer idPeriode, Boolean publiBulletin, Handler<Either<String, JsonObject>> handler) {
        String query ="UPDATE viesco.periode SET publication_bulletin = ? WHERE id = ?";
        JsonArray values = new JsonArray().add(publiBulletin).add(idPeriode);

        Sql.getInstance().prepared(query, values, validRowsResultHandler(handler));
    }
}
