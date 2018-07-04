package fr.openent.viescolaire.controller;

import fr.openent.viescolaire.service.*;
import fr.openent.viescolaire.service.impl.*;
import fr.wseduc.bus.BusAddress;
import fr.wseduc.webutils.Either;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.http.request.JsonHttpServerRequest;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.entcore.common.user.UserInfos;

import java.util.List;

public class EventBusController extends ControllerHelper {

    private GroupeService groupeService;
    private ClasseService classeService;
    private UserService userService;
    private EleveService eleveService;
    private MatiereService matiereService;
    private PeriodeService periodeService;
    private EventService eventService;

    public EventBusController() {
        groupeService = new DefaultGroupeService();
        classeService = new DefaultClasseService();
        userService = new DefaultUserService(eb);
        eleveService = new DefaultEleveService();
        matiereService = new DefaultMatiereService();
        periodeService = new DefaultPeriodeService();
        eventService = new DefaultEventService();
    }

    @BusAddress("viescolaire")
    public void getData(final Message<JsonObject> message) {
        final String action = message.body().getString("action");

        if (action == null) {
            log.warn("[@BusAddress](viescolaire) Invalid action.");
            message.reply(new JsonObject().put("status", "error")
                    .put("message", "Invalid action."));
            return;
        }

        String service = action.split("\\.")[0];
        String method = action.split("\\.")[1];

        switch (service) {
            case "groupe": {
                groupeBusService(method, message);
            }
            break;
            case "classe": {
                classeBusService(method, message);
            }
            break;
            case "user": {
                userBusService(method, message);
            }
            break;
            case "eleve": {
                eleveBusService(method, message);
            }
            break;
            case "matiere": {
                matiereBusService(method, message);
            }
            break;
            case "periode": {
                periodeBusService(method, message);
            }
            break;
            case "structure": {
                structureBusService(method, message);
            }
            break;
            case "event": {
                eventBusService(method, message);
            }
        }
    }

    private void eventBusService(String method, Message<JsonObject> message) {
        switch (method) {
            case "add": {
                JsonObject formattedUser = message.body().getJsonObject("user");
                Number idRessource = message.body().getLong("idRessource");
                JsonObject ressource = message.body().getJsonObject("ressource");
                String event = message.body().getString("event");

                eventService.add(formattedUser, idRessource, ressource, event);
            }
            break;
        }
    }

    private void groupeBusService(String method, Message<JsonObject> message) {
        switch (method) {
            case "listUsersByGroupeEnseignementId": {
                String groupeEnseignementId = message.body().getString("groupEnseignementId");
                String profile = message.body().getString("profile");
                groupeService.listUsersByGroupeEnseignementId(groupeEnseignementId, profile, getJsonArrayBusResultHandler(message));
            }
            break;
            case "listGroupesEnseignementsByUserId": {
                String userId = message.body().getString("userId");
                groupeService.listGroupesEnseignementsByUserId(userId, getJsonArrayBusResultHandler(message));
            }
            break;
            default: {
                message.reply(getErrorReply("Method not found"));
            }
        }
    }

    private void classeBusService(String method, Message<JsonObject> message) {
        switch (method) {
            case "getNbElevesGroupe": {
                JsonArray idGroupes = message.body().getJsonArray("idGroupes");
                classeService.getNbElevesGroupe(idGroupes, getJsonArrayBusResultHandler(message));
            }
            break;
            case "getEtabClasses": {
                String[] idClasses = convertJsonArrayToStringArray(message.body().getJsonArray("idClasses"));
                classeService.getEtabClasses(idClasses, getJsonArrayBusResultHandler(message));
            }
            break;
            case "getClasseEtablissement": {
                String idEtablissement = message.body().getString("idEtablissement");
                classeService.getClasseEtablissement(idEtablissement, getJsonArrayBusResultHandler(message));
            }
            break;
            case "getElevesClasses": {
                String[] idClasses = convertJsonArrayToStringArray(message.body().getJsonArray("idClasses"));
                Long idPeriode = message.body().getLong("idPeriode");
                classeService.getElevesClasses(idClasses, idPeriode, getJsonArrayBusResultHandler(message));
            }
            break;
            case "getEleveClasse": {
                String idClasse = message.body().getString("idClasse");
                Long idPeriode = message.body().getLong("idPeriode");
                classeService.getEleveClasse(idClasse, idPeriode, getJsonArrayBusResultHandler(message));
            }
            break;
            case "getClasseInfo": {
                String idClasse = message.body().getString("idClasse");
                classeService.getClasseInfo(idClasse, getJsonObjectBusResultHandler(message));
            }
            break;
            case "getClasseIdByEleve": {
                String idEleve = message.body().getString("idEleve");
                classeService.getClasseIdByEleve(idEleve, getJsonObjectBusResultHandler(message));
            }
            break;
            case "listClasses": {
                String idEtablissement = message.body().getString("idStructure");
                classeService.listClasses(idEtablissement, true,null, getJsonArrayBusResultHandler(message));
            }
            break;
            default: {
                message.reply(getErrorReply("Method not found"));
            }
        }
    }

