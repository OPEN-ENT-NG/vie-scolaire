package fr.openent.viescolaire.security;

import fr.openent.viescolaire.service.ClasseService;
import fr.openent.viescolaire.service.CommonCoursService;
import fr.openent.viescolaire.service.impl.DefaultClasseService;
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
    public static final Logger log = LoggerFactory.getLogger(Renders.class);
    @Override
    public void authorize(HttpServerRequest request, Binding binding, UserInfos user, Handler<Boolean> handler) {
        CommonCoursService commonService = new DefaultCommonCoursService();
        ClasseService classService = new DefaultClasseService();
        String idCourse = request.params().get("idCourse");
        if (idCourse == null) {
            handler.handle(false);
            return;
        }
        commonService.getCourse(idCourse)
                .onSuccess(course -> {
                    if(course.isEmpty()){
                        handler.handle(false);
                        return;
                    }
                    String structureId = course.getString("structureId");
                    handler.handle(user.getStructures().contains(structureId));
                })

                .onFailure(err -> {
                    log.error("error", err.getMessage());
                    handler.handle(false);
                });
    }
}
