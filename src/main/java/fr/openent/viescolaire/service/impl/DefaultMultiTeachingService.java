package fr.openent.viescolaire.service.impl;

import fr.openent.Viescolaire;
import fr.openent.viescolaire.service.MultiTeachingService;
import fr.wseduc.webutils.Either;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;

import static fr.openent.Viescolaire.VSCO_SCHEMA;
import static fr.wseduc.webutils.Utils.handlerToAsyncHandler;
import static java.util.Objects.isNull;
import static org.entcore.common.http.response.DefaultResponseHandler.defaultResponseHandler;
import static org.entcore.common.neo4j.Neo4jResult.validUniqueResultHandler;
import static org.entcore.common.sql.SqlResult.*;

import java.util.List;



public class DefaultMultiTeachingService extends SqlCrudService implements MultiTeachingService {

    private final String multiTeaching_table = "multi_teaching";
    private final String VSCO_PERIODE_TABLE = "periode";
    protected static final Logger log = LoggerFactory.getLogger(DefaultMultiTeachingService.class);
    public DefaultMultiTeachingService() {
        super(VSCO_SCHEMA, Viescolaire.SERVICES_TABLE);
    }

    @Override
    public void createMultiTeaching (String stuctureId, String mainTeacherId, JsonArray secondTeacherIds,
                                     String subjectId, JsonArray classOrGroupIds, String startDate, String endDate,
                                     String enteredEndDate, Boolean coTeaching,
                                     Handler<Either<String, JsonArray>> handler, EventBus eb, boolean hasCompetences) {


        String query ;
        query = "WITH insert AS ( INSERT INTO " + VSCO_SCHEMA + "." + multiTeaching_table +
                " (structure_id, main_teacher_id,  second_teacher_id, subject_id, class_or_group_id, is_coteaching, start_date, end_date, entered_end_date)" +
                " VALUES ";

        JsonArray values = new JsonArray();

        for (int i = 0; i < secondTeacherIds.size(); i++) {
            String secondTeacherId = secondTeacherIds.getString(i);
            for (int j = 0; j < classOrGroupIds.size(); j++) {
                String classOrGroupId = classOrGroupIds.getString(j);
                query += "( ?, ?, ?, ?, ?, ?,";

                values.add(stuctureId)
                        .add(mainTeacherId)
                        .add(secondTeacherId)
                        .add(subjectId)
                        .add(classOrGroupId)
                        .add(coTeaching);

                if(startDate != null && endDate != null && enteredEndDate != null) {
                    query += " to_timestamp( ?, 'YYYY-MM-DD'), to_timestamp( ?, 'YYYY-MM-DD'), to_timestamp( ?, 'YYYY-MM-DD') ),";
                    values.add(startDate)
                            .add(endDate)
                            .add(enteredEndDate);
                }
                else {
                    query += " NULL, NULL, NULL ),";
                }

            }
        }

        query = query.substring(0, query.length() - 1);//remove useless ','

        query += "RETURNING * ) SELECT second_teacher_id FROM insert " +
                "UNION SELECT DISTINCT second_teacher_id FROM " + VSCO_SCHEMA + "." + multiTeaching_table +
                " WHERE structure_id = ? AND main_teacher_id = ? AND subject_id = ? " +
                "AND class_or_group_id IN "+ Sql.listPrepared(classOrGroupIds.getList());

        values.add(stuctureId).add(mainTeacherId).add(subjectId);

        for(Object o : classOrGroupIds){
            values.add(o);
        }

        for(int i = 0; i < classOrGroupIds.size(); i++){
            String classId = classOrGroupIds.getString(i);
            JsonObject classAction = new JsonObject()
                    .put("action", "manual-add-users")
                    .put("classId", classId)
                    .put("userIds", secondTeacherIds);
            eb.send("entcore.feeder", classAction, handlerToAsyncHandler(validUniqueResultHandler(new Handler<Either<String, JsonObject>>() {
                @Override
                public void handle(Either<String, JsonObject> event) {

                }
            })));

            for(int j = 0; j < secondTeacherIds.size();j++){
                //manual-add-user-group
                JsonObject groupAction = new JsonObject()
                        .put("action", "manual-add-user-group")
                        .put("groupId", classId)
                        .put("userId", secondTeacherIds.getString(j));
                eb.send("entcore.feeder", groupAction, handlerToAsyncHandler(validUniqueResultHandler(new Handler<Either<String, JsonObject>>() {
                    @Override
                    public void handle(Either<String, JsonObject> event) {

                    }
                })));
            }
        }
        handleCreationSqlResponse(stuctureId, mainTeacherId, secondTeacherIds, subjectId, classOrGroupIds, handler, eb, hasCompetences, query, values);
    }