    private void userBusService(String method, Message<JsonObject> message) {
        switch (method) {
            case "getMoyenne": {
                String idEleve = message.body().getString("idEleve");
                Long[] idDevoirs = convertJsonArrayToLongArray(message.body().getJsonArray("idDevoirs"));
                userService.getMoyenne(idEleve, idDevoirs, getJsonObjectBusResultHandler(message));
            }
            break;
            case "getUAI": {
                String idEtabl = message.body().getString("idEtabl");
                userService.getUAI(idEtabl, getJsonObjectBusResultHandler(message));
            }
            break;
            case "getResponsablesEtabl": {
                List<String> idsResponsable = message.body().getJsonArray("idsResponsable").getList();
                userService.getResponsablesEtabl(idsResponsable, getJsonArrayBusResultHandler(message));
            }
            break;
            case "getElevesRelatives": {
                List<String> idsClass = message.body().getJsonArray("idsClass").getList();
                userService.getElevesRelatives(idsClass, getJsonArrayBusResultHandler(message));
            }
            break;
            case "getCodeDomaine": {
                String idClass = message.body().getString("idClass");
                userService.getCodeDomaine(idClass, getJsonArrayBusResultHandler(message));
            }
            break;
            case "getResponsablesDirection": {
                String idStructure = message.body().getString("idStructure");
                userService.getResponsablesDirection(idStructure, getJsonArrayBusResultHandler(message));
            }
            break;
            default: {
                message.reply(getErrorReply("Method not found"));
            }
        }
    }

    private void eleveBusService(String method, Message<JsonObject> message) {
        switch (method) {
            case "getUsers": {
                JsonArray idUsers = message.body().getJsonArray("idUsers");
                eleveService.getUsers(idUsers, getJsonArrayBusResultHandler(message));
            }
            break;
            case "getInfoEleve": {
                String[] idEleves = convertJsonArrayToStringArray(message.body().getJsonArray("idEleves"));
                eleveService.getInfoEleve(idEleves, getJsonArrayBusResultHandler(message));
            }
            break;
            case "getCycle": {
                String idClasse = message.body().getString("idClasse");
                eleveService.getCycle(idClasse, getJsonArrayBusResultHandler(message));
            }
            break;
            default: {
                message.reply(getErrorReply("Method not found"));
            }
        }
    }

    private void structureBusService(String method, Message<JsonObject> message) {
        switch (method) {
            case "getStructuresActives": {
                String module = message.body().getString("module");
                userService.getActivesIDsStructures(module,getJsonArrayBusResultHandler(message));
            }
            break;
            default: {
                message.reply(getErrorReply("Method not found"));
            }
        }
    }

    private void matiereBusService(String method, final Message<JsonObject> message) {
        switch (method) {
            case "getMatieres": {
                JsonArray idMatieres = message.body().getJsonArray("idMatieres");
                matiereService.getMatieres(idMatieres, getJsonArrayBusResultHandler(message));
            }
            break;
            case "getMatiere": {
                String idMatiere = message.body().getString("idMatiere");
                matiereService.getMatiere(idMatiere, getJsonObjectBusResultHandler(message));
            }
            break;
            case "getMatieresForUser": {
                final String userType = message.body().getString("userType");
                final String idEnseignant = message.body().getString("idUser");
                final String idStructure = message.body().getString("idStructure");
                final Boolean onlyId = message.body().getBoolean("onlyId");
                if ("Personnel".equals(userType)) {
                    matiereService.listMatieresEtab(idStructure, onlyId,getJsonArrayBusResultHandler(message));
                } else {
                    new DefaultUtilsService()
                            .getTitulaires(idEnseignant, idStructure, new Handler<Either<String, JsonArray>>() {
                                        @Override
                                        public void handle(Either<String, JsonArray> event) {
                                            if (event.isRight()) {
                                                JsonArray oTitulairesIdList = event.right().getValue();
                                                new MatiereController().listMatieres(idStructure,
                                                        oTitulairesIdList,null, message, idEnseignant, onlyId);
                                            } else {
                                                message.reply(getErrorReply(event.left().getValue()));
                                            }
                                        }
                                    }
                            );
                }
            }
            break;
            default: {
                message.reply(getErrorReply("Method not found"));
            }
        }
    }

