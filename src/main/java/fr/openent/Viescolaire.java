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
import fr.openent.evaluations.controller.*;
import fr.openent.evaluations.controller.UtilsController;
import fr.openent.viescolaire.controller.*;
import fr.openent.absences.controller.*;
import fr.openent.viescolaire.controller.EleveController;
import fr.openent.viescolaire.service.impl.VieScolaireRepositoryEvents;
import fr.wseduc.webutils.email.EmailSender;
import org.entcore.common.email.EmailFactory;
import org.entcore.common.http.BaseServer;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.share.impl.SqlShareService;
import org.entcore.common.sql.SqlConf;
import org.entcore.common.sql.SqlConfs;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonArray;

import java.util.ArrayList;
import java.util.List;

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
	public final static String VSCO_PERSONNEL_TABLE = "personnel";
	public final static String VSCO_CLASSE_TABLE = "classe";
	public final static String VSCO_PERIODE_TABLE = "periode";
	public final static String VSCO_MATIERE_TABLE = "matiere";
	public final static String VSCO_SOUSMATIERE_TABLE = "sousmatiere";
	public final static String VSCO_PERSONNES_SUPP_TABLE = "personnes_supp";

	public final static String ABSC_APPEL_TABLE = "appel";
	public final static String ABSC_MOTIF_TABLE = "motif";
	public final static String ABSC_MOTIF_APPEL_TABLE = "motif_appel";
	public final static String ABSC_EVENEMENT_TABLE = "evenement";
	public final static String ABSC_CATEGORIE_MOTIF = "categorie_motif_absence";
	public final static String ABSC_CATEGORIE_MOTIF_APPEL = "categorie_motif_appel";

	public static final String EVAL_NOTES_TABLE = "notes";
	public static final String EVAL_DEVOIR_TABLE = "devoirs";
	public static final String EVAL_ANNOTATIONS = "annotations";
	public static final String EVAL_COMPETENCES_TABLE = "competences";
	public static final String EVAL_COMPETENCES_NOTES_TABLE = "competences_notes";
    public static final String EVAL_ENSEIGNEMENTS_TABLE = "enseignements";
	public static final String EVAL_DOMAINES_TABLE = "domaines";
	public static final String EVAL_REL_PROFESSEURS_REMPLACANTS_TABLE = "rel_professeurs_remplacants";
	public static final String EVAL_REL_ANNOTATIONS_DEVOIRS_TABLE = "rel_annotations_devoirs";
	public static final String EVAL_APPRECIATIONS_TABLE = "appreciations";
	public static final String EVAL_BFC_TABLE = "bilan_fin_cycle";
	public static final String EVAL_PERSO_NIVEAU_COMPETENCES_TABLE = "perso_niveau_competences";
	public static final String EVAL_NIVEAU_COMPETENCES_TABLE = "niveau_competences";
	public static final String EVAL_USE_PERSO_NIVEAU_COMPETENCES_TABLE = "use_perso";
	public static final String EVAL_CYCLE_TABLE = "cycle";

	/**
	 * Déclaration des router préfixs
	 */
	public final static String VSCO_PATHPREFIX = "/viescolaire";
	public final static String ABSC_PATHPREFIX = "/viescolaire/presences";
	public final static String EVAL_PATHPREFIX = "/viescolaire/evaluations";

	/**
	 * Déclaration des JSON Schéma validator
	 */
	public static final String SCHEMA_NOTES_CREATE = "eval_createNote";
	public static final String SCHEMA_NOTES_UPDATE = "eval_updateNote";
	public static final String SCHEMA_REL_PROFESSEURS_REMPLACANTS_CREATE = "eval_createRel_professeurs_remplacants";

	public static final String SCHEMA_DEVOIRS_CREATE = "eval_createDevoir";

	public static final String SCHEMA_COMPETENCES_DEVOIR = "eval_createCompetence";
	public static final String SCHEMA_DEVOIRS_UPDATE = "eval_updateDevoir";
    public static final String SCHEMA_COMPETENCE_NOTE_CREATE = "eval_createCompetenceNote";
    public static final String SCHEMA_COMPETENCE_NOTE_UPDATE = "eval_updateCompetenceNote";

	public static final String SCHEMA_APPEL_CREATE = "absc_createAppel";
	public static final String SCHEMA_APPEL_UPDATE = "absc_updateAppel";
	public static final String SCHEMA_CATEGORIE_ABS_CREATE = "absc_createCategorieAbs";
	public static final String SCHEMA_CATEGORIE_ABS_UPDATE = "absc_updateCategorieAbs";
	public static final String SCHEMA_MOTIF_CREATE = "absc_createMotif";
	public static final String SCHEMA_MOTIF_UPDATE = "absc_updateMotif";
	public static final String SCHEMA_EVENEMENT_CREATE = "absc_createEvenement";
	public static final String SCHEMA_EVENEMENT_UPDATE = "absc_updateEvenement";
	public static final String SCHEMA_ABSPREV_CREATE = "absc_createAbscPrev";
	public static final String SCHEMA_ABSPREV_UPDATE = "absc_updateAbscPrev";

	public final static String DEVOIR_RESOURCE_ID = "devoirsid";
	public final static String DEVOIR_TABLE = "devoirs";
	public final static String DEVOIR_SHARE_TABLE = "devoirs_shares";

	public final static String DEVOIR_ACTION_UPDATE = "fr-openent-evaluations-controller-DevoirController|updateDevoir";

	public final static String SCHEMA_APPRECIATIONS_CREATE = "eval_createAppreciation";
	public final static String SCHEMA_APPRECIATIONS_UPDATE = "eval_updateAppreciation";


	//public final static String SCHEMA_ANNOTATION_CREATE = "eval_createAnnotation";
	public final static String SCHEMA_ANNOTATION_UPDATE = "eval_updateAnnotation";
	public final static String SCHEMA_ANNOTATION_DELETE = "eval_deleteAnnotation";

	public final static String SCHEMA_MAITRISE_CREATE = "eval_createMaitrise";
	public final static String SCHEMA_MAITRISE_UPDATE = "eval_updateMaitrise";
	public final static String SCHEMA_USE_PERSO_NIVEAU_COMPETENCE = "eval_usePersoNiveauCompetence";

	public static final Integer CLASSE_TYPE = 0;
	public static final Integer GROUPE_TYPE = 1;

	public final static String SCHEMA_BFC_CREATE = "eval_createBFC";
	public final static String SCHEMA_BFC_UPDATE = "eval_updateBFC";

	public final static Integer MAX_NBR_COMPETENCE = 12;

	@Override
	public void start() {
		super.start();

		final EventBus eb = getEventBus(vertx);

		EmailFactory emailFactory = new EmailFactory(vertx, container, container.config());
		EmailSender notification = emailFactory.getSender();

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

		/*
			CONTROLEURS NOTES
		 */
		addController(new CompetenceController());
		addController(new CompetenceNoteController());
		addController(new RemplacementController());
		addController(new NiveauDeMaitriseController());

		// devoir table
		SqlConf confDevoir = SqlConfs.createConf(DevoirController.class.getName());
		confDevoir.setResourceIdLabel(DEVOIR_RESOURCE_ID);
		confDevoir.setTable(DEVOIR_TABLE);
		confDevoir.setShareTable(DEVOIR_SHARE_TABLE);
		confDevoir.setSchema(EVAL_SCHEMA);

		// devoir controller
		DevoirController devoirController = new DevoirController();
		SqlCrudService devoirSqlCrudService = new SqlCrudService(EVAL_SCHEMA, DEVOIR_TABLE, DEVOIR_SHARE_TABLE, new JsonArray().addString("*"), new JsonArray().add("*"), true);
		devoirController.setCrudService(devoirSqlCrudService);
		devoirController.setShareService(new SqlShareService(EVAL_SCHEMA, DEVOIR_SHARE_TABLE, eb, securedActions, null));
		addController(devoirController);

		addController(new EnseignementController());
		addController(new DomaineController());
		addController(new ExportPDFController(eb, notification));
		addController(new NoteController());
		addController(new AppreciationController());
		addController(new UtilsController());
		addController(new BFCController());
		addController(new AnnotationController());

		setRepositoryEvents(new VieScolaireRepositoryEvents());
	}

}
