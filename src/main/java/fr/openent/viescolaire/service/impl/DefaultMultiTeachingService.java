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
import static org.entcore.common.sql.SqlResult.*;


public class DefaultMultiTeachingService extends SqlCrudService implements MultiTeachingService {

    private String multiTeaching_table = "multi_teaching";
    protected static final Logger log = LoggerFactory.getLogger(DefaultMultiTeachingService.class);
    public DefaultMultiTeachingService() {
        super(VSCO_SCHEMA, Viescolaire.SERVICES_TABLE);
    }

    @Override
    public void createMultiTeaching
            (String stuctureId, String mainTeacherId, JsonArray secondTeacherIds, String subjectId, JsonArray classOrGroupIds,
             String startDate, String endDate, String enteredEndDate, Boolean coTeaching, Handler<Either<String, JsonArray>> handler ,
             EventBus eb,boolean hasCompetences) {


        String query ;
        query = "INSERT INTO " + VSCO_SCHEMA + "." + multiTeaching_table +
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
        query += " RETURNING id";

//        query = "INSERT INTO viesco.multi_teaching(\n" +
//                "\t structure_id, main_teacher_id, second_teacher_id, subject_id, class_or_group_id, start_date, end_date, entered_end_date, is_coteaching)\n" +
//                "\tVALUES ( '92feb6f1-2016-4215-b53f-5337fbcba244', '386e9b4d-6671-42d7-8bf6-8b030e7d5e10', '2f5ef34b-dc45-4cfc-82ae-baad55a60706','2436365-1567681798037',\n" +
//                "\t\t\t'36e6eeb8-3a90-497b-8b6a-18ac9d0f3cb0',\n" +
//                "\t\t '2020-03-01', '2020-04-01', '2020-04-01',true) RETURNING second_teacher_id,main_teacher_id,subject_id;" ;

        /*if (hasCompetences) {
            Sql.getInstance().prepared(query, new JsonArray(), new Handler<Message<JsonObject>>() {
                @Override
                public void handle(Message<JsonObject> event) {
                    log.info(event.body());
                    if(event.body().getString("status").equals("ok")){
                        JsonArray idsToSend = event.body().getJsonArray("results");
                        sendIds(idsToSend,handler,eb);
                    }
                }
            });
        }else{*/
             Sql.getInstance().prepared(query, values, validResultHandler(handler));
        //}


    }

    private void sendIds(JsonArray idsToSend, Handler<Either<String, JsonArray>> requestHandler,EventBus eb) {
        JsonObject action = new JsonObject()
                .put("action", "homeworks.setShare")
                .put("ids", idsToSend);
        eb.send(Viescolaire.COMPETENCES_BUS_ADDRESS, action,   handlerToAsyncHandler(new Handler<Message<JsonObject>>(){
            @Override
            public void handle(Message<JsonObject> event) {
                log.info(event.body());
            }
        }));

    }

    @Override
    public void updateMultiTeaching(
            String id, String structureId, String mainTeacherId, JsonArray secondTeacherId, String subjectId, JsonArray classOrGroupId,
            String startDate, String endDate, String enteredEndDate, Boolean coTeaching, Handler<Either<String, JsonObject>> handler) {

        String query = "UPDATE INTO " + VSCO_SCHEMA + "."+ multiTeaching_table +
                " SET structure_id=?, main_teacher_id= ?,  second_teacher_id= ?, subject_id=?, class_or_group_id=?, " +
                " start_date=to_timestamp( ?, 'YYYY-MM-DD'), end_date=to_timestamp( ?, 'YYYY-MM-DD'), entered_end_date=to_timestamp( ?, 'YYYY-MM-DD'), " +
                " is_coteaching=? " +
                " WHERE id= ? "+
                " RETURNING id";

        JsonArray values = new JsonArray()
                .add(structureId)
                .add(mainTeacherId)
                .add(secondTeacherId)
                .add(subjectId)
                .add(classOrGroupId)
                .add(String.valueOf(startDate))
                .add(String.valueOf(endDate))
                .add(String.valueOf(enteredEndDate))
                .add(coTeaching)
                .add(id);

        Sql.getInstance().prepared(query, values, validUniqueResultHandler(handler));
    }

    @Override
    public void deleteMultiTeaching(JsonArray multiTeachingIds, Handler<Either<String, JsonObject>> handler) {
        String query = "DELETE FROM " + VSCO_SCHEMA + "."+ multiTeaching_table +" WHERE id IN " + Sql.listPrepared(multiTeachingIds.getList());

       // JsonArray values = new JsonArray();
       // for(Object o: )
        Sql.getInstance().prepared(query, multiTeachingIds, validRowsResultHandler(handler));

    }

    @Override
    public void getMultiTeaching (String structureId, Handler<Either<String, JsonArray>> handler) {

        String query = "SELECT * FROM "+ VSCO_SCHEMA + "." + multiTeaching_table + " WHERE structure_id = ? ;";

        JsonArray values = new JsonArray().add(structureId);
        Sql.getInstance().prepared(query, values, validResultHandler(handler));
    }

    @Override
    public void getSubTeachers(String userId, String idStructure, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new fr.wseduc.webutils.collections.JsonArray();

        query.append("SELECT DISTINCT main_teacher_id ")
                .append("FROM " + Viescolaire.VSCO_SCHEMA + ".multi_teaching ")
                .append("WHERE second_teacher_id = ? ")
                .append("AND structure_id = ? ")
                .append("AND start_date <= current_date ")
                .append("AND is_coteaching is FALSE ")
                .append("AND current_date <= end_date ");

        values.add(userId);
        values.add(idStructure);
        log.info(query);
        log.info(values);
        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }
    @Override
    public void getSubTeachersandCoTeachers(String userId, String idStructure, String subjectId,Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new fr.wseduc.webutils.collections.JsonArray();

        query.append("SELECT DISTINCT main_teacher_id")
                .append("FROM " + Viescolaire.VSCO_SCHEMA + ".multi_teaching ")
                .append("WHERE second_teacher_id = ? ")
                .append("AND structure_id = ? ")
                .append("AND subject_id = ? ")
                .append("AND start_date <= current_date ")
                .append("AND current_date <= end_date ");

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
                .append("FROM " + Viescolaire.VSCO_SCHEMA + ".multi_teaching ")
                .append("WHERE second_teacher_id = ? ")
                .append("AND structure_id = ? ")
                .append("AND start_date <= current_date ")
                .append("AND is_coteaching is TRUE ")
                .append("AND current_date <= end_date ");

        values.add(userId);
        values.add(idStructure);
        log.info(query);
        log.info(values);
        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }
}
