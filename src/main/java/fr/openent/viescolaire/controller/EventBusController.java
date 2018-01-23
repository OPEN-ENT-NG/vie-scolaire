package fr.openent.viescolaire.controller;

import fr.openent.viescolaire.service.*;
import fr.openent.viescolaire.service.impl.*;
import fr.wseduc.bus.BusAddress;
import fr.wseduc.webutils.Either;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.http.request.JsonHttpServerRequest;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.List;

public class EventBusController extends ControllerHelper {

    private GroupeService groupeService;
    private ClasseService classeService;
    private UserService userService;
    private EleveService eleveService;
    private MatiereService matiereService;
    private PeriodeService periodeService;

    public EventBusController() {
        groupeService = new DefaultGroupeService();
        classeService = new DefaultClasseService();
        userService = new DefaultUserService(eb);
        eleveService = new DefaultEleveService();
        matiereService = new DefaultMatiereService();
        periodeService = new DefaultPeriodeService();
    }

    @BusAddress("viescolaire")
    public void getData(final Message<JsonObject> message) {
        final String action = message.body().getString("action");

        if (action == null) {
            log.warn("[@BusAddress](viescolaire) Invalid action.");
            message.reply(new JsonObject().putString("status", "error")
                    .putString("message", "Invalid action."));
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
        }
    }

    private void groupeBusService(String method, Message<JsonObject> message) {
        switch (method) {
            case "listUsersByGroupeEnseignementId": {
                String groupeEnseignementId = message.body().getString("groupEnseignementId");
                String profile = message.body().getString("profile");
                groupeService.listUsersByGroupeEnseignementId(groupeEnseignementId, profile, getArrayBusResultHandler(message));
            }
            break;
            case "listGroupesEnseignementsByUserId": {
                String userId = message.body().getString("userId");
                groupeService.listGroupesEnseignementsByUserId(userId, getArrayBusResultHandler(message));
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
                JsonArray idGroupes = message.body().getArray("idGroupes");
                classeService.getNbElevesGroupe(idGroupes, getArrayBusResultHandler(message));
            }
            break;
            case "getEtabClasses": {
                String[] idClasses = convertJsonArrayToStringArray(message.body().getArray("idClasses"));
                classeService.getEtabClasses(idClasses, getArrayBusResultHandler(message));
            }
            break;
            case "getClasseEtablissement": {
                String idEtablissement = message.body().getString("idEtablissement");
                classeService.getClasseEtablissement(idEtablissement, getArrayBusResultHandler(message));
            }
            break;
            case "getElevesClasses": {
                String[] idClasses = convertJsonArrayToStringArray(message.body().getArray("idClasses"));
                classeService.getElevesClasses(idClasses, getArrayBusResultHandler(message));
            }
            break;
            case "getEleveClasse": {
                String idClasse = message.body().getString("idClasse");
                classeService.getEleveClasse(idClasse, getArrayBusResultHandler(message));
            }
            break;
            case "getClasseInfo": {
                String idClasse = message.body().getString("idClasse");
                classeService.getClasseInfo(idClasse, getObjectBusResultHandler(message));
            }
            break;
            case "getClasseByEleve": {
                String idEleve = message.body().getString("idEleve");
                classeService.getClasseByEleve(idEleve, getObjectBusResultHandler(message));
            }
            break;
            case "getElevesGroupesClasses": {
                String[] idClasses = convertJsonArrayToStringArray(message.body().getArray("idClasses"));
                classeService.getElevesGroupesClasses(idClasses, getArrayBusResultHandler(message));
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
                Long[] idDevoirs = convertJsonArrayToLongArray(message.body().getArray("idDevoirs"));
                userService.getMoyenne(idEleve, idDevoirs, getObjectBusResultHandler(message));
            }
            break;
            case "getUAI": {
                String idEtabl = message.body().getString("idEtabl");
                userService.getUAI(idEtabl, getObjectBusResultHandler(message));
            }
            break;
            case "getResponsablesEtabl": {
                List<String> idsResponsable = message.body().getArray("idsResponsable").toList();
                userService.getResponsablesEtabl(idsResponsable, getArrayBusResultHandler(message));
            }
            break;
            case "getElevesRelatives": {
                List<String> idsClass = message.body().getArray("idsClass").toList();
                userService.getElevesRelatives(idsClass, getArrayBusResultHandler(message));
            }
            break;
            case "getCodeDomaine": {
                String idClass = message.body().getString("idClass");
                userService.getCodeDomaine(idClass, getArrayBusResultHandler(message));
            }
            break;
            case "getResponsablesDirection": {
                String idStructure = message.body().getString("idStructure");
                userService.getResponsablesDirection(idStructure, getArrayBusResultHandler(message));
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
                JsonArray idUsers = message.body().getArray("idUsers");
                eleveService.getUsers(idUsers, getArrayBusResultHandler(message));
            }
            break;
            case "getInfoEleve": {
                String[] idEleves = convertJsonArrayToStringArray(message.body().getArray("idEleves"));
                eleveService.getInfoEleve(idEleves, getArrayBusResultHandler(message));
            }
            break;
            default: {
                message.reply(getErrorReply("Method not found"));
            }
        }
    }

