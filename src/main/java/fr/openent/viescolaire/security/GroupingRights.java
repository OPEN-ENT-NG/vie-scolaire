package fr.openent.viescolaire.security;

import fr.openent.Viescolaire;
import fr.openent.viescolaire.core.constants.Field;
import fr.openent.viescolaire.helper.PromiseHelper;
import fr.wseduc.webutils.http.Binding;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.http.filter.ResourcesProvider;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.entcore.common.user.UserInfos;

public class GroupingRights implements ResourcesProvider {
    private static final Logger log = LoggerFactory.getLogger(GroupingRights.class);

    @Override
    public void authorize(final HttpServerRequest resourceRequest, Binding binding, final UserInfos user, final Handler<Boolean> handler) {
        String id = resourceRequest.getParam(Field.ID);
        checkGroupingsRights(user, id)
                .onSuccess(res -> handler.handle(res && WorkflowActionUtils.hasRight(user, WorkflowActionUtils.ADMIN_RIGHT)))
                .onFailure(err -> {
                    handler.handle(false);
                });
    }

    private Future<Boolean> checkGroupingsRights(UserInfos user, String groupingId) {
        Promise<Boolean> promise = Promise.promise();
        String query = "SELECT structure_id FROM " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.GROUPING_TABLE + " WHERE id = ? ;";
        JsonArray values = new JsonArray();
        values.add(groupingId);
        Sql.getInstance().prepared(query, values, SqlResult.validUniqueResultHandler(res -> {
            if(res.isRight() && !res.right().getValue().isEmpty()){
                promise.complete(user.getStructures().contains(res.right().getValue().getString(Field.STRUCTURE_ID)));
            }
            else {
                String messageToFormat = "[Viescolaire@%s::isUserAllowToManageGroupings] Error while checking rights : %s";
                PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), new Exception(res.left().getValue()), promise);
            }
        }));
        return promise.future();
    }
}
