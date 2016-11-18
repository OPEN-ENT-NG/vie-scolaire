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

package fr.openent.evaluations.controller;

import fr.openent.Viescolaire;
import fr.openent.evaluations.bean.NoteDevoir;
import fr.openent.evaluations.service.DevoirService;
import fr.openent.evaluations.service.UtilsService;
import fr.openent.evaluations.service.impl.DefaultDevoirService;
import fr.openent.evaluations.service.impl.DefaultUtilsService;
import fr.openent.viescolaire.service.MatiereService;
import fr.openent.viescolaire.service.PeriodeService;
import fr.openent.viescolaire.service.impl.DefaultMatiereService;
import fr.openent.viescolaire.service.impl.DefaultPeriodeService;
import fr.wseduc.rs.Get;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.email.EmailSender;
import fr.wseduc.webutils.http.Renders;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.entcore.directory.services.UserService;
import org.entcore.directory.services.impl.DefaultUserService;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.entcore.common.http.response.DefaultResponseHandler.leftToResponse;

/**
 * Created by ledunoiss on 05/08/2016.
 */
public class ExportPDFController extends ControllerHelper {
    private String assetsPath = "../..";
    private Map<String, String> skins = new HashMap<String, String>();

    private String node;

    /**
     * Déclaration des services
     */
    private DevoirService devoirService;
    private UserService userService;
    private UtilsService utilsService;
    private MatiereService matiereService;
    private PeriodeService periodeService;

    public ExportPDFController(EventBus eb, EmailSender notification) {
        pathPrefix = Viescolaire.EVAL_PATHPREFIX;
        devoirService = new DefaultDevoirService(Viescolaire.EVAL_SCHEMA, Viescolaire.EVAL_DEVOIR_TABLE);
        utilsService = new DefaultUtilsService();
        matiereService = new DefaultMatiereService();
        periodeService = new DefaultPeriodeService();
        userService = new DefaultUserService(notification, eb);
    }

