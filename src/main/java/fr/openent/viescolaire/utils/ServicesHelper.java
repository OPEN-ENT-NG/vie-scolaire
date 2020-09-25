package fr.openent.viescolaire.utils;

import fr.openent.viescolaire.helper.ModelHelper;
import fr.openent.viescolaire.model.Model;
import fr.openent.viescolaire.model.MultiTeaching;
import fr.openent.viescolaire.model.ServiceModel;
import fr.openent.viescolaire.service.GroupeService;
import fr.openent.viescolaire.service.MultiTeachingService;
import fr.openent.viescolaire.service.ServicesService;
import fr.openent.viescolaire.service.UtilsService;
import fr.openent.viescolaire.service.impl.DefaultGroupeService;
import fr.openent.viescolaire.service.impl.DefaultMultiTeachingService;
import fr.openent.viescolaire.service.impl.DefaultServicesService;
import fr.openent.viescolaire.service.impl.DefaultUtilsService;
import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import java.text.ParseException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;



public class ServicesHelper {
    private static final String COEFFICIENT = "coefficient" ;
    private static Logger log =  LoggerFactory.getLogger(ServicesHelper.class);
    private static UtilsService utilsService = new DefaultUtilsService();

    private static final String TYPE_GROUP_CLASS = "Class";
    private static final String TYPE_GROUP_MANUAL_GROUP = "ManualGroup";
    private static final String TYPE_GROUP_FUNCTIONAL_GROUP = "FunctionalGroup";
    private static final String EVALUABLE_STR = "evaluable";
    private static GroupeService groupeService = new DefaultGroupeService();

    public static void setParamsServices(JsonArray neoServices, JsonArray SQLServices,
                                         List<MultiTeaching> coTeachersList, List<ServiceModel> result, Boolean compressed) {
        for (Object o : neoServices) {
            JsonObject oDBService = normalizeMatiere((JsonObject) o);
            ServiceModel service = new ServiceModel();
            service.setIdTopic(oDBService.getString("id_matiere"));
            service.setIdTeacher(oDBService.getString("id_enseignant"));
            service.setIdGroup(oDBService.getString("id_groupe"));

            JsonObject criteria = new JsonObject();

            criteria.put("id_matiere", service.getIdTopic());
            criteria.put("id_enseignant", service.getIdTeacher());
            criteria.put("id_groupe", service.getIdGroup());

            JsonObject overwrittenService = utilsService.findWhere(SQLServices, criteria);
            if (overwrittenService != null) {
                SQLServices.remove(overwrittenService);
                service.setModalite(overwrittenService.getString("modalite"));
                service.setEvaluable(overwrittenService.getBoolean("evaluable"));
                service.setCoefficient(overwrittenService.getLong(COEFFICIENT));
                service.setVisible(overwrittenService.getBoolean("is_visible"));
            }

            List<ServiceModel> newServices = service.addCoteachers(coTeachersList);
            result.add(service);
            if(!newServices.isEmpty()){
                result.addAll(newServices);
            }
        }

        for (Object o : SQLServices) {
            JsonObject oParamService = (JsonObject) o;
            ServiceModel manualService = new ServiceModel();
            manualService.setIdTopic(oParamService.getString("id_matiere"));
            manualService.setIdTeacher(oParamService.getString("id_enseignant"));
            manualService.setIdGroup(oParamService.getString("id_groupe"));
            manualService.setModalite(oParamService.getString("modalite"));
            manualService.setEvaluable(oParamService.getBoolean("evaluable"));
            manualService.setCoefficient(oParamService.getLong(COEFFICIENT));
            manualService.setVisible(oParamService.getBoolean("is_visible"));
            manualService.setId_groups(compressed);

            manualService.setManual(true);

            List<ServiceModel> newServices = manualService.addCoteachers(coTeachersList);
            result.add(manualService);
            if(!newServices.isEmpty()){
                result.addAll(newServices);
            }
        }
    }