    private void handleCreationSqlResponse(String stuctureId, String mainTeacherId, JsonArray secondTeacherIds,
                                           String subjectId, JsonArray classOrGroupIds,
                                           Handler<Either<String, JsonArray>> handler, EventBus eb,
                                           boolean hasCompetences, String query, JsonArray values) {
        if (hasCompetences) {
            Sql.getInstance().prepared(query, values,  (res) -> {
                if(res.body().getString("status").equals("ok")){
                    JsonArray idsToSend = new JsonArray();
                    secondTeacherIds.clear().add(mainTeacherId);
                    for(Object teacher : res.body().getJsonArray("results")){
                        JsonArray teacherArray = (JsonArray)teacher;
                        secondTeacherIds.add(teacherArray.getString(0));
                    }
                    for(int i = 0;i < secondTeacherIds.size();i++){

                        String firstSecondId = secondTeacherIds.getString(i);
                        for(int j = i ; j<secondTeacherIds.size();j++){
                            String secondSecondId =  secondTeacherIds.getString(j);
                            if(!secondSecondId.equals(firstSecondId))
                                for(int k = 0 ; k < classOrGroupIds.size();k++){
                                    JsonArray ids = new JsonArray();
                                    String groupId =  classOrGroupIds.getString(k);

                                    ids.add(secondSecondId).add(firstSecondId).add(subjectId).add(groupId).add(stuctureId);
                                    idsToSend.add(ids);
                                }
                        }
                    }
                    sendIdsToShare(idsToSend,handler,eb);

                }else{
                    handler.handle(new Either.Left<>("createMultiTeaching failed"));
                }
            });
        }else{
            Sql.getInstance().prepared(query, values, validResultHandler(handler));
        }
    }

    private void sendIdsToShare(JsonArray idsToSend, Handler<Either<String, JsonArray>> requestHandler, EventBus eb) {
        JsonObject action = new JsonObject()
                .put("action", "homeworks.setShare")
                .put("ids", idsToSend);
        eb.send(Viescolaire.COMPETENCES_BUS_ADDRESS, action,   handlerToAsyncHandler(new Handler<Message<JsonObject>>(){
            @Override
            public void handle(Message<JsonObject> event) {
                if(event.body().getString("status").equals("ok")){
                    requestHandler.handle(new Either.Right<>(new JsonArray().add(event.body().getJsonArray("results"))));
                }
            }
        }));
    }
    private void sendIdsToDelete(JsonArray idsToSend, Handler<Either<String, JsonObject>> requestHandler, EventBus eb) {
        JsonObject action = new JsonObject()
                .put("action", "homeworks.removeShare")
                .put("ids", idsToSend);
        eb.send(Viescolaire.COMPETENCES_BUS_ADDRESS, action,   handlerToAsyncHandler(new Handler<Message<JsonObject>>(){
            @Override
            public void handle(Message<JsonObject> event) {
                if(event.body().getString("status").equals("ok")){
                    requestHandler.handle(new Either.Right<>(new JsonObject().put("results",event.body().getJsonArray("results"))));
                }
            }
        }));
    }



