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
import fr.openent.viescolaire.service.impl.DefaultTimeSlotService;
import fr.openent.viescolaire.service.impl.VieScolaireRepositoryEvents;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import org.entcore.common.http.BaseServer;
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
	public final static String VSCO_COURS_TABLE = "cours";
	public final static String VSCO_ELEVE_TABLE = "eleve";
	public final static String VSCO_CLASSE_TABLE = "classe";
	public final static String VSCO_PERIODE_TABLE = "periode";
	public final static String VSCO_MATIERE_TABLE = "matiere";
	public final static String VSCO_SOUSMATIERE_TABLE = "sousmatiere";
	public final static String VSCO_SETTING_PERIOD = "setting_period";
	public final static String VSCO_MATIERE_LIBELLE_TABLE = "subject_libelle";
	public final static String VSCO_MODEL_MATIERE_LIBELLE_TABLE = "model_subject_libelle";
	public final static String VSCO_TIME_SLOTS = "time_slots";
	public final static String VSCO_SLOTS = "slots";
	public static final String SERVICES_TABLE = "services";

	/**
	 * Déclaration des router préfixs
	 */
	public final static String VSCO_PATHPREFIX = "/viescolaire";

	public static final Integer CLASSE_TYPE = 0;
	public static final Integer GROUPE_TYPE = 1;
	public static final Integer GROUPE_MANUEL_TYPE = 2;


	public static final String COMPETENCES_BUS_ADDRESS = "competences";
	public static final String VIESCO_BUS_ADDRESS = "viescolaire";


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
	public final static String DIRECTORY_ADDRESS = "directory";


	@Override
	public void start() throws Exception {
		super.start();

        final EventBus eb = getEventBus(vertx);
        final Storage storage = new StorageFactory(vertx).getStorage();

		LSUN_CONFIG = config.getJsonObject("lsun");
		UPDATE_CLASSES_CONFIG = config.getJsonObject("update-classes");
		if(UPDATE_CLASSES_CONFIG.getString("enable-date") == null){
			throw new RuntimeException("no date in update-classes");
		}
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
		addController(new MultiTeachingController());
		addController(new GroupeEnseignementController());
		addController(new SousMatiereController());
		addController(new UserController());
		addController(new ImportCsvController(storage));
		addController(new PeriodeAnneeController());
		addController(new ServicesController());
		addController(new TimeSlotController(new DefaultTimeSlotService()));
		addController(new MementoController(eb));
		addController(new ConfigController(config));

		addController(new EventBusController(eb,config));

		setRepositoryEvents(new VieScolaireRepositoryEvents(eb,config));
	}

}
