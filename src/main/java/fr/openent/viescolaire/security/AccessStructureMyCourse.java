package fr.openent.viescolaire.security;

import fr.openent.viescolaire.core.constants.Field;
import fr.openent.viescolaire.service.CommonCoursService;
import fr.openent.viescolaire.service.impl.DefaultCommonCoursService;
import fr.wseduc.webutils.http.Binding;
import fr.wseduc.webutils.http.Renders;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.http.filter.ResourcesProvider;
import org.entcore.common.user.UserInfos;

public class AccessStructureMyCourse implements ResourcesProvider {
    public static final Logger log = LoggerFactory.getLogger(AccessStructureMyCourse.class);
    private final CommonCoursService service;
    public AccessStructureMyCourse(){this.service = new DefaultCommonCoursService();}


    @Override
    public void authorize(HttpServerRequest request, Binding binding, UserInfos user, Handler<Boolean> handler) {
        String idCourse = request.getParam(Field.IDCOURSE);
        this.service.getCourse(idCourse)
                .onSuccess(course -> {
                    if (course.isEmpty()) {
                        handler.handle(false);
                        return;
                    }
                    String structureId = course.getString(Field.STRUCTUREID);
                    handler.handle(structureId != null && user.getStructures().contains(structureId));
                })
                .onFailure(err -> {
                    log.error(String.format("[Viescolaire@%s::authorize] Failed to retrieve structure from course %s", this.getClass().getSimpleName(), err.getMessage()));
                    handler.handle(false);
                });
    }
}