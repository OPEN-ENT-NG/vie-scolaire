/*
 * Copyright (c) Région Hauts-de-France, Département de la Seine-et-Marne, Région Nouvelle Aquitaine, Mairie de Paris, CGI, 2016.
 * This file is part of OPEN ENT NG. OPEN ENT NG is a versatile ENT Project based on the JVM and ENT Core Project.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation (version 3 of the License).
 * For the sake of explanation, any module that communicate over native
 * Web protocols, such as HTTP, with OPEN ENT NG is outside the scope of this
 * license and could be license under its own terms. This is merely considered
 * normal use of OPEN ENT NG, and does not fall under the heading of "covered work".
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package fr.openent;

import fr.openent.viescolaire.controller.*;
import fr.openent.viescolaire.db.DB;
import fr.openent.viescolaire.service.ServiceFactory;
import fr.openent.viescolaire.service.impl.VieScolaireRepositoryEvents;
import fr.openent.viescolaire.worker.*;
import fr.wseduc.mongodb.MongoDb;
import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import org.entcore.common.http.BaseServer;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.sql.Sql;
import org.entcore.common.storage.Storage;
import org.entcore.common.storage.StorageFactory;

public class Viescolaire extends BaseServer {

    /**
     * Déclaration des schémas
     */
    public final static String VSCO_SCHEMA = "viesco";
    public final static String EVAL_SCHEMA = "notes";
    public final static String ABSC_SCHEMA = "presences";
    public final static String MEMENTO_SCHEMA = "memento";

    /**
     * Déclaration des tables
     */
    public static final String VSCO_COURS_TABLE = "cours";
    public static final String VSCO_ELEVE_TABLE = "eleve";
    public static final String VSCO_CLASSE_TABLE = "classe";
    public static final String VSCO_PERIODE_TABLE = "periode";
    public static final String VSCO_MATIERE_TABLE = "matiere";
    public static final String VSCO_SOUSMATIERE_TABLE = "sousmatiere";
    public static final String VSCO_SETTING_PERIOD = "setting_period";
    public static final String VSCO_MATIERE_LIBELLE_TABLE = "subject_libelle";
    public static final String VSCO_MODEL_MATIERE_LIBELLE_TABLE = "model_subject_libelle";
    public static final String VSCO_MULTI_TEACHING_TABLE = "multi_teaching";
    public static final String VSCO_TIME_SLOTS = "time_slots";
    public static final String VSCO_REL_TIME_SLOT_CLASS = "rel_time_slot_class";
    public static final String VSCO_SLOTS = "slots";
    public static final String SERVICES_TABLE = "services";
    public static final String GROUPING_TABLE = "grouping";
    public static final String REL_GROUPING_CLASS_TABLE = "rel_grouping_class";

    /**
     * Déclaration des router préfixs
     */
    public final static String VSCO_PATHPREFIX = "/viescolaire";

    public static final Integer CLASSE_TYPE = 0;
    public static final Integer GROUPE_TYPE = 1;
    public static final Integer GROUPE_MANUEL_TYPE = 2;


    public static final String COMPETENCES_BUS_ADDRESS = "competences";
    public static final String VIESCO_BUS_ADDRESS = "viescolaire";
    public static final String DIRECTORY_ADDRESS = "directory";
    public static final String FEEDER_ADDRESS = "entcore.feeder";

    public static final String EDT_ADDRESS = "fr.cgi.edt";
    public static final String PRESENCES_ADDRESS = "fr.openent.presences";
    // rights
    public static final String MANAGE_TROMBINOSCOPE = "viescolaire.trombinoscope.manage";
    public static final String SEARCH = "viescolaire.search";
    public static JsonObject LSUN_CONFIG;
    public static JsonObject UPDATE_CLASSES_CONFIG;
    public static Long IMPORT_MAX_SIZE_OCTETS = 3000000L;
    public static Long TIME_OUT_HANDLER = 600 * 1000L;
    // usual keys
    public static String ID_KEY = "id";
    public static String ID_STRUCTURE_KEY = "idStructure";
    public static String ID_ETABLISSEMENT_KEY = "idEtablissement";
    public static String EXTERNAL_ID_KEY = "externalId";
    public static String NAME = "name";
    public static String FORADMIN = "forAdmin";
	public static final String SEARCH_RESTRICTED = "viescolaire.search.restricted";
    public static final String VIESCOLAIRE_1D = "viescolaire.1d";

	@Override
    public void start(Promise<Void> startPromise) throws Exception {
        super.start(startPromise);

        final EventBus eb = getEventBus(vertx);
        final Sql sql = Sql.getInstance();
        final Neo4j neo4j = Neo4j.getInstance();
        final MongoDb mongoDb = MongoDb.getInstance();
        final Storage storage = new StorageFactory(vertx).getStorage();
        final ServiceFactory serviceFactory = new ServiceFactory(eb, sql, neo4j, mongoDb, config);

        LSUN_CONFIG = config.getJsonObject("lsun");
        UPDATE_CLASSES_CONFIG = config.getJsonObject("update-classes");
        if (UPDATE_CLASSES_CONFIG.getString("enable-date") == null) {
            throw new RuntimeException("no date in update-classes");
        }

        DB.getInstance().init(neo4j, sql, mongoDb);

        /*
			DISPLAY CONTROLLER
		 */
        addController(new DisplayController());

		/*
			CONTROLEURS VIE SCOLAIRE
		 */
        addController(new CoursController());
        addController(new EleveController());
        addController(new ClasseController());
        addController(new PeriodeController());
        addController(new MatiereController(eb));
        addController(new MultiTeachingController(eb));
        addController(new GroupeEnseignementController());
        addController(new SousMatiereController());
        addController(new UserController());
        addController(new ImportCsvController(storage));
        addController(new PeriodeAnneeController());
        addController(new ServicesController(eb));
        addController(new TimeSlotController(serviceFactory));
        addController(new MementoController(eb));
        addController(new ConfigController(config));
        addController(new StructureController());
        addController(new TrombinoscopeController(vertx, storage));
        addController(new GroupingController(serviceFactory));
        addController(new InitController(serviceFactory));

        addController(new EventBusController(serviceFactory, config));

        setRepositoryEvents(new VieScolaireRepositoryEvents(serviceFactory));

        startPromise.tryComplete();
        startPromise.tryFail("[Vie-Scolaire@Viescolaire::start] Failed to start Vie-scolaire module.");

        // worker to be triggered manually
        vertx.deployVerticle(InitWorker1D.class, new DeploymentOptions().setConfig(config).setWorker(true));
    }

}
