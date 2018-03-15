/*
 * Copyright (c) Région Hauts-de-France, Département 77, CGI, 2016.
 *
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
 *
 */

package fr.openent;

import fr.openent.absences.controller.*;
import fr.openent.viescolaire.controller.*;
import fr.openent.viescolaire.controller.EleveController;
import fr.openent.viescolaire.service.impl.VieScolaireRepositoryEvents;
import org.entcore.common.http.BaseServer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonObject;

public class Viescolaire extends BaseServer {

	/**
	 * Déclaration des schémas
	 */
	public final static String VSCO_SCHEMA = "viesco";
	public final static String EVAL_SCHEMA = "notes";
	public final static String ABSC_SCHEMA = "presences";

	/**
	 * Déclaration des tables
	 */
	public final static String VSCO_COURS_TABLE = "cours";
	public final static String VSCO_ELEVE_TABLE = "eleve";
	public final static String VSCO_CLASSE_TABLE = "classe";
	public final static String VSCO_PERIODE_TABLE = "periode";
	public final static String VSCO_MATIERE_TABLE = "matiere";
	public final static String VSCO_SOUSMATIERE_TABLE = "sousmatiere";

	public final static String ABSC_APPEL_TABLE = "appel";
	public final static String ABSC_MOTIF_TABLE = "motif";
	public final static String ABSC_MOTIF_APPEL_TABLE = "motif_appel";
	public final static String ABSC_EVENEMENT_TABLE = "evenement";
	public final static String ABSC_CATEGORIE_MOTIF_APPEL = "categorie_motif_appel";

	public static final String EVAL_NOTES_TABLE = "notes";
	/**
	 * Déclaration des router préfixs
	 */
	public final static String VSCO_PATHPREFIX = "/viescolaire";
	public final static String ABSC_PATHPREFIX = "/viescolaire/presences";
	public final static String EVAL_PATHPREFIX = "/viescolaire/evaluations";

	public static final String SCHEMA_APPEL_CREATE = "absc_createAppel";
	public static final String SCHEMA_APPEL_UPDATE = "absc_updateAppel";
	public static final String SCHEMA_CATEGORIE_ABS_CREATE = "absc_createCategorieAbs";
	public static final String SCHEMA_CATEGORIE_ABS_UPDATE = "absc_updateCategorieAbs";
	public static final String SCHEMA_MOTIF_CREATE = "absc_createMotif";
	public static final String SCHEMA_MOTIF_UPDATE = "absc_updateMotif";
	public static final String SCHEMA_EVENEMENT_CREATE = "absc_createEvenement";
	public static final String SCHEMA_EVENEMENT_UPDATE = "absc_updateEvenement";
	public static final String SCHEMA_ABSPREV_CREATE = "absc_createAbscPrev";
	public static final Integer CLASSE_TYPE = 0;
	public static final Integer GROUPE_TYPE = 1;
	public static final Integer GROUPE_MANUEL_TYPE = 2;

	public static final String COMPETENCES_BUS_ADDRESS = "competences";

	public static JsonObject LSUN_CONFIG;

	@Override
	public void start() {
		super.start();

        final EventBus eb = getEventBus(vertx);

		LSUN_CONFIG = config.getObject("lsun");
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
		addController(new MatiereController());
		addController(new GroupeEnseignementController());
		addController(new SousMatiereController());
		addController(new UserController());


		/*
			CONTROLEURS ABSENCES
		 */
		addController(new fr.openent.absences.controller.EleveController());
		addController(new MotifController());
		addController(new MotifAppelController());
		addController(new AppelController());
		addController(new AbsencePrevisionnelleController());
		addController(new EvenementController());

        addController(new EventBusController());

        setRepositoryEvents(new VieScolaireRepositoryEvents(eb));
	}

}
