package org.cgi.evaluations.service.impl;

import fr.wseduc.webutils.Either;
import org.cgi.evaluations.service.IEvalEnseignementService;
import org.entcore.common.service.impl.SqlCrudService;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;

/**
 * Created by ledunoiss on 05/08/2016.
 */
public class CEvalEnseignementServiceImpl extends SqlCrudService implements IEvalEnseignementService {
    public CEvalEnseignementServiceImpl(String table) {
        super(table);
    }

    @Override
    public void getEnseignements(Handler<Either<String, JsonArray>> handler) {
        super.list(handler);
    }
}