    private static JsonArray getIdNeo(List<ServiceModel> result) {
        Set<String> setGroupsIdsForNeo = new HashSet<String>();
        JsonArray groupsIdsForNeo = new JsonArray();
        for (int i = 0; i < result.size(); i++) {
            if (setGroupsIdsForNeo.add(result.get(i).getIdGroup()))
                groupsIdsForNeo.add(result.get(i).getIdGroup());
        }
        return groupsIdsForNeo;
    }

    public static void handleMultiTeaching(JsonArray neoServices, Handler<Either<String, JsonArray>> requestHandler,
                                           boolean manualGroups, boolean groups, boolean classes, boolean notEvaluable,
                                           boolean evaluable, boolean compressed, JsonArray SQLservices,
                                           List<ServiceModel> result, List<MultiTeaching> coTeachers,
                                           JsonArray multiTeachings) {
        if(!multiTeachings.isEmpty()){
            for(int i=0; i < multiTeachings.size() ; i++){
                try {
                    coTeachers.add(new MultiTeaching(multiTeachings.getJsonObject(i)));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        setParamsServices(neoServices, SQLservices, coTeachers, result, compressed);
        JsonArray groupsIdsForNeo = getIdNeo(result);
        groupeService.getTypesOfGroup(groupsIdsForNeo, getNeoReplyHandler(requestHandler, result, manualGroups, groups,
                classes, notEvaluable, evaluable, compressed));
    }

    private static Handler<Either<String, JsonArray>> getNeoReplyHandler(Handler<Either<String, JsonArray>> requestHandler, List<ServiceModel> result,
                                                                         boolean manualGroups, boolean groups, boolean classes,
                                                                         boolean notEvaluable, boolean evaluable, boolean compressed) {
        return new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> event) {
                if (event.isRight()){
                    List<ServiceModel> resultList = new ArrayList<>();
                    log.debug("FIN Appel bus");
                    JsonArray neoTypeGroups = event.right().getValue();
                    for(int i = 0 ; i < neoTypeGroups.size() ; i++){
                        JsonObject neoTypeGroup = neoTypeGroups.getJsonObject(i);
                        for(int j = 0 ; j < result.size() ; j++){
                            ServiceModel resultService = result.get(j);
                            if(resultService.getIdGroup().equals(neoTypeGroup.getString("id"))){
                                resultService.setTypeGroup(neoTypeGroup.getString("type"));
                                resultList.add(resultService);
                            }
                        }
                    }
                    filterResults(resultList, manualGroups, groups, classes, evaluable, notEvaluable);
                    if(compressed)
                        requestHandler.handle(new Either.Right<>(getCompressedService(resultList)));
                    else
                        requestHandler.handle(new Either.Right<>(ModelHelper.convertToJsonArray(resultList)));
                }else{
                    log.info(event.left().getValue());
                }
            }
        };
    }

    private static void filterResults(List<ServiceModel> resultList, boolean manualGroups, boolean groups,
                                      boolean classes, boolean evaluable, boolean notEvaluable) {
        List<Integer> posToDelete = new ArrayList<>();
        for(int i = resultList.size() -1 ; i >= 0; i--){
            boolean needToDelete;
            boolean isGoodTypeOfGroup = false;
            boolean hasGoodEvaluableFilter = false;
            ServiceModel resultElement = resultList.get(i);
            if(manualGroups == groups  && groups == classes){
                isGoodTypeOfGroup  = true;
            } else {
                if (manualGroups && resultElement.getTypeGroup().equals(TYPE_GROUP_MANUAL_GROUP)) {
                    isGoodTypeOfGroup = true;
                }
                if (groups && resultElement.getTypeGroup().equals(TYPE_GROUP_FUNCTIONAL_GROUP)) {
                    isGoodTypeOfGroup = true;
                }
                if (classes && resultElement.getTypeGroup().equals(TYPE_GROUP_CLASS)) {
                    isGoodTypeOfGroup = true;
                }
            }

            if(evaluable != notEvaluable) {
                if (notEvaluable && !resultElement.isEvaluable()) {
                    hasGoodEvaluableFilter = true;
                }
                if (evaluable && resultElement.isEvaluable()) {
                    hasGoodEvaluableFilter = true;
                }
            } else {
                hasGoodEvaluableFilter = true;
            }

            needToDelete = !isGoodTypeOfGroup || !hasGoodEvaluableFilter;
            if(needToDelete)
                posToDelete.add(i);
        }

        for (Integer pos : posToDelete) {
            resultList.remove((int) pos);
        }
    }

    private static JsonArray getCompressedService(List<ServiceModel> resultList) {
        JsonArray compressedServices = new JsonArray();
        sortServices(resultList);
        String id_topic = "", id_teacher = "";
        List<MultiTeaching> coTeachers = new ArrayList<>();
        List<MultiTeaching> substituteTeachers = new ArrayList<>();
        Boolean isVisible = true;
        JsonObject compressedService = new JsonObject();

        for(int i = 0; i < resultList.size(); i++) {
            ServiceModel resultService = resultList.get(i);
            if(id_topic.equals(resultService.getIdTopic())){
                if(id_teacher.equals(resultService.getIdTeacher())){
                    if(isVisible.equals(resultService.isVisible())) {
                        List<String> idsSecondTeacherL1 = coTeachers.stream().map(MultiTeaching::getSecondTeacherId)
                                .sorted().collect(Collectors.toList());
                        List<String> idsSecondTeacherL2 = resultService.getCoteachers().stream()
                                .map(MultiTeaching::getSecondTeacherId).sorted().collect(Collectors.toList());
                        List<Boolean> isVisibleSecondTeacherL1 = coTeachers.stream().map(MultiTeaching::isVisible)
                                .sorted().collect(Collectors.toList());
                        List<Boolean> isVisibleSecondTeacherL2 = resultService.getCoteachers().stream()
                                .map(MultiTeaching::isVisible).sorted().collect(Collectors.toList());
                        if (idsSecondTeacherL1.equals(idsSecondTeacherL2) &&
                                isVisibleSecondTeacherL1.equals(isVisibleSecondTeacherL2)) {
                            boolean conditionToCompresse = substituteTeachers.size() == resultService.getSubstituteTeachers().size();
                            for(int k = 0; k < substituteTeachers.size() && conditionToCompresse; k++){
                                for(int l = 0; l < resultService.getSubstituteTeachers().size() && conditionToCompresse; l++){
                                    MultiTeaching substituteTeacher = substituteTeachers.get(k);
                                    MultiTeaching substituteTeacherService = resultService.getSubstituteTeachers().get(l);
                                    conditionToCompresse = substituteTeacher != null &&
                                            substituteTeacherService != null &&
                                            substituteTeacher.getSecondTeacherId().equals(substituteTeacherService.getSecondTeacherId()) &&
                                            substituteTeacher.getStartDate().equals(substituteTeacherService.getStartDate()) &&
                                            substituteTeacher.getEndDate().equals(substituteTeacherService.getEndDate()) &&
                                            substituteTeacher.getEnteredEndDate().equals(substituteTeacherService.getEnteredEndDate()) &&
                                            substituteTeacher.isVisible() == substituteTeacherService.isVisible() ||
                                            substituteTeacher == null && substituteTeacherService == null;
                                }
                            }

                            if (conditionToCompresse) {
                                compressedService.getJsonArray("id_groups").add(resultService.getIdGroup());
                                addIdMtultiTeaching(compressedService, resultService);
                                addCompetencesParams(compressedService, resultService);
                            } else {
                                compressedServices.add(compressedService);
                                substituteTeachers = resultService.getSubstituteTeachers();

                                compressedService = resultService.toJsonObject();
                                compressedService.put("id_groups", new JsonArray().add(resultService.getIdGroup()));
                                setCompetencesParams(compressedService, resultService);
                            }

                        } else {
                            compressedServices.add(compressedService);
                            coTeachers = resultService.getCoteachers();
                            substituteTeachers = resultService.getSubstituteTeachers();

                            compressedService = resultService.toJsonObject();
                            compressedService.put("id_groups", new JsonArray().add(resultService.getIdGroup()));
                            setCompetencesParams(compressedService, resultService);
                        }
                    } else {
                        isVisible = resultService.isVisible();
                        id_teacher = resultService.getIdTeacher();
                        coTeachers = resultService.getCoteachers();
                        substituteTeachers = resultService.getSubstituteTeachers();
                        compressedServices.add(compressedService);

                        compressedService = resultService.toJsonObject();
                        compressedService.put("id_groups", new JsonArray().add(resultService.getIdGroup()));
                        setCompetencesParams(compressedService, resultService);
                    }
                } else {
                    id_teacher = resultService.getIdTeacher();
                    coTeachers = resultService.getCoteachers();
                    substituteTeachers = resultService.getSubstituteTeachers();
                    compressedServices.add(compressedService);

                    compressedService = resultService.toJsonObject();
                    compressedService.put("id_groups", new JsonArray().add(resultService.getIdGroup()));
                    setCompetencesParams(compressedService, resultService);
                }
            } else {
                if(i != 0){
                    compressedServices.add(compressedService);
                }
                id_topic = resultService.getIdTopic();
                id_teacher = resultService.getIdTeacher();
                coTeachers = resultService.getCoteachers();
                substituteTeachers = resultService.getSubstituteTeachers();

                compressedService = resultService.toJsonObject();
                compressedService.put("id_groups", new JsonArray().add(resultService.getIdGroup()));
                setCompetencesParams(compressedService, resultService);
            }
        }
        compressedServices.add(compressedService);
        return compressedServices;
    }

    private static void addIdMtultiTeaching(JsonObject compressedService, ServiceModel resultService){
        JsonArray coTeachers = compressedService.getJsonArray("coTeachers");
        List<MultiTeaching> coteachersService = resultService.getCoteachers();

        for(MultiTeaching coTeacherOfList : coteachersService){
            for(int i = 0; i < coTeachers.size(); i++){
                JsonObject coteacher = coTeachers.getJsonObject(i);
                if(coTeacherOfList.getSecondTeacherId().equals(coteacher.getString("second_teacher_id"))){
                    coteacher.getJsonArray("idsAndIdsGroups").add(new JsonObject()
                            .put("id",coTeacherOfList.getId()).put("idGroup", coTeacherOfList.getClassOrGroupId() ));
                }
            }
        }

        JsonArray substituteTeachers = compressedService.getJsonArray("substituteTeachers");
        List<MultiTeaching> substituteTeachersService = resultService.getSubstituteTeachers();
        for(MultiTeaching substituteTeacherOfList : substituteTeachersService) {
            for(int i = 0; i < substituteTeachers.size(); i++) {
                JsonObject substituteTeacher = substituteTeachers.getJsonObject(i);
                if(substituteTeacherOfList.getSecondTeacherId().equals(substituteTeacher.getString("second_teacher_id"))){
                    substituteTeacher.getJsonArray("idsAndIdsGroups").add(new JsonObject()
                            .put("id", substituteTeacherOfList.getId()).put("idGroup", substituteTeacherOfList.getClassOrGroupId()));
                }
            }
        }
    }

    private static void addCompetencesParams(JsonObject compressedService, ServiceModel resultService) {
        compressedService.getJsonArray("competencesParams")
                .add(new JsonObject()
                        .put("id_groupe", resultService.getIdGroup())
                        .put("modalite", resultService.getModalite())
                        .put(COEFFICIENT, resultService.getCoefficient())
                        .put(EVALUABLE_STR, resultService.isEvaluable()));
    }

    private static JsonObject setCompetencesParams(JsonObject compressedService, ServiceModel resultService) {
        return compressedService.put("competencesParams", new JsonArray()
                .add(new JsonObject()
                        .put("id_groupe", resultService.getIdGroup())
                        .put("modalite", resultService.getModalite())
                        .put(COEFFICIENT, resultService.getCoefficient())
                        .put(EVALUABLE_STR, resultService.isEvaluable())));
    }

    private static void sortServices(List<ServiceModel> resultList) {
        Collections.sort(resultList, Comparator.comparing(ServiceModel::getIdTopic)
                .thenComparing(ServiceModel::getIdTeacher)
                .thenComparing(new Comparator<ServiceModel>() {
                    @Override
                    public int compare (ServiceModel s1, ServiceModel s2) {
                        int compare = 0;
                        if (s1.getCoteachers().size() < s2.getCoteachers().size()) compare = 1;
                        if (s1.getCoteachers().size() == s2.getCoteachers().size()) compare = 0;
                        if (s1.getCoteachers().size() > s2.getCoteachers().size()) compare = -1;
                        return compare;
                    }
                })
                .thenComparing(new Comparator<ServiceModel>() {
                    @Override
                    public int compare (ServiceModel s1, ServiceModel s2) {
                        int compareCoTeachers = 0;

                        List<String> idsSecondTeacherL1 = s1.getCoteachers().stream()
                                .map(MultiTeaching::getSecondTeacherId).sorted().collect(Collectors.toList());
                        List<String> idsSecondTeacherL2 = s2.getCoteachers().stream()
                                .map(MultiTeaching::getSecondTeacherId).sorted().collect(Collectors.toList());

                        for(String item1: idsSecondTeacherL1){
                            for(String item2 : idsSecondTeacherL2){
                                compareCoTeachers = item1.compareTo(item2);
                            }
                        }

                        return compareCoTeachers;
                    }
                })
                .thenComparing(new Comparator<ServiceModel>() {
                    @Override
                    public int compare (ServiceModel s1, ServiceModel s2) {
                        int compare = 0;
                        if (s1.getSubstituteTeachers().size() < s2.getSubstituteTeachers().size()) compare = 1;
                        if (s1.getSubstituteTeachers().size() == s2.getSubstituteTeachers().size()) compare = 0;
                        if (s1.getSubstituteTeachers().size() > s2.getSubstituteTeachers().size()) compare = -1;
                        return compare;
                    }
                })
                .thenComparing(new Comparator<ServiceModel>() {
                    @Override
                    public int compare (ServiceModel s1, ServiceModel s2) {
                        int compareSubTeachers = 0;

                        List<String> idsSecondTeacherL1 = s1.getSubstituteTeachers().stream()
                                .map(MultiTeaching::getSecondTeacherId).sorted().collect(Collectors.toList());
                        List<String> idsSecondTeacherL2 = s2.getSubstituteTeachers().stream()
                                .map(MultiTeaching::getSecondTeacherId).sorted().collect(Collectors.toList());

                        for(String item1: idsSecondTeacherL1){
                            for(String item2 : idsSecondTeacherL2){
                                compareSubTeachers = item1.compareTo(item2);
                            }
                        }

                        return compareSubTeachers;
                    }
                })
        );
    }

    private static JsonObject normalizeMatiere(JsonObject matiere) {
        JsonObject finalMatiere = new JsonObject();
        for(Map.Entry<String, Object> value : matiere.getMap().entrySet()) {
            switch (value.getKey()) {
                case "id":
                    finalMatiere.put("id_matiere", value.getValue());
                    break;
                case "idClasses":
                    finalMatiere.put("id_groupe", value.getValue());
                    break;
                case "idEnseignant":
                    finalMatiere.put("id_enseignant", value.getValue());
                    break;
                case "idEtablissement":
                    finalMatiere.put("id_etablissement", value.getValue());
                    break;
            }
        }
        return finalMatiere;
    }

    public static JsonObject getParams (HttpServerRequest request) {
        JsonObject oService = new JsonObject();

        if (request.params().contains("id_groupe")) {
            oService.put("id_groupe", request.getParam("id_groupe"));
        }
        if (request.params().contains("id_groups")) {
            oService.put("id_groups", Arrays.asList(request.getParam("id_groups").split(",")));
        }
        if (request.params().contains("id_matiere")) {
            oService.put("id_matiere", request.getParam("id_matiere"));
        }
        if (request.params().contains("id_enseignant")) {
            oService.put("id_enseignant", request.getParam("id_enseignant"));
        }
        return oService;
    }
}
