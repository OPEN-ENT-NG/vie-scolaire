package fr.openent.viescolaire.security;

import fr.openent.viescolaire.core.constants.Field;
import fr.openent.viescolaire.db.DBService;
import fr.wseduc.webutils.http.Binding;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import org.entcore.common.http.filter.ResourcesProvider;
import org.entcore.common.neo4j.Neo4jResult;
import org.entcore.common.user.UserInfos;


public class StructureAdminPersonnalTeacherFromGroup extends DBService implements ResourcesProvider {
    @Override
    public void authorize(HttpServerRequest request, Binding binding, UserInfos user, Handler<Boolean> handler) {
        String idGroup = request.params().get(Field.GROUP_ID_CAMEL);
        if (idGroup == null || idGroup.trim().isEmpty()) {
            handler.handle(false);
            return;
        }
        String query = "MATCH(s:Structure)<-[:DEPENDS]-(g:Group) WHERE g.id = {idGroup} RETURN DISTINCT s.id as structureId";
        JsonObject params = new JsonObject();

        params.put(Field.GROUP_ID_CAMEL, idGroup);

        neo4j.execute(query ,params, Neo4jResult.validResultHandler(either -> {
            if (either.right().getValue() != null && !either.right().getValue().isEmpty()) {
                String structureId = either.right().getValue().getJsonObject(0).getString(Field.STRUCTUREID);
                handler.handle(structureId != null && user.getStructures().contains(structureId) && (WorkflowActionUtils.hasRight(user, WorkflowActionUtils.ADMIN_RIGHT) ||
                        Field.PERSONNEL.equals(user.getType()) || Field.TEACHER.equals(user.getType())));
                return;
            }
            handler.handle(false);
        }));
    }
}
