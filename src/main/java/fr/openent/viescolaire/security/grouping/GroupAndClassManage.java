package fr.openent.viescolaire.security.grouping;

import fr.openent.Viescolaire;
import fr.openent.viescolaire.core.constants.Field;
import fr.openent.viescolaire.helper.PromiseHelper;
import fr.openent.viescolaire.security.WorkflowActionUtils;
import fr.openent.viescolaire.service.impl.DefaultClasseService;
import fr.wseduc.webutils.http.Binding;
import fr.wseduc.webutils.request.RequestUtils;
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

public class GroupAndClassManage implements ResourcesProvider {
    private static final Logger log = LoggerFactory.getLogger(GroupAndClassManage.class);

    @Override
    public void authorize(final HttpServerRequest resourceRequest, Binding binding, final UserInfos user, final Handler<Boolean> handler) {
        RequestUtils.bodyToJson(resourceRequest, body -> {
            String studentDivisionId = body.getString(Field.STUDENT_DIVISION_ID);
            if (studentDivisionId == null) {
                handler.handle(false);
                return;
            }
            String groupingId = resourceRequest.getParam(Field.ID);
            this.checkGroupingsRights(user, groupingId)
                    .compose(event -> {
                        if (Boolean.TRUE.equals(event)) {
                            return new DefaultClasseService().getEtabClasses(studentDivisionId);
                        } else {
                            return Future.succeededFuture(new JsonArray());
                        }
                    }).onSuccess(event -> handler.handle(!event.isEmpty() //if checkGroupingsRights is false also event is empty
                            && user.getStructures().contains(event.getJsonObject(0).getString(Field.IDSTRUCTURE))
                            && WorkflowActionUtils.hasRight(user, WorkflowActionUtils.ADMIN_RIGHT)))
                    .onFailure(error -> {
                        String message = String.format("[Viescolaire@%s::authorize] Error while execute ResourcesProvider : %s",
                                GroupAndClassManage.class, error.getMessage());
                        log.error(message);
                        handler.handle(false);
                    });
        });
    }

    /**
     * Check if the user have the rights to manage a specific grouping.
     *
     * @param user       User data
     * @param groupingId Identifier of the grouping
     * @return Return a future with the result of the check
     */
    private Future<Boolean> checkGroupingsRights(UserInfos user, String groupingId) {
        Promise<Boolean> promise = Promise.promise();
        String query = "SELECT structure_id FROM " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.GROUPING_TABLE + " WHERE id = ? ;";
        JsonArray values = new JsonArray();
        values.add(groupingId);
        Sql.getInstance().prepared(query, values, SqlResult.validUniqueResultHandler(res -> {
            if (res.isLeft()) {
                String messageToFormat = "[Viescolaire@%s::checkGroupingsRights] Error while checking rights : %s";
                PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), new Exception(res.left().getValue()), promise);
            } else if (res.right().getValue().isEmpty()) {
                promise.complete(false);
            } else {
                promise.complete(user.getStructures().contains(res.right().getValue().getString(Field.STRUCTURE_ID)));
            }

        }));
        return promise.future();
    }
}