    @Override
    public void deleteMultiTeaching(JsonArray multiTeachingIds, boolean hasCompetences,EventBus eb,
                                    Handler<Either<String, JsonObject>> handler) {
        String deleteQuery = "UPDATE " + VSCO_SCHEMA + "."+ multiTeaching_table +
                " SET start_date=NULL, end_date=NULL, entered_end_date=NULL, is_coteaching=NULL " +
                "WHERE id IN " + Sql.listPrepared(multiTeachingIds.getList()) ;
        JsonArray oldMultiTeachingIds = new JsonArray().addAll(multiTeachingIds);
        String selectQuery = "SELECT second_teacher_id, main_teacher_id,subject_id,class_or_group_id " +
                "FROM " + VSCO_SCHEMA + "."+ multiTeaching_table +" WHERE id IN "
                + Sql.listPrepared(multiTeachingIds.getList())
                + "UNION "
                + "SELECT mtt.second_teacher_id, mtt.main_teacher_id,mtt.subject_id,mtt.class_or_group_id "
                +"FROM " + Viescolaire.VSCO_SCHEMA + "." + multiTeaching_table +" mt "
                +"INNER JOIN " + Viescolaire.VSCO_SCHEMA + "." + multiTeaching_table +" mtt "
                +"ON  mtt.main_teacher_id = mt.main_teacher_id AND mt.subject_id = mtt.subject_id " +
                " AND mt.second_teacher_id != mtt.second_teacher_id AND  mt.class_or_group_id = mtt.class_or_group_id "
                +  "WHERE mt.id IN "
                + Sql.listPrepared(multiTeachingIds.getList());

        if (hasCompetences) {
            Sql.getInstance().prepared(selectQuery,multiTeachingIds.addAll(multiTeachingIds), new Handler<Message<JsonObject>>() {
                @Override
                public void handle(Message<JsonObject> event) {
                    if(event.body().getString("status").equals("ok") && event.body().getJsonArray("results").size() > 0){
                        JsonArray idsToSend = event.body().getJsonArray("results");

                        Sql.getInstance().prepared(deleteQuery, oldMultiTeachingIds, new Handler<Message<JsonObject>>() {
                            @Override
                            public void handle(Message<JsonObject> deleteEvent) {
                                sendIdsToDelete(idsToSend,handler,eb);
                            }
                        });

                    }
                }
            });
        }else{
            Sql.getInstance().prepared(deleteQuery, multiTeachingIds, validRowsResultHandler(handler));
        }
    }

    @Override
    public void updateMultiteaching (JsonArray ids_multiTeachingToUpdate, String second_teacher, String startDate,
                                     String endDate, String enteredEndDate, Boolean isVisible,
                                     EventBus eb, boolean hasCompetences, Handler<Either<String, JsonArray>> handler) {
        JsonArray values = new JsonArray();
        String query = "UPDATE " + VSCO_SCHEMA + "." + multiTeaching_table +
                " SET second_teacher_id = ? , start_date = to_timestamp( ?, 'YYYY-MM-DD'), end_date = to_timestamp( ?, 'YYYY-MM-DD'), entered_end_date = to_timestamp( ?, 'YYYY-MM-DD'), is_visible = ?" +
                " WHERE id IN " + Sql.listPrepared(ids_multiTeachingToUpdate.getList()) + "  RETURNING second_teacher_id, main_teacher_id,subject_id,class_or_group_id;";
        values.add(second_teacher).add(startDate).add(endDate).add(enteredEndDate).add(isVisible);

        for(Object o : ids_multiTeachingToUpdate){
            values.add(o);
        }

        if (hasCompetences) {
            Sql.getInstance().prepared(query, values, new Handler<Message<JsonObject>>() {
                @Override
                public void handle(Message<JsonObject> event) {
                    if(event.body().getString("status").equals("ok")){
                        JsonArray idsToSend = event.body().getJsonArray("results");
                        sendIdsToShare(idsToSend,handler,eb);
                    }
                }
            });
        }else {
            Sql.getInstance().prepared(query, values, validResultHandler(handler));
        }
    }

    @Override
    public void updateMultiTeachingVisibility(JsonArray groupsId, String structureId, String mainTeacherId,
                                              String secondTeacherId, String subjectId,
                                              Boolean isVisible, Handler<Either<String, JsonArray>> handler) {
        JsonArray values = new JsonArray();
        String query = "UPDATE " + VSCO_SCHEMA + "." + multiTeaching_table +
                " SET is_visible = ? WHERE structure_id = ? AND subject_id = ? AND main_teacher_id = ? AND " +
                "second_teacher_id = ? AND class_or_group_id IN " + Sql.listPrepared(groupsId.getList());
        values.add(isVisible).add(structureId).add(subjectId).add(mainTeacherId).add(secondTeacherId);

        for(Object o : groupsId){
            values.add(o);
        }

        Sql.getInstance().prepared(query, values, validResultHandler(handler));
    }