    private void periodeBusService(String method, final Message<JsonObject> message) {
        switch (method) {
            case "getLibellePeriode": {
                if (message.body().containsKey("ordre")) {
                    Integer type = message.body().getInteger("type");
                    Integer ordre = message.body().getInteger("ordre");
                    JsonObject request = message.body().getJsonObject("request");

                    String libelle = periodeService.getLibellePeriode(type, ordre, new JsonHttpServerRequest(request));
                    message.reply(new JsonObject()
                            .put("status", "ok")
                            .put("result", libelle));
                } else {
                    Integer idType = message.body().getInteger("idType") == null
                            ? 0
                            : message.body().getInteger("idType");
                    JsonObject request = message.body().getJsonObject("request");
                    periodeService.getLibellePeriode(Long.parseLong(idType.toString()),
                            new JsonHttpServerRequest(request), new Handler<Either<String, String>>() {
                                @Override
                                public void handle(Either<String, String> res) {
                                    if (res.isRight()) {
                                        message.reply(new JsonObject()
                                                .put("status", "ok")
                                                .put("result", res.right().getValue()));
                                    } else {
                                        message.reply(getErrorReply(res.left().getValue()));
                                    }
                                }
                            });
                }
            }
            break;
            case "getPeriodes": {
                List<String> l = message.body().getJsonArray("idGroupes").getList();
                String [] idGroupes = l.toArray(new String[l.size()]);
                String idEtablissement = message.body().getString("idEtablissement");

                JsonObject request = message.body().getJsonObject("request");
                periodeService.getPeriodes(idEtablissement, idGroupes,
                        new Handler<Either<String, JsonArray>>() {
                            @Override
                            public void handle(Either<String, JsonArray> res) {
                                if (res.isRight()) {
                                    message.reply(new JsonObject()
                                            .put("status", "ok")
                                            .put("result", res.right().getValue()));
                                } else {
                                    message.reply(getErrorReply(res.left().getValue()));
                                }
                            }
                        });
            }
            break;
            case "getDatesDtFnAnneeByClasse": {
                List<String> idClasses = message.body().getJsonArray("idClasses").getList();
                String idEtablissement = message.body().getString("idEtablissement");
                periodeService.getDatesDtFnAnneeByClasse(idEtablissement, idClasses,getJsonArrayBusResultHandler(message));
            }
            break;
            default: {
                message.reply(getErrorReply("Method not found"));
            }
        }
    }

    private Handler<Either<String, JsonArray>> getJsonArrayBusResultHandler(final Message<JsonObject> message) {
        return new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> result) {
                if (result.isRight()) {
                    message.reply(new JsonObject()
                            .put("status", "ok")
                            .put("results", result.right().getValue()));
                } else {
                    message.reply(getErrorReply(result.left().getValue()));
                }
            }
        };
    }

    private Handler<Either<String, JsonObject>> getJsonObjectBusResultHandler(final Message<JsonObject> message) {
        return new Handler<Either<String, JsonObject>>() {
            @Override
            public void handle(Either<String, JsonObject> result) {
                if (result.isRight()) {
                    message.reply(new JsonObject()
                            .put("status", "ok")
                            .put("result", result.right().getValue()));
                } else {
                    message.reply(getErrorReply(result.left().getValue()));
                }
            }
        };
    }

    private JsonObject getErrorReply(String message) {
        return new JsonObject()
                .put("status", "error")
                .put("message", message);
    }

    private String[] convertJsonArrayToStringArray(JsonArray list) {
        String[] objects = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            objects[i] = list.getString(i);
        }

        return objects;
    }

    private Long[] convertJsonArrayToLongArray(JsonArray list) {
        Long[] objects = new Long[list.size()];
        for (int i = 0; i < list.size(); i++) {
            objects[i] = list.getLong(i);
        }

        return objects;
    };
}
