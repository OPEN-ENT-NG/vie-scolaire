package fr.openent.evaluations.service.impl;

import fr.openent.Viescolaire;
import fr.openent.evaluations.service.*;
import fr.openent.viescolaire.service.ClasseService;
import fr.openent.viescolaire.service.EleveService;
import fr.openent.viescolaire.service.MatiereService;
import fr.openent.viescolaire.service.PeriodeService;
import fr.openent.viescolaire.service.impl.*;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.I18n;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonElement;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static fr.wseduc.webutils.http.Renders.getHost;

public class DefaultExportService implements ExportService {

    protected static final Logger log = LoggerFactory.getLogger(DefaultExportService.class);

    /**
     * Déclaration des services
     */
    private DevoirService devoirService;
    private UtilsService utilsService;
    private MatiereService matiereService;
    private PeriodeService periodeService;
    private ClasseService classeService;
    private BFCService bfcService;
    private DomainesService domaineService;
    private EleveService eleveService;
    private CompetenceNoteService competenceNoteService;
    private NoteService noteService;
    private CompetencesService competencesService;
    private NiveauDeMaitriseService niveauDeMaitriseService;
    private AnnotationService annotationsService;

    public DefaultExportService() {
        devoirService = new DefaultDevoirService(Viescolaire.EVAL_SCHEMA, Viescolaire.EVAL_DEVOIR_TABLE);
        utilsService = new DefaultUtilsService();
        matiereService = new DefaultMatiereService();
        periodeService = new DefaultPeriodeService();
        classeService = new DefaultClasseService();
        bfcService = new DefaultBFCService(Viescolaire.EVAL_SCHEMA, Viescolaire.EVAL_BFC_TABLE);
        domaineService = new DefaultDomaineService(Viescolaire.EVAL_SCHEMA, Viescolaire.EVAL_DOMAINES_TABLE);
        eleveService = new DefaultEleveService();
        competenceNoteService = new DefaultCompetenceNoteService(Viescolaire.EVAL_SCHEMA, Viescolaire.EVAL_COMPETENCES_NOTES_TABLE);
        noteService = new DefaultNoteService(Viescolaire.EVAL_SCHEMA, Viescolaire.EVAL_NOTES_TABLE);
        competencesService = new DefaultCompetencesService(Viescolaire.EVAL_SCHEMA, Viescolaire.EVAL_COMPETENCES_TABLE);
        niveauDeMaitriseService = new DefaultNiveauDeMaitriseService();
        annotationsService = new DefaultAnnotationService(Viescolaire.EVAL_SCHEMA, Viescolaire.EVAL_REL_ANNOTATIONS_DEVOIRS_TABLE);
    }