    @Override
    public void getMultiTeaching (String structureId, Handler<Either<String, JsonArray>> handler) {
        String query = "SELECT * FROM "+ VSCO_SCHEMA + "." + multiTeaching_table + " " +
                "WHERE structure_id = ? AND is_coteaching IS NOT NULL ;";

        JsonArray values = new JsonArray().add(structureId);
        Sql.getInstance().prepared(query, values, validResultHandler(handler));
    }

    /**
     * @param structureId struture id
     * @param groupIds    classes or/and groups ids
     * @param periodId    periode
     * @param onlyVisible visible
     * @param handler     response visible multiteachers on periode on classIds and on etablissement
     */
    @Override
    public void getMultiTeachers(String structureId, JsonArray groupIds, String periodId, Boolean onlyVisible,
                                 Handler<Either<String, JsonArray>> handler) {
        JsonArray values = new JsonArray().add(structureId);
        for (int i= 0; i < groupIds.size(); i++) {
            values.add(groupIds.getString(i));
        }
        values.add(onlyVisible);

        StringBuffer query = new StringBuffer();
        query.append("SELECT * FROM " + VSCO_SCHEMA + "." + multiTeaching_table )
                .append(" JOIN " + VSCO_SCHEMA + "." + VSCO_PERIODE_TABLE + " on class_or_group_id = id_classe ")
                .append("WHERE structure_id = ? AND class_or_group_id IN "+ Sql.listPrepared(groupIds))
                .append("AND is_visible = ? ");

        if(periodId != null){
            query.append("AND id_type = ? AND (is_coteaching = TRUE OR (is_coteaching = FALSE AND (")
                    .append("(timestamp_dt <= start_date AND start_date <= timestamp_fn) OR ")
                    .append("(timestamp_dt <= end_date AND end_date <= timestamp_fn) OR ")
                    .append("(start_date <= timestamp_dt AND timestamp_dt <= end_date) OR ")
                    .append("(start_date <= timestamp_fn AND timestamp_fn <= end_date) )))");

            values.add(periodId);
        }

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }

    @Override
    public void getSubTeachers(String userId, String idStructure, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new fr.wseduc.webutils.collections.JsonArray();

        query.append("SELECT DISTINCT main_teacher_id ")
                .append("FROM " + Viescolaire.VSCO_SCHEMA + "." + multiTeaching_table)
                .append(" WHERE second_teacher_id = ? ")
                .append("AND structure_id = ? ")
                .append("AND start_date <= current_date ")
                .append("AND is_coteaching is FALSE ")
                .append("AND current_date <= entered_end_date ");

        values.add(userId);
        values.add(idStructure);
        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }

    @Override
    public void getSubTeachersandCoTeachers(String userId, String idStructure, String subjectId,
                                            String groupId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new fr.wseduc.webutils.collections.JsonArray();
        query.append("SELECT DISTINCT main_teacher_id  as teacher_id ")
                .append("FROM " + Viescolaire.VSCO_SCHEMA + "." + multiTeaching_table)
                .append(" WHERE second_teacher_id = ? ")
                .append("AND structure_id = ? ")
                .append("AND subject_id = ? ")
                .append("AND class_or_group_id = ? ")
                .append("AND (start_date <= current_date OR start_date IS NULL) ")
                .append("AND (current_date <= entered_end_date OR entered_end_date IS NULL) ")
                .append("AND is_coteaching IS NOT NULL ")

                .append("UNION ")

                .append("SELECT DISTINCT second_teacher_id  as teacher_id ")
                .append("FROM " + Viescolaire.VSCO_SCHEMA + "." + multiTeaching_table)
                .append(" WHERE main_teacher_id = ? ")
                .append("AND structure_id = ? ")
                .append("AND subject_id = ? ")
                .append("AND  class_or_group_id = ? ")
                .append("AND (start_date <= current_date OR start_date IS NULL) ")
                .append("AND (current_date <= entered_end_date OR entered_end_date IS NULL) ")
                .append("AND is_coteaching IS NOT NULL ")

                .append("UNION ")

                .append("SELECT DISTINCT  mtt.second_teacher_id  as teacher_id ")
                .append("FROM " + Viescolaire.VSCO_SCHEMA + "." + multiTeaching_table + " mt ")
                .append("INNER JOIN " + Viescolaire.VSCO_SCHEMA + "." + multiTeaching_table + " mtt ")
                .append("ON  mtt.main_teacher_id = mt.main_teacher_id AND mt.subject_id = mtt.subject_id ")
                .append("WHERE mtt.second_teacher_id != ? ")
                .append("AND mt.second_teacher_id = ? ")
                .append("AND mt.is_coteaching IS NOT NULL ")
                .append("AND  mtt.structure_id = ? ")
                .append("AND  mtt.subject_id = ? ")
                .append("AND  mtt.class_or_group_id = ? ")
                .append("AND (mtt.start_date <= current_date OR mtt.start_date IS NULL) ")
                .append("AND (current_date <= mtt.entered_end_date OR mtt.entered_end_date IS NULL) ")
                .append("AND mtt.is_coteaching IS NOT NULL ");

        values.add(userId);
        values.add(idStructure);
        values.add(subjectId);
        values.add(groupId);

        values.add(userId);
        values.add(idStructure);
        values.add(subjectId);
        values.add(groupId);

        values.add(userId);
        values.add(userId);
        values.add(idStructure);
        values.add(subjectId);
        values.add(groupId);

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }

    public void getOtherSubTeachersandCoTeachers(String userId, String idStructure, String subjectId, Handler<Either<String, JsonArray>> handler) {
        //TODO PASSER PAR LE MAIN TEACHER
        StringBuilder query = new StringBuilder();
        JsonArray values = new fr.wseduc.webutils.collections.JsonArray();

        query.append("SELECT DISTINCT main_teacher_id  as teacher_id ")
                .append("FROM " + Viescolaire.VSCO_SCHEMA + "." + multiTeaching_table)
                .append(" WHERE second_teacher_id = ? ")
                .append("AND structure_id = ? ")
                .append("AND subject_id = ? ")
                .append("AND start_date <= current_date ")
                .append("AND current_date <= entered_end_date ")
                .append("AND is_coteaching IS NOT NULL ")
                .append("UNION ")
                .append("SELECT DISTINCT second_teacher_id  as teacher_id ")
                .append("FROM " + Viescolaire.VSCO_SCHEMA + "." + multiTeaching_table)
                .append(" WHERE main_teacher_id = ? ")
                .append("AND structure_id = ? ")
                .append("AND subject_id = ? ")
                .append("AND start_date <= current_date ")
                .append("AND current_date <= entered_end_date ")
                .append("AND is_coteaching IS NOT NULL ");

        values.add(userId);
        values.add(idStructure);
        values.add(subjectId);
        values.add(userId);
        values.add(idStructure);
        values.add(subjectId);
        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }

    @Override
    public void getCoTeachers(String userId, String idStructure, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new fr.wseduc.webutils.collections.JsonArray();

        query.append("SELECT DISTINCT main_teacher_id ")
                .append("FROM " + Viescolaire.VSCO_SCHEMA + "." + multiTeaching_table)
                .append(" WHERE second_teacher_id = ? ")
                .append("AND structure_id = ? ")
                .append("AND is_coteaching is TRUE ");

        values.add(userId);
        values.add(idStructure);
        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }

    @Override
    public void getIdGroupsMutliTeaching(String userId, String idStructure, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new fr.wseduc.webutils.collections.JsonArray();

        query.append("SELECT class_or_group_id  as group_id ")
                .append("FROM " + Viescolaire.VSCO_SCHEMA + "." + multiTeaching_table)
                .append(" WHERE second_teacher_id = ? ")
                .append("AND structure_id = ? ")
                .append("AND start_date <= current_date ")
                .append("AND current_date <= entered_end_date ")
                .append("AND is_coteaching IS NOT NULL ");

        values.add(userId);
        values.add(idStructure);
        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }
}
