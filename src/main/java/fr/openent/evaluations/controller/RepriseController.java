package fr.openent.evaluations.controller;

import fr.openent.Viescolaire;
import fr.openent.evaluations.service.RepriseService;
import fr.openent.evaluations.service.impl.RepriseServiceImpl;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.http.filter.ResourceFilter;
import org.entcore.common.http.filter.SuperAdminFilter;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.Objects;

public class RepriseController extends ControllerHelper {

    private final RepriseService repriseService;
    private JsonArray report;

    public RepriseController () {
        pathPrefix = Viescolaire.EVAL_PATHPREFIX;
        repriseService = new RepriseServiceImpl();
    }

    @Get("/mn-490/reprise")
    @ApiDoc("Reprise de données pour la demande MN-490")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(SuperAdminFilter.class)
    public void reprise (final HttpServerRequest request) {
        report = new JsonArray();
        final Integer[] duplicationsNumber = new Integer[1];
        repriseService.getDuplicationsList(new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> result) {
                final JsonArray duplications;
                if (result.isRight()) {
                    duplications = result.right().getValue();
                    duplicationsNumber[0] = duplications.size();
                    for (int i = 0; i < duplications.size(); i++) {
                        JsonObject duplication = duplications.get(i);
                        repriseService.getDuplication(
                                duplication.getInteger("id_devoir"),
                                duplication.getInteger("id_competence"),
                                duplication.getString("id_eleve"),
                                getDuplicationHandler(duplication, duplicationsNumber, request)
                        );
                    }
                } else {
                    request.response().end("An error occurred when collecting duplications");
                }
            }
        });
    }

    private Handler<Either<String, JsonArray>> getDuplicationHandler
            (final JsonObject duplication, final Integer[] duplicationsNumber, final HttpServerRequest request) {
        return new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> result) {
                if (result.isRight()) {
                    JsonObject correctSkill = findRightSkill(result.right().getValue());
                    duplication.putNumber("evaluation", correctSkill.getNumber("evaluation"));
                    repriseService.deleteDuplication(
                            duplication.getInteger("id_devoir"),
                            duplication.getInteger("id_competence"),
                            duplication.getString("id_eleve"),
                            correctSkill.getInteger("id"),
                            getDuplicationDeletionHandler(duplication, duplicationsNumber, request)
                    );
                } else {
                    log.error("An error occurred when collecting duplication for id_devoir = " + duplication.getInteger("id_devoir")
                            + ", id_competence = " + duplication.getInteger("id_competence") + ", id_eleve = " + duplication.getString("id_eleve"));
                    JsonObject o = generateReportObject(duplication, "", "", "");
                    o.putString("comment","Une erreur s'est produite lors de la récupération des duplications pour id_devoir = " + duplication.getInteger("id_devoir") +
                            ", id_competence = " + duplication.getInteger("id_competence") + ", id_eleve = " + duplication.getString("id_eleve"));
                    report.add(o);
                }
            }
        };
    }

    private Handler<Boolean> getDuplicationDeletionHandler
            (final JsonObject duplication, final Integer[] duplicationsNumber, final HttpServerRequest request) {
        return new Handler<Boolean>() {
            @Override
            public void handle(Boolean deletionResult) {
                if (deletionResult) {
                    repriseService.getStudentInformation(duplication.getString("id_eleve"),
                            getStudentInformationHandler(duplication, duplicationsNumber, request));
                } else {
                    log.error("An error occurred when deleting duplication for id_devoir = " + duplication.getInteger("id_devoir")
                            + ", id_competence = " + duplication.getInteger("id_competence") + ", id_eleve = " + duplication.getString("id_eleve"));
                    JsonObject o = generateReportObject(duplication, "", "", "");
                    o.putString("comment","Une erreur s'est produite lors de la suppression des duplications pour id_devoir = " + duplication.getInteger("id_devoir") +
                            ", id_competence = " + duplication.getInteger("id_competence") + ", id_eleve = " + duplication.getString("id_eleve"));
                    report.add(o);
                }
            }
        };
    }

    private Handler<JsonObject> getStudentInformationHandler (final JsonObject duplication,
                      final Integer[] duplicationsNumber, final HttpServerRequest request) {
        return new Handler<JsonObject>() {
            @Override
            public void handle(final JsonObject studentInformation) {
                if (studentInformation.containsField("id")) {
                    log.error("Student information not found for student = " + studentInformation.getString("id"));
                    JsonObject o = generateReportObject(duplication, "", studentInformation.getString("id"),"");
                    o.putString("comment", "Les informations de l'élève " + studentInformation.getString("id") + " n'ont pas étés" +
                            "trouvées dans l'annuaire.");
                    report.addObject(o);
                    if (duplicationsNumber[0] == report.size()) {
                        sendResponse(request, report);
                    }
                } else {
                    repriseService.getGroupName(
                            duplication.getString("id_groupe"),
                            duplication.getInteger("type_groupe"),
                            new Handler<String>() {
                                @Override
                                public void handle(String groupName) {
                                    report.addObject(generateReportObject(
                                            duplication,
                                            studentInformation.getString("structureName"),
                                            studentInformation.getString("displayName"),
                                            groupName
                                    ));
                                    if (duplicationsNumber[0] == report.size()) {
                                        sendResponse(request, report);
                                    }
                                }
                            }
                    );
                }
            }
        };
    }

    private void sendResponse (HttpServerRequest request, JsonArray report) {
        request.response()
                .putHeader("Content-Type", "text/csv; charset=utf-8")
                .putHeader("Content-Disposition", "attachment; filename=mn-490-report.csv")
                .end(generateReport(report));
    }

    private JsonObject generateReportObject (JsonObject duplication,
            String structureName, String studentName, String className) {
        return new JsonObject().putString("structureName", structureName)
                .putString("teacherName", duplication.getString("username"))
                .putString("evaluationName", duplication.getString("name"))
                .putString("date", duplication.getString("date"))
                .putString("className", className)
                .putString("studentName", studentName)
                .putString("skillName", duplication.getString("nom"))
                .putNumber("evaluation", duplication.getNumber("evaluation"));
    }

    private JsonObject findRightSkill (JsonArray skills) {
        JsonObject skill = skills.get(0);
        Double skillDate = skillGreaterDate(skill);
        if (skills.size() > 1) {
            for (int i = 1; i < skills.size(); i++) {
                JsonObject currentSkill = skills.get(i);
                if (Objects.equals(skill.getInteger("evaluation"), currentSkill.getInteger("evaluation"))) {
                    continue;
                }
                Double currentSkillDate =  skillGreaterDate(currentSkill);
                if (currentSkillDate >= skillDate) {
                    skill = currentSkill;
                    skillDate = currentSkillDate;
                }
            }
        }
        return skill;
    }

    private String generateReport (JsonArray report) {
        StringBuilder csvReport = new StringBuilder(getCsvHeader() + "\n");
        for (int i = 0; i < report.size(); i++) {
            csvReport.append(generateCsvReportLine((JsonObject) report.get(i))).append("\n");
        }
        return csvReport.toString();
    }

    private Double skillGreaterDate (JsonObject skill) {
        if (null == skill.getNumber("modified")) {
            return skill.getNumber("created").doubleValue();
        }
        Number created = skill.getNumber("created");
        Number modified = skill.getNumber("modified");
        return created.doubleValue() > modified.doubleValue()
                ? created.doubleValue()
                : modified.doubleValue();
    }

    private String generateCsvReportLine (JsonObject reportLine) {
        String line = reportLine.getString("structureName") + ";" +
                reportLine.getString("teacherName") + ";" +
                reportLine.getString("evaluationName").replace(";", ".") + ";" +
                reportLine.getString("date") + ";" +
                reportLine.getString("className") + ";" +
                reportLine.getString("studentName") + ";" +
                reportLine.getString("skillName").replace(";", ".") + ";" +
                reportLine.getNumber("evaluation") + ";";

        line = line + (reportLine.containsField("comment") ? reportLine.getString("comment") : "");
        return line;
    }

    private String getCsvHeader () {
        return "Etablissement;Enseignant;Devoir;Date;Classe;Eleve;Competence;Valeur;Commentaire";
    }
}
