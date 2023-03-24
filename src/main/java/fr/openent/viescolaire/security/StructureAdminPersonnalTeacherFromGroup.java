package fr.openent.viescolaire.security;

import fr.openent.viescolaire.core.constants.Field;
import fr.openent.viescolaire.helper.FutureHelper;
import fr.wseduc.webutils.http.Binding;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.http.filter.ResourcesProvider;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.neo4j.Neo4jResult;
import org.entcore.common.user.UserInfos;


public class StructureAdminPersonnalTeacherFromGroup implements ResourcesProvider {
    private final Logger log = LoggerFactory.getLogger(StructureAdminPersonnalTeacherFromGroup.class);
    @Override
    public void authorize(HttpServerRequest request, Binding binding, UserInfos user, Handler<Boolean> handler) {
        String groupId = request.params().get(Field.GROUP_ID_CAMEL);
        if (groupId == null || groupId.trim().isEmpty()) {
            handler.handle(false);
            return;
        }

        getStructureByGroup(groupId)
                .onSuccess(structure -> {
                    if (structure.isEmpty() && !structure.containsKey(Field.STRUCTUREID)){
                        handler.handle(false);
                        return;
                    }
                    String structureId = structure.getString(Field.STRUCTUREID, "");
                    handler.handle(structureId != null && user.getStructures().contains(structureId)
                                    && (WorkflowActionUtils.hasRight(user, WorkflowActionUtils.ADMIN_RIGHT) ||
                        Field.PERSONNEL.equals(user.getType()) || Field.TEACHER.equals(user.getType())));
                })
                .onFailure(err -> {
                    handler.handle(false);
                    log.error(String.format("[Viescolaire@%s::Authorize] : an error has occurred during the attempting" +
                            "to authorize, %s", this.getClass().getSimpleName(), err.getMessage()));
                });
    }


    public Future<JsonObject> getStructureByGroup(String groupId){
        Promise<JsonObject> promise = Promise.promise();
        String query = "MATCH(s:Structure)<-[:DEPENDS]-(g:Group) WHERE g.id = {groupId} RETURN DISTINCT s.id as structureId";
        JsonObject params = new JsonObject();

        params.put(Field.GROUP_ID_CAMEL, groupId);
        Neo4j.getInstance().execute(query ,params, Neo4jResult.validUniqueResultHandler(FutureHelper.handlerEitherPromise(promise)));
        return promise.future();
    }
}