    /**
     * Récupère le nom des enseignants de chacune des matières puis positionne
     * les devoirs de l'élève sur les bonnes matières et enfin génère le PDF associé
     * formant le relevé de notes de l'élève.
     *
     * @param request
     * @param user l'utilisateur connecté.
     * @param matieres la liste des matières de l'élève.
     * @param classe
     * @param classesFieldOfStudy
     * @param devoirsJson la liste des devoirs et notes de l'élève.
     * @param periodeJson la periode
     * @param userJson l'élève
     * @param etabJson l'établissement
     */
    public void getEnseignantsMatieres(final HttpServerRequest request, final UserInfos user, final JsonArray matieres,
                                       final String classe, ArrayList<String> classesFieldOfStudy, final JsonArray devoirsJson,
                                       final JsonObject periodeJson, final JsonObject userJson, final JsonObject etabJson) {

        matiereService.getEnseignantsMatieres(classesFieldOfStudy, new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> eventEnseignantsMatieres) {

                JsonArray matieresArray = new JsonArray();
                if(eventEnseignantsMatieres.isRight()){
                    JsonArray r = eventEnseignantsMatieres.right().getValue();

                    ArrayList<String> matieresExternalList = new ArrayList<String>();

                    for(int i = 0 ; i < matieres.size(); i++){
                        JsonObject matiere = matieres.get(i);
                        matieresExternalList.add(classe + "$" + matiere.getObject("f").getObject("data").getString("externalId"));
                    }

                    JsonObject n = new JsonObject();
                    JsonObject enseignant = new JsonObject();
                    for(int i = 0; i < r.size(); i++){
                        n = r.get(i);
                        enseignant = n.getObject("n").getObject("data");
                        JsonArray classes = enseignant.getField("classesFieldOfStudy");
                        for(int j = 0; j < classes.size(); j++){
                            if(matieresExternalList.contains(classes.get(j))){
                                JsonObject matiere = matieres.get(matieresExternalList.indexOf(classes.get(j)));
                                final JsonObject matiereInter = matiere.getObject("f").getObject("data");
                                matiereInter.putString("displayEnseignantName", enseignant.getString("displayName"));

                                String firstNameEnsiegnant = enseignant.getString("firstName");
                                matiereInter.putString("firstNameEnseignant", firstNameEnsiegnant);
                                matiereInter.putString("firstNameInitialeEnseignant", firstNameEnsiegnant.substring(0,1)+".");
                                matiereInter.putString("surnameEnseignant", enseignant.getString("surname"));
                                matiereInter.putString("idEnseignant", enseignant.getString("id"));
                                //response.add(matiereInter);

                                // récupération des devoirs de la matière et positionnement sur l'objet JSON
                                getDevoirsByMatiere(devoirsJson, matiereInter);
                                matieresArray.add(matiereInter);
                            }
                        }
                    }
                    final JsonObject templateProps = new JsonObject();

                    templateProps.putArray("matieres", matieresArray);
                    templateProps.putObject("periode", periodeJson);
                    templateProps.putObject("user", userJson.getObject("u").getObject("data"));
                    templateProps.putObject("classe", userJson.getObject("c").getObject("data"));
                    templateProps.putObject("etablissement", etabJson);
                    String templateName = "releve-eleve.pdf.xhtml";

                    String prefixPdfName = "releve-eleve";
                    prefixPdfName+= "-" + userJson.getObject("u").getObject("data").getString("displayName");
                    prefixPdfName+= "-" + userJson.getObject("c").getObject("data").getString("name");

                    String etablissementName = etabJson.getString("name");
                    etablissementName = etablissementName.trim().replaceAll(" ", "-");
                    prefixPdfName+= "-" + etablissementName;

                    genererPdf(request, templateProps, templateName, prefixPdfName);

                }else{
                    leftToResponse(request, eventEnseignantsMatieres.left());
                }
            }

        });
    }

    /**
     * Récupère les devoirs de la matière et les positionnent sur celle ci.
     *
     * @param devoirsJson la liste de tous les devoirs de l'élève.
     * @param matiereInter la matière dont on cherche les devoirs.
     */
    private void getDevoirsByMatiere(JsonArray devoirsJson, JsonObject matiereInter) {

        JsonArray devoirsMatiereJson = new JsonArray();

        List<NoteDevoir> listeNoteDevoirs = new ArrayList<NoteDevoir>();

        // parcours des devoirs
        for (int i = 0; i < devoirsJson.size(); i++) {
            JsonObject devoirJson = devoirsJson.get(i);

            // boolean permettant de savoir s'il y a un coefficient différent de 1 sur la note
            devoirJson.putBoolean("hasCoeff", !Double.valueOf(devoirJson.getString("coefficient")).equals(new Double(1)));

            // ajout du devoir sur la matiere, si son identifiant de matière correspond bien
            if(matiereInter.getString("id").equals(devoirJson.getString("idmatiere"))) {
                devoirsMatiereJson.add(devoirJson);
                NoteDevoir noteDevoir = new NoteDevoir(Double.valueOf(devoirJson.getString("note")),
                        devoirJson.getInteger("diviseur"),
                        devoirJson.getBoolean("ramenersur"),
                        Double.valueOf(devoirJson.getString("coefficient")));
                listeNoteDevoirs.add(noteDevoir);
            }
        }
        matiereInter.putArray("devoirs", devoirsMatiereJson);

        boolean hasDevoirs = !listeNoteDevoirs.isEmpty();
        matiereInter.putBoolean("hasDevoirs", hasDevoirs);

        if(hasDevoirs) {
            // calcul de la moyenne de l'eleve pour la matiere
            JsonObject moyenneMatiere = utilsService.calculMoyenne(listeNoteDevoirs, false, 20);// TODO recuper le diviseur de la matiere
            // ajout sur l'objet json
            matiereInter.putString("moyenne", moyenneMatiere.getNumber("moyenne").toString());
        }
    }

    /**
     * Genere le releve d'un eleve sous forme de PDF
     *
     */
    @Get("/releve/pdf")
    @SecuredAction(value = "", type= ActionType.AUTHENTICATED)
    public void getReleveEleve(final HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if(user != null){

                    // parametres de l'url
                    MultiMap params = request.params();
                    final Integer idPeriode = Integer.parseInt(params.get("idPeriode"));
                    final String idEtablissement = params.get("idEtablissement");
                    final String idUser = params.get("idUser");


                    // TODO verifier que l'utilisateur connecte est bien l'eleve dont essaie d'acceder au releve ou que
                    // le parent connecte essaie bien d'acceder au releve d'un de ses eleves

                    // récupération de l'élève
                    utilsService.getInfoEleve(idUser, new Handler<Either<String,JsonObject>>() {

                        @Override
                        public void handle(Either<String, JsonObject> eventUser) {
                            if(eventUser.isRight()) {
                                final JsonObject userJSON = eventUser.right().getValue();

                                final String classeEleve = userJSON.getObject("u").getObject("data").getArray("classes").get(0);

                                // Récupération de la liste des devoirs de la personne avec ses notes associées
                                devoirService.listDevoirs(idEtablissement, idPeriode, idUser,new Handler<Either<String, JsonArray>>() {
                                    @Override
                                    public void handle(final Either<String, JsonArray> eventListDevoirs) {
                                        if(eventListDevoirs.isRight()){

                                            // devoirs de l'eleve (avec ses notes) sous forme d'objet JSON
                                            final JsonArray devoirsJSON = eventListDevoirs.right().getValue();

                                            // récupération de l'ensemble des matières de l'élève
                                            matiereService.listMatieresEleve(request.params().get("idUser"), new Handler<Either<String, JsonArray>>() {
                                                @Override
                                                public void handle(Either<String, JsonArray> eventListMatieresEleve) {
                                                    if(eventListMatieresEleve.isRight()){

                                                        //formatage du resultat
                                                        JsonArray r = eventListMatieresEleve.right().getValue();
                                                        final ArrayList<String> classesFieldOfStudy = new ArrayList<String>();
                                                        String key = new String();
                                                        JsonObject f = new JsonObject();
                                                        final JsonArray matieres = r;

                                                        for(int i = 0; i < r.size(); i++){
                                                            JsonObject o = r.get(i);
                                                            f = o.getObject("f");

                                                            key = classeEleve+"$"+f.getObject("data").getString("externalId");
                                                            classesFieldOfStudy.add(key);
                                                        }

                                                        // récupération de la période
                                                        periodeService.getPeriode(idPeriode, new Handler<Either<String,JsonObject>>() {

                                                            @Override
                                                            public void handle(Either<String, JsonObject> eventPeriode) {
                                                                if(eventPeriode.isRight()) {
                                                                    final JsonObject periodeJSON = eventPeriode.right().getValue();


                                                                    // recuperation etablissement
                                                                    utilsService.getStructure(idEtablissement, new Handler<Either<String,JsonObject>>() {

                                                                        @Override
                                                                        public void handle(Either<String, JsonObject> eventStructure) {
                                                                            if(eventStructure.isRight()) {
                                                                                JsonObject etabJSON = eventStructure.right().getValue();
                                                                                etabJSON = etabJSON.getObject("s").getObject("data");
                                                                                // pour chaque matiere on recupere egalement le nom des enseignants
                                                                                getEnseignantsMatieres(request, user, matieres, classeEleve, classesFieldOfStudy, devoirsJSON, periodeJSON, userJSON, etabJSON);
                                                                            }
                                                                        }
                                                                    });// recuperation etablissement
                                                                }
                                                            }

                                                        }); // fin getPeriode



                                                    }else{
                                                        leftToResponse(request, eventListMatieresEleve.left());
                                                    }

                                                } // fin handle listMatieresEleve
                                            }); // fin listMatieresEleve
                                        }else{
                                            leftToResponse(request, eventListDevoirs.left());
                                        }

                                    } // fin handle listDevoirs
                                }); // fin lisDevoirs
                            }
                        }
                    }); // fin récupération élève
                }else{
                    unauthorized(request);
                }
            }
        });
    }

    /**
     * Generation d'un PDF à partir d'un template xhtml
     * @param request
     * @param templateProps objet JSON contenant l'ensemble des valeurs à remplir dans le template
     * @param templateName nom du template
     * @param prefixPdfName prefixe du nom du pdf (qui sera complété de la date de génération)
     */
    private void genererPdf(final HttpServerRequest request, final JsonObject templateProps, final String templateName,
                            final String prefixPdfName) {
        final String dateDebut = new SimpleDateFormat("dd.MM.yyyy").format(new Date().getTime());
        log.info(new SimpleDateFormat("HH:mm:ss:S").format(new Date().getTime()) + " -> Debut Generation PDF du template " + templateName);

        this.assetsPath = (String) vertx.sharedData().getMap("server").get("assetPath");
        this.skins = vertx.sharedData().getMap("skins");
        final String assetsPath = this.assetsPath + "/assets/themes/" + this.skins.get(Renders.getHost(request));
        final String templatePath = assetsPath + "/template/viescolaire/";
        final String baseUrl = getScheme(request) + "://" + Renders.getHost(request) + "/assets/themes/" + this.skins.get(Renders.getHost(request)) + "/img/";

        node = (String) vertx.sharedData().getMap("server").get("node");
        if (node == null) {
            node = "";
        }
        vertx.fileSystem().readFile(templatePath + templateName, new Handler<AsyncResult<Buffer>>() {

            @Override
            public void handle(AsyncResult<Buffer> result) {
                if (!result.succeeded()) {
                    badRequest(request);
                    return;
                }
                StringReader reader = new StringReader(result.result().toString("UTF-8"));
                processTemplate(request, templateProps, templateName, reader, new Handler<Writer>() {

                    @Override
                    public void handle(Writer writer) {
                        String processedTemplate = ((StringWriter) writer).getBuffer().toString();
                        if (processedTemplate == null) {
                            badRequest(request);
                            return;
                        }
                        JsonObject actionObject = new JsonObject();
                        byte[] bytes;
                        try {
                            bytes = processedTemplate.getBytes("UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            bytes = processedTemplate.getBytes();
                            log.error(e.getMessage(), e);
                        }

                        actionObject
                                .putBinary("content", bytes)
                                .putString("baseUrl", baseUrl);
                        eb.send(node + "entcore.pdf.generator", actionObject, new Handler<Message<JsonObject>>() {
                            @Override
                            public void handle(Message<JsonObject> reply) {
                                JsonObject pdfResponse = reply.body();
                                if (!"ok".equals(pdfResponse.getString("status"))) {
                                    badRequest(request, pdfResponse.getString("message"));
                                    return;
                                }
                                byte[] pdf = pdfResponse.getBinary("content");
                                request.response().putHeader("Content-Type", "application/pdf");
                                request.response().putHeader("Content-Disposition",
                                        "attachment; filename="+prefixPdfName+"-"+dateDebut+".pdf");
                                request.response().end(new Buffer(pdf));
                                log.info(new SimpleDateFormat("HH:mm:ss:S").format(new Date().getTime()) + " -> Fin Generation PDF du template " + templateName);
                            }
                        });
                    }
                });

            }
        });

    }


}