    private void matiereBusService(String method, Message<JsonObject> message) {
        switch (method) {
            case "getMatieres": {
                JsonArray idMatieres = message.body().getArray("idMatieres");
                matiereService.getMatieres(idMatieres, getArrayBusResultHandler(message));
            }
            break;
            case "getMatiere": {
                String idMatiere = message.body().getString("idMatiere");
                matiereService.getMatiere(idMatiere, getObjectBusResultHandler(message));
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
                if (message.body().containsField("ordre")) {
                    Integer type = message.body().getInteger("type");
                    Integer ordre = message.body().getInteger("ordre");
                    JsonObject request = message.body().getObject("request");

                    String libelle = periodeService.getLibellePeriode(type, ordre, new JsonHttpServerRequest(request));
                    message.reply(new JsonObject()
                            .putString("status", "ok")
                            .putString("result", libelle));
                } else {
                    Integer idType = message.body().getInteger("idType") == null
                            ? 0
                            : message.body().getInteger("idType");
                    JsonObject request = message.body().getObject("request");
                    periodeService.getLibellePeriode(Long.parseLong(idType.toString()),
                            new JsonHttpServerRequest(request), new Handler<Either<String, String>>() {
                                @Override
                                public void handle(Either<String, String> res) {
                                    if (res.isRight()) {
                                        message.reply(new JsonObject()
                                                .putString("status", "ok")
                                                .putString("result", res.right().getValue()));
                                    } else {
                                        message.reply(getErrorReply(res.left().getValue()));
                                    }
                                }
                            });
                }
            }
            break;
            default: {
                message.reply(getErrorReply("Method not found"));
            }
        }
    }

    private Handler<Either<String, JsonArray>> getArrayBusResultHandler(final Message<JsonObject> message) {
        return new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> result) {
                if (result.isRight()) {
                    message.reply(new JsonObject()
                            .putString("status", "ok")
                            .putArray("results", result.right().getValue()));
                } else {
                    message.reply(getErrorReply(result.left().getValue()));
                }
            }
        };
    }

    private Handler<Either<String, JsonObject>> getObjectBusResultHandler(final Message<JsonObject> message) {
        return new Handler<Either<String, JsonObject>>() {
            @Override
            public void handle(Either<String, JsonObject> result) {
                if (result.isRight()) {
                    message.reply(new JsonObject()
                            .putString("status", "ok")
                            .putObject("result", result.right().getValue()));
                } else {
                    message.reply(getErrorReply(result.left().getValue()));
                }
            }
        };
    }

    private JsonObject getErrorReply(String message) {
        return new JsonObject()
                .putString("status", "error")
                .putString("message", message);
    }

    private String[] convertJsonArrayToStringArray(JsonArray list) {
        String[] objects = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            objects[i] = list.get(i);
        }

        return objects;
    }

    private Long[] convertJsonArrayToLongArray(JsonArray list) {
        Long[] objects = new Long[list.size()];
        for (int i = 0; i < list.size(); i++) {
            objects[i] = list.get(i);
        }

        return objects;
    };
}