    @Override
    public void getExportEval(final Boolean text, final JsonObject devoir, String idGroupe, final String idEtablissement,
                              HttpServerRequest request, final Handler<Either<String, JsonObject>> handler) {

        Long idDevoir = devoir.getLong("id");
        final AtomicBoolean answered = new AtomicBoolean();
        JsonArray elevesArray = new JsonArray();
        final JsonArray maitriseArray = new JsonArray();
        JsonArray competencesArray = new JsonArray();
        JsonArray notesArray = new JsonArray();
        JsonArray competencesNotesArray = new JsonArray();
        JsonArray annotationsArray = new JsonArray();

        final Handler<Either<String, JsonArray>> finalHandler = getDevoirFinalHandler(text, devoir, request, elevesArray,
                maitriseArray, competencesArray, notesArray, competencesNotesArray, annotationsArray, answered, handler);

        classeService.getEleveClasses(idEtablissement, new JsonArray().addString(idGroupe), true,
                getIntermediateHandler(elevesArray, finalHandler));
        competencesService.getDevoirCompetences(idDevoir,
                getIntermediateHandler(competencesArray, finalHandler));
        noteService.listNotesParDevoir(idDevoir,
                getIntermediateHandler(notesArray, finalHandler));
        competenceNoteService.getCompetencesNotesDevoir(idDevoir,
                getIntermediateHandler(competencesNotesArray, finalHandler));
        annotationsService.listAnnotations(idEtablissement,
                getIntermediateHandler(annotationsArray, finalHandler));
        utilsService.getCycle(Arrays.asList(idGroupe), new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> stringJsonArrayEither) {
                if (stringJsonArrayEither.isRight()) {
                    Long idCycle = ((JsonObject) stringJsonArrayEither.right().getValue().get(0)).getLong("id_cycle");
                    niveauDeMaitriseService.getNiveauDeMaitrise(idEtablissement, idCycle,
                            getIntermediateHandler(maitriseArray, finalHandler));
                } else {
                    finalHandler.handle(new Either.Left<String, JsonArray>(stringJsonArrayEither.left().getValue()));
                }
            }
        });
    }

    private Handler<Either<String, JsonArray>> getDevoirFinalHandler(final Boolean text,
                                                                     final JsonObject devoir, final HttpServerRequest request,
                                                                     final JsonArray eleves, final JsonArray maitrises,
                                                                     final JsonArray competences, final JsonArray notes,
                                                                     final JsonArray competencesNotes, final JsonArray annotations,
                                                                     final AtomicBoolean answered,
                                                                     final Handler<Either<String, JsonObject>> responseHandler) {

        final AtomicBoolean elevesDone = new AtomicBoolean();
        final AtomicBoolean maitriseDone = new AtomicBoolean();
        final AtomicBoolean competencesDone = new AtomicBoolean();
        final AtomicBoolean notesDone = new AtomicBoolean();
        final AtomicBoolean competencesNotesDone = new AtomicBoolean();
        final AtomicBoolean annotationsDone = new AtomicBoolean();

        return new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> stringJsonArrayEither) {
                if (!answered.get()) {
                    if (stringJsonArrayEither.isRight()) {

                        elevesDone.set(eleves.size() > 0);
                        maitriseDone.set(maitrises.size() > 0);
                        competencesDone.set(competences.size() > 0);
                        notesDone.set(notes.size() > 0);
                        competencesNotesDone.set(competencesNotes.size() > 0);
                        annotationsDone.set(annotations.size() > 0);

                        if (elevesDone.get()
                                && maitriseDone.get()
                                && competencesDone.get()
                                && notesDone.get()
                                && competencesNotesDone.get()
                                && annotationsDone.get()) {
                            answered.set(true);

                            if (eleves.contains("empty")
                                    || maitrises.contains("empty")
                                    || (competencesNotes.contains("empty") && notes.contains("empty"))
                                    || annotations.contains("empty")) {
                                answered.set(true);
                                responseHandler.handle(new Either.Left<String, JsonObject>("exportDevoir : empty result."));
                            } else {

                                getDevoirInfos(devoir, request, new Handler<Either<String, JsonObject>>() {
                                    @Override
                                    public void handle(Either<String, JsonObject> stringJsonObjectEither) {
                                        if (stringJsonObjectEither.isRight()) {
                                            Map<String, Map<String, JsonObject>> competenceNoteElevesMap = new HashMap<>();
                                            for (int i = 0; i < competencesNotes.size(); i++) {
                                                JsonObject competenceNote = competencesNotes.get(i);
                                                if (!competenceNoteElevesMap.containsKey(competenceNote.getString("id_eleve"))) {
                                                    competenceNoteElevesMap.put(
                                                            competenceNote.getString("id_eleve"),
                                                            new HashMap<String, JsonObject>());
                                                }
                                                competenceNoteElevesMap.get(competenceNote.getString("id_eleve"))
                                                        .put(String.valueOf(competenceNote.getLong("id_competence")), competenceNote);
                                            }
                                            responseHandler.handle(new Either.Right<String, JsonObject>(
                                                    formatJsonObjectExportDevoir(text,
                                                            stringJsonObjectEither.right().getValue(),
                                                            extractData(orderBy(eleves, "lastName"), "id"),
                                                            extractData(orderBy(addMaitriseNE(maitrises), "ordre"), "ordre"),
                                                            extractData(orderBy(competences, "code_domaine"),"id_competence"),
                                                            extractData(notes, "id_eleve"),
                                                            extractData(annotations, "id"),
                                                            competenceNoteElevesMap)));

                                        } else {
                                            responseHandler.handle(new Either.Left<String, JsonObject>("formatJsonObjectExportDevoir : an error occured."));
                                        }
                                    }
                                });
                            }
                        }
                    } else {
                        answered.set(true);
                        responseHandler.handle(new Either.Left<String, JsonObject>("exportDevoir : empty result."));
                    }
                }
            }
        };
    }

    private JsonObject formatJsonObjectExportDevoir(final Boolean text, final JsonObject devoir,
                                                    final Map<String, JsonObject> eleves,
                                                    final Map<String, JsonObject> maitrises,
                                                    final Map<String, JsonObject> competences,
                                                    final Map<String, JsonObject> notes,
                                                    final Map<String, JsonObject> annotations,
                                                    final Map<String, Map<String, JsonObject>> competenceNotes) {

        JsonObject result = new JsonObject();
        result.putBoolean("text", text);

        Map<String, String> competenceIndice = new LinkedHashMap<>();
        int i = 1;
        for (Map.Entry<String, JsonObject> competence : competences.entrySet()) {
            competenceIndice.put("[C" + String.valueOf(i) + "]", String.valueOf(competence.getValue().getLong("id_competence")));
            i++;
        }

        //Devoir
        devoir.removeField("id");
        result.putObject("devoir", devoir);

        //Maitrise
        JsonArray maitrisesArray = new JsonArray();
        for (JsonObject maitrise : maitrises.values()) {
            JsonObject _maitrise = new JsonObject();
            _maitrise.putString("libelle", maitrise.getString("libelle"));
            _maitrise.putString("visu", String.valueOf(maitrise.getLong("ordre")));
            maitrisesArray.add(_maitrise);
        }
        result.putArray("maitrise", maitrisesArray);

        //Competences
        JsonArray competencesArray = new JsonArray();
        for (Map.Entry<String, String> competence : competenceIndice.entrySet()) {
            competencesArray.addString(competence.getKey() + " " + competences.get(competence.getValue()).getString("code_domaine") + " " + competences.get(competence.getValue()).getString("nom"));
        }
        result.putArray("competence", competencesArray);

        //Eleves
        JsonArray elevesArray = new JsonArray();

        //Header
        JsonObject headerEleves = new JsonObject();
        headerEleves.putString("header", "");
        headerEleves.putString("note", "Note");
        headerEleves.putArray("competenceNotes", new JsonArray());
        for (String indice : competenceIndice.keySet()) {
            headerEleves.getArray("competenceNotes").addString(indice);
        }
        result.putObject("elevesHeader", headerEleves);

        //Body
        for (Map.Entry<String, JsonObject> eleve : eleves.entrySet()) {
            JsonObject eleveObject = new JsonObject();
            eleveObject.putString("header", eleve.getValue().getString("displayName"));

            String note = "";
            Boolean hasAnnotation = false;
            if (notes.containsKey(eleve.getKey())) {
                if (notes.get(eleve.getKey()).getString("appreciation") != null && !notes.get(eleve.getKey()).getString("appreciation").equals("")) {
                    eleveObject.putString("appreciation", notes.get(eleve.getKey()).getString("appreciation"));
                    eleveObject.putNumber("appreciationColspan", competences.size() + 1);
                }
                if (notes.get(eleve.getKey()).getLong("id_annotation") != null) {
                    note = annotations.get(String.valueOf(notes.get(eleve.getKey()).getLong("id_annotation"))).getString("libelle_court");
                    hasAnnotation = true;
                } else {
                    note = notes.get(eleve.getKey()).getString("valeur");
                }
            }
            eleveObject.putString("note", note);


            JsonArray comptenceNotesEleves = new JsonArray();
            for (String competence : competenceIndice.values()) {
                if (hasAnnotation) {
                    comptenceNotesEleves.addString("");
                } else if (competenceNotes.containsKey(eleve.getKey()) && competenceNotes.get(eleve.getKey()).containsKey(competence)) {
                    Map<String, JsonObject> competenceNotesEleve = competenceNotes.get(eleve.getKey());
                    String evaluation = String.valueOf(competenceNotesEleve.get(competence).getLong("evaluation"));
                    comptenceNotesEleves.addString(String.valueOf(Integer.valueOf(evaluation) + 1));
                } else {
                    comptenceNotesEleves.addString("0");
                }
                eleveObject.putArray("competenceNotes", comptenceNotesEleves);
            }
            elevesArray.addObject(eleveObject);
        }
        result.putArray("eleves", elevesArray);

        result.putString("height", String.valueOf(calcNumbLine(result)) + "%");

        return result;
    }
    @Override
    public void getExportReleveComp(final Boolean text, final String idEleve, final String[] idGroupes, final String idEtablissement, String idMatiere,
                                    Long idPeriodeType, final Handler<Either<String, JsonObject>> handler) {

        final AtomicBoolean answered = new AtomicBoolean();
        final JsonArray maitriseArray = new JsonArray();
        final JsonArray devoirsArray = new JsonArray();
        final JsonArray competencesArray = new JsonArray();
        final JsonArray domainesArray = new JsonArray();
        final JsonArray competencesNotesArray = new JsonArray();

        final Handler<Either<String, JsonArray>> finalHandler = getReleveCompFinalHandler(text, devoirsArray,
                maitriseArray, competencesArray, domainesArray, competencesNotesArray, answered, handler);

        devoirService.listDevoirs(idGroupes, null,
                idPeriodeType != null ? new Long[]{idPeriodeType} : null,
                idEtablissement != null ? new String[]{idEtablissement} : null,
                idMatiere != null ? new String[]{idMatiere} : null,
                getIntermediateHandler(devoirsArray, new Handler<Either<String, JsonArray>>() {
                    @Override
                    public void handle(Either<String, JsonArray> stringJsonArrayEither) {
                        if(stringJsonArrayEither.isRight() && !(stringJsonArrayEither.right().getValue().get(0) instanceof String)) {
                            for (int i = 0; i < stringJsonArrayEither.right().getValue().size(); i++) {
                                Long idDevoir = ((JsonObject) stringJsonArrayEither.right().getValue().get(i)).getLong("id");
                                competencesService.getDevoirCompetences(idDevoir,
                                        getIntermediateHandler(competencesArray, finalHandler));
                                competenceNoteService.getCompetencesNotes(idDevoir, idEleve,
                                        getIntermediateHandler(competencesNotesArray, finalHandler));
                            }
                            domaineService.getDomainesRacines(idGroupes[0],
                                    getIntermediateHandler(domainesArray, finalHandler));
                        } else if (stringJsonArrayEither.right().getValue().get(0) instanceof String){
                            finalHandler.handle(new Either.Left<String, JsonArray>("getExportReleveComp : No exams on given period and/or material."));
                        } else {
                            finalHandler.handle(stringJsonArrayEither.left());
                        }
                    }
                }));
        utilsService.getCycle(Arrays.asList(idGroupes), new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> stringJsonArrayEither) {
                if (stringJsonArrayEither.isRight()) {
                    Long idCycle = ((JsonObject) stringJsonArrayEither.right().getValue().get(0)).getLong("id_cycle");
                    for (int i = 0; i < stringJsonArrayEither.right().getValue().size(); i++) {
                        JsonObject cycleObj = stringJsonArrayEither.right().getValue().get(i);
                        if(!idCycle.equals(cycleObj.getLong("id_cycle"))) {
                            finalHandler.handle(new Either.Left<String, JsonArray>("getExportReleveComp : Given groups belong to different cycle."));
                        }
                    }
                    niveauDeMaitriseService.getNiveauDeMaitrise(idEtablissement, idCycle,
                            getIntermediateHandler(maitriseArray, finalHandler));
                } else {
                    finalHandler.handle(new Either.Left<String, JsonArray>(stringJsonArrayEither.left().getValue()));
                }
            }
        });
    }

    private Handler<Either<String, JsonArray>> getIntermediateHandler(final JsonArray collection, final Handler<Either<String, JsonArray>> finalHandler) {
        return new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> stringJsonArrayEither) {
                if (stringJsonArrayEither.isRight()) {
                    JsonArray result = stringJsonArrayEither.right().getValue();
                    if (result.size() == 0) {
                        result.addString("empty");
                    }
                    utilsService.saUnion(collection, result);
                    finalHandler.handle(stringJsonArrayEither.right());
                } else {
                    finalHandler.handle(stringJsonArrayEither.left());
                }
            }
        };
    }

    private int getNbDiffKey(JsonArray collection, String key) {
        Set<String> keyShown = new HashSet<>();
        Integer nbEmpty = 0;
        for (int i = 0; i < collection.size(); i++) {
            if (collection.get(i) instanceof String) {
                nbEmpty++;
                continue;
            }
            JsonObject row = collection.get(i);
            String keyValue = String.valueOf(row.getField(key));
            if(!keyShown.contains(keyValue)) {
                keyShown.add(keyValue);
            }
        }
        return keyShown.size() + nbEmpty;
    }

    private Map<String, JsonObject> extractData(JsonArray collection, String key) {

        Map<String, JsonObject> result = new LinkedHashMap<>();

        for (int i = 0; i < collection.size(); i++) {
            if (collection.get(i) instanceof String) {
                break;
            }
            JsonObject item = collection.get(i);
            String itemKey = String.valueOf(item.getField(key));
            if(!result.containsKey(itemKey)) {
                result.put(itemKey, item);
            }
        }

        return result;
    }

    private JsonArray orderBy(JsonArray collection, String key) {
        Set<String> sortedSet = new TreeSet<>();
        Map<String, JsonArray> unsortedMap = new HashMap<>();
        JsonArray result = new JsonArray();

        for (int i = 0; i < collection.size(); i++) {
            if(collection.get(i) instanceof String) {
                continue;
            }
            JsonObject item = collection.get(i);
            String itemKey = String.valueOf(item.getField(key));
            if(!unsortedMap.containsKey(itemKey)) {
                unsortedMap.put(itemKey, new JsonArray());
            }
            unsortedMap.get(itemKey).add(item);
            sortedSet.add(itemKey);
        }

        for (String aSortedSet : sortedSet) {
            utilsService.saUnion(result, unsortedMap.get(aSortedSet));
        }
        return result;
    }

    private Handler<Either<String, JsonArray>> getReleveCompFinalHandler(final Boolean text, final JsonArray devoirs,
                                                                         final JsonArray maitrises, final JsonArray competences,
                                                                         final JsonArray domaines, final JsonArray competencesNotes,
                                                                         final AtomicBoolean answered,
                                                                         final Handler<Either<String, JsonObject>> responseHandler) {
        final AtomicBoolean devoirsDone = new AtomicBoolean();
        final AtomicBoolean maitriseDone = new AtomicBoolean();
        final AtomicBoolean competencesDone = new AtomicBoolean();
        final AtomicBoolean domainesDone = new AtomicBoolean();
        final AtomicBoolean competencesNotesDone = new AtomicBoolean();

        return new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> stringJsonArrayEither) {
                if (!answered.get()) {
                    if (stringJsonArrayEither.isRight()) {

                        devoirsDone.set(devoirs.size() > 0);
                        maitriseDone.set(maitrises.size() > 0);
                        domainesDone.set(domaines.size() > 0);
                        competencesDone.set(competences.size() > 0
                                && getNbDiffKey(competences, "id_devoir") == devoirs.size());
                        competencesNotesDone.set(competencesNotes.size() > 0
                                && getNbDiffKey(competencesNotes, "id_devoir") == devoirs.size());

                        if (devoirsDone.get()
                                && maitriseDone.get()
                                && competencesDone.get()
                                && competencesNotesDone.get()) {
                            answered.set(true);

                            if (devoirs.contains("empty")
                                    || maitrises.contains("empty")
                                    || domaines.contains("empty")) {
                                responseHandler.handle(new Either.Left<String, JsonObject>("exportReleveComp : empty result."));
                            } else {
                                Map<String, Map<String, Long>> competenceNotesMap = new HashMap<>();

                                for (int i = 0; i < competencesNotes.size(); i++) {
                                    if(competencesNotes.get(i) instanceof String) {
                                        continue;
                                    }
                                    JsonObject row = competencesNotes.get(i);
                                    String compKey = String.valueOf(row.getLong("id_competence"));
                                    String devoirKey = String.valueOf(row.getLong("id_devoir"));
                                    Long eval = row.getLong("evaluation");
                                    if (!competenceNotesMap.containsKey(devoirKey)) {
                                        competenceNotesMap.put(devoirKey, new HashMap<String, Long>());
                                    }
                                    if (!competenceNotesMap.get(devoirKey).containsKey(compKey)) {
                                        competenceNotesMap.get(devoirKey).put(compKey, eval);
                                    }
                                }

                                responseHandler.handle(new Either.Right<String, JsonObject>(
                                        formatJsonObjectExportReleveComp(
                                                text,
                                                new ArrayList<>(extractData(devoirs, "id").keySet()),
                                                extractData(orderBy(addMaitriseNE(maitrises), "ordre"), "ordre"),
                                                extractData(orderBy(competences, "nom"), "id_competence"),
                                                extractData(domaines, "id"),
                                                competenceNotesMap)));
                            }
                        }
                    } else {
                        answered.set(true);
                        responseHandler.handle(new Either.Left<String, JsonObject>("exportReleveComp : empty result."));
                    }
                }
            }
        };
    }

    private JsonArray addMaitriseNE(JsonArray maitrises) {
        JsonObject nonEvalue = new JsonObject();
        nonEvalue.putString("libelle", "Competence non evaluee");
        nonEvalue.putNumber("ordre", 0);
        nonEvalue.putString("default", "grey");
        maitrises.addObject(nonEvalue);

        return maitrises;
    }

    private JsonObject formatJsonObjectExportReleveComp(Boolean text, List<String> devoirs,
                                                        Map<String, JsonObject> maitrises,
                                                        Map<String, JsonObject> competences,
                                                        Map<String, JsonObject> domaines,
                                                        Map<String, Map<String, Long>> competenceNotesByDevoir) {
        JsonObject result = new JsonObject();
        result.putBoolean("text", text);

        JsonObject header = new JsonObject();
        JsonObject body = new JsonObject();

        //Maitrise
        JsonArray headerMiddle = new JsonArray();
        for (JsonObject maitrise : maitrises.values()) {
            JsonObject _maitrise = new JsonObject();
            _maitrise.putString("libelle", maitrise.getString("libelle"));
            _maitrise.putString("visu", String.valueOf(maitrise.getLong("ordre")));
            headerMiddle.add(_maitrise);
        }
        header.putArray("right", headerMiddle);
        result.putObject("header", header);

        Map<String, List<String>> competencesByDomain = new LinkedHashMap<>();
        for(String idDomain : domaines.keySet()) {
            competencesByDomain.put(idDomain, new ArrayList<String>());
        }

        for (Map.Entry<String, JsonObject> competence : competences.entrySet()) {
            String idDomain = competence.getValue().getString("ids_domaine");
            competencesByDomain.get(idDomain).add(competence.getKey());
        }

        JsonObject bodyHeader = new JsonObject();
        bodyHeader.putString("left", "Domaines / items");
        bodyHeader.putString("right", "Nb d'evaluations et Niveau des competences");
        body.putObject("header", bodyHeader);

        JsonArray bodyBody = new JsonArray();
        for(Map.Entry<String, List<String>> competencesInDomain : competencesByDomain.entrySet()) {
            if(competencesInDomain.getValue() == null || competencesInDomain.getValue().size() == 0) {
                continue;
            }
            JsonObject domainObj = new JsonObject();
            domainObj.putString("domainHeader", domaines.get(competencesInDomain.getKey()).getString("codification") + " " + domaines.get(competencesInDomain.getKey()).getString("libelle"));
            JsonArray competencesInDomainArray = new JsonArray();
            for(String competence : competencesInDomain.getValue()) {
                List<Long> valuesByComp = new ArrayList<>();
                for (String devoir : devoirs) {
                    if (competenceNotesByDevoir.containsKey(devoir) && competenceNotesByDevoir.get(devoir).containsKey(competence)) {
                        valuesByComp.add(competenceNotesByDevoir.get(devoir).get(competence) + 1);
                    } else {
                        valuesByComp.add(0L);
                    }
                }
                JsonObject competenceNote = new JsonObject();
                competenceNote.putString("header", competences.get(competence).getString("nom"));
                competenceNote.putArray("competenceNotes", calcWidthNote(text, maitrises, valuesByComp, devoirs.size()));
                competencesInDomainArray.addObject(competenceNote);
            }
            domainObj.putArray("domainBody", competencesInDomainArray);
            bodyBody.addObject(domainObj);
        }

        body.putArray("body", bodyBody);

        result.putObject("body", body);
        return result;
    }

    private void getDevoirInfos(JsonObject devoir, final HttpServerRequest request, final Handler<Either<String, JsonObject>> handler) {

        final AtomicBoolean handled = new AtomicBoolean();
        final Map<String, Object> devoirMap = new HashMap<>();

        final Handler<Either<String, Map<String, Object>>> finalHandler = new Handler<Either<String, Map<String, Object>>>() {
            @Override
            public void handle(Either<String, Map<String, Object>> stringMapEither) {
                if (!handled.get()) {
                    if (stringMapEither.isRight()) {
                        Map<String, Object> devoir = stringMapEither.right().getValue();
                        int checkDevoirInfos = checkDevoirInfos(devoir);
                        if (checkDevoirInfos == 0) {
                            handled.set(true);
                            handler.handle(new Either.Right<String, JsonObject>(new JsonObject(devoir)));
                        } else if (checkDevoirInfos == 1) {
                            handled.set(true);
                            handler.handle(new Either.Left<String, JsonObject>("getDevoirsInfos : Devoir doesn't respect format."));
                        }
                    } else {
                        handled.set(true);
                        handler.handle(new Either.Left<String, JsonObject>("getDevoirsInfos : Error handled"));
                    }
                }
            }
        };
        String[] date = devoir.getString("date").substring(0, devoir.getString("date").indexOf(" ")).split("-");
        devoirMap.put("date", date[2] + '/' + date[1] + '/' + date[0]);

        devoirMap.put("id", devoir.getLong("id"));
        devoirMap.put("nom", devoir.getString("name"));
        devoirMap.put("coeff", devoir.getString("coefficient"));
        devoirMap.put("sur", devoir.getLong("diviseur"));
        devoirMap.put("periode", I18n.getInstance().translate(
                "viescolaire.periode." + String.valueOf(devoir.getLong("periodetype")),
                getHost(request),
                I18n.acceptLanguage(request)) + " " + String.valueOf(devoir.getLong("periodeordre")));
        classeService.getClasseInfo(devoir.getString("id_groupe"),
                new Handler<Either<String, JsonObject>>() {
                    @Override
                    public void handle(Either<String, JsonObject> stringJsonObjectEither) {
                        if (stringJsonObjectEither.isRight()) {
                            devoirMap.put("classe", stringJsonObjectEither.right().getValue().getObject("c").getObject("data").getString("name"));
                            finalHandler.handle(new Either.Right<String, Map<String, Object>>(devoirMap));
                        } else {
                            finalHandler.handle(
                                    new Either.Left<String, Map<String, Object>>(
                                            "getDevoirsInfos : devoir '" + devoirMap.get("id") + "), couldn't get class name."));
                        }
                    }
                });

        matiereService.getMatiere(devoir.getString("id_matiere"),
                new Handler<Either<String, JsonObject>>() {
                    @Override
                    public void handle(Either<String, JsonObject> stringJsonObjectEither) {
                        if (stringJsonObjectEither.isRight()) {
                            devoirMap.put("matiere", stringJsonObjectEither.right().getValue().getObject("n").getObject("data").getString("label"));
                            finalHandler.handle(new Either.Right<String, Map<String, Object>>(devoirMap));
                        } else {
                            finalHandler.handle(
                                    new Either.Left<String, Map<String, Object>>(
                                            "getDevoirsInfos : devoir '" + devoirMap.get("id") + "), couldn't get matiere name."));
                        }
                    }
                });
    }

    private int checkDevoirInfos(Map<String, Object> devoir) {
        List<String> params = new ArrayList<>(Arrays.asList("classe", "nom", "matiere", "periode", "date", "coeff", "sur"));

        if (devoir.containsValue(null)) {
            return 1;
        } else {
            for (String param : params) {
                if (!devoir.keySet().contains(param)) {
                    return 2;
                }
            }
        }
        return 0;
    }


    // TODO passage en pixel
    private double calcNumbLine(JsonObject result) {
        JsonObject devoirHeader = result.getObject("devoir");
        if (result.containsField("competence")) {
            JsonArray competenceHeader = result.getArray("competence");
            Integer size = 0;
            Integer line = 0;
            Integer length = 145; // le nombre de caractére max dans une ligne
            Double height = 0.885D; // la hauteur d'une ligne
            for (int i = 0; i < competenceHeader.size(); i++) {
                String competence = competenceHeader.get(i);
                size = competence.length(); // +10 pour "[ Cx ]"
                line += size / length;
                if (size % length > 0) {
                    line++;
                }
            }

            Double totalHeight = line * height;
            return totalHeight;
        }
        return 0;
    }

    private JsonArray calcWidthNote(Boolean text, Map<String, JsonObject> maitrises, List<Long> competenceNotes, Integer nbDevoir) {
        Map<Long, Integer> occNote = new HashMap<>();
        for(Long competenceNote : competenceNotes) {
            if(!occNote.containsKey(competenceNote)) {
                occNote.put(competenceNote, 0);
            }
            occNote.put(competenceNote, occNote.get(competenceNote) + 1);
        }

        JsonArray resultList = new JsonArray();
        for(Map.Entry<Long, Integer> notesMaitrises : occNote.entrySet()) {
            JsonObject competenceNotesObj = new JsonObject();
            String number = text ? maitrises.get(String.valueOf(notesMaitrises.getKey())).getString("lettre") : String.valueOf(notesMaitrises.getValue());
            if(number == null) {
                number = "NE";
            }
            competenceNotesObj.putString("number", number);
            String color = text ? "white" : maitrises.get(String.valueOf(notesMaitrises.getKey())).getString("default");
            competenceNotesObj.putString("color", color);
            competenceNotesObj.putString("width", String.valueOf(Integer.valueOf(notesMaitrises.getValue()) / (double) nbDevoir * 100D));
            resultList.add(competenceNotesObj);
        }
        return resultList;
    }
}
