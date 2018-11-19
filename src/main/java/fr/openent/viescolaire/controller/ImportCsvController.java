package fr.openent.viescolaire.controller;

import fr.openent.viescolaire.service.ImportCsvService;
import fr.openent.viescolaire.service.impl.DefaultImportCsvService;
import fr.wseduc.rs.Post;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.storage.Storage;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;

import java.io.IOException;

public class ImportCsvController extends ControllerHelper {
    private ImportCsvService importCsvService;
    private Storage storage;

    public ImportCsvController(Storage storage) {
        importCsvService = new DefaultImportCsvService();
        this.storage = storage;
    }

    @Post("/import/evenements")
    @SecuredAction("import.retards.and.absences")
    public void importRetadsAndAbsences(final HttpServerRequest request) throws IOException {
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if (user == null) {
                    unauthorized(request);
                    return;
                }
                String idEtablissement = request.params().get("idEtablissement");
                Long idPeriode = Long.valueOf(request.params().get("idPeriode"));
                importCsvService.importAbsencesAndRetard(idEtablissement, idPeriode, storage, request,
                        new Handler<Either<String, JsonObject>>() {
                    @Override
                    public void handle(Either<String, JsonObject> event) {
                        if (event.isLeft()) {
                            badRequest(request, event.left().getValue());
                        }
                        else {
                            renderJson(request, event.right().getValue());
                        }
                    }
                });
            }
        });
    }
}
