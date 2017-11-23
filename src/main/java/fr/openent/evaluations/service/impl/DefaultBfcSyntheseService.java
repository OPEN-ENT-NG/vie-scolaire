package fr.openent.evaluations.service.impl;

import fr.openent.Viescolaire;
import fr.openent.evaluations.service.BfcSyntheseService;
import fr.openent.evaluations.service.UtilsService;
import fr.openent.viescolaire.service.ClasseService;
import fr.openent.viescolaire.service.impl.DefaultClasseService;
import fr.wseduc.webutils.Either;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;



/**
 * Created by agnes.lapeyronnie on 03/11/2017.
 */
public class DefaultBfcSyntheseService extends SqlCrudService implements BfcSyntheseService {

    private ClasseService classeService;
    private UtilsService utilsService ;
    private static final Logger log = LoggerFactory.getLogger(DefaultBfcSyntheseService.class);

    public DefaultBfcSyntheseService(String schema, String table) {
        super(schema,table);
        classeService = new DefaultClasseService();
        utilsService = new DefaultUtilsService();
    }

    @Override
    public void createBfcSynthese(JsonObject synthese, UserInfos user, Handler<Either<String, JsonObject>> handler) {
        super.create(synthese,user,handler);
    }

    @Override
    public void updateBfcSynthese(String id, JsonObject synthese, Handler<Either<String, JsonObject>> handler) {
        super.update( id, synthese, handler);
    }

    @Override
    public void deleteBfcSynthese(String id, Handler<Either<String, JsonObject>> handler) {
        super.delete( id, handler);
    }

    @Override
    public void getBfcSyntheseByEleve(String idEleve,Integer idCycle, Handler<Either<String, JsonObject>> handler) {
        StringBuilder query = new StringBuilder()
                .append("SELECT * FROM "+ Viescolaire.EVAL_SCHEMA +".bfc_synthese WHERE id_eleve = ? AND id_cycle = ? ;");

        Sql.getInstance().prepared(query.toString(),new JsonArray().addString(idEleve).addNumber(idCycle), SqlResult.validUniqueResultHandler(handler));
    }


    // A partir d'un idEleve retourne idCycle dans lequel il est.(avec les requêtes getClasseByEleve de ClasseService et getCycle de UtilsService)
    @Override
    public void getIdCycleWithIdEleve(String idEleve,final Handler<Either<String,Integer>> handler) {
        classeService.getClasseByEleve(idEleve, new Handler<Either<String, JsonObject>>() {
            @Override
            public void handle(Either<String, JsonObject> classeObject) {
                if (classeObject.isRight()) {
                    utilsService.getCycle(classeObject.right().getValue().getObject("c").getObject("data").getString("id"), new Handler<Either<String, JsonObject>>() {
                        @Override
                        public void handle(Either<String, JsonObject> idCycleObject) {
                            if (idCycleObject.isRight()) {
                                Integer idCycle = idCycleObject.right().getValue().getInteger("id_cycle");
                                handler.handle(new Either.Right<String,Integer>(idCycle));
                            } else {
                                log.error("idCycle not found" + idCycleObject.left().getValue());
                                handler.handle(new Either.Left<String,Integer>("idCycle not found : " + idCycleObject.left().getValue()));
                            }
                        }
                    });
                } else {
                    log.error("Class not found" + classeObject.left().getValue());
                    handler.handle(new Either.Left<String,Integer>("idClass not found : "+classeObject.left().getValue()));
                }
            }
        });
    }

}