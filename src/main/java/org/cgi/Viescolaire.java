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

package org.cgi;

import fr.wseduc.webutils.email.EmailSender;
import fr.wseduc.webutils.email.NotificationHelper;
import org.cgi.absences.controller.CAbscAppelController;
import org.cgi.absences.controller.CAbscEleveController;
import org.cgi.absences.controller.CAbscEvenementController;
import org.cgi.absences.controller.CAbscMotifController;
import org.cgi.evaluations.controller.*;
import org.cgi.viescolaire.controller.CVscoClasseController;
import org.cgi.viescolaire.controller.CVscoCoursController;
import org.cgi.viescolaire.controller.CVscoEleveController;
import org.cgi.viescolaire.controller.CVsoPersonnelController;
import org.entcore.common.email.EmailFactory;
import org.entcore.common.http.BaseServer;
import org.vertx.java.core.eventbus.EventBus;

public class Viescolaire extends BaseServer {

	/**
	 * Déclaration des schémas
	 */
	public final static String VSCO_SCHEMA = "viesco";
	public final static String EVALUATIONS_SCHEMA = "notes";
	public final static String ABSC_SCHEMA = "abs";

	/**
	 * Déclaration des tables
	 */
	public final static String VSCO_COURS_TABLE = "cours";
	public final static String VSCO_ELEVE_TABLE = "eleve";
	public final static String VSCO_PERSONNEL_TABLE = "personnel";
	public final static String VSCO_CLASSE_TABLE = "classe";

	public final static String ABSC_APPEL_TABLE = "appel";
	public final static String ABSC_MOTIF_TABLE = "motif";
	public final static String ABSC_EVENEMENT_TABLE = "evenement";

	public static final String EVAL_NOTES_TABLE = "notes";
	public static final String EVAL_DEVOIR_TABLE = "devoirs";
	public static final String EVAL_COMPETENCES_TABLE = "competences";
	public static final String EVAL_COMPETENCES_NOTES_TABLE = "competences_notes";
    public static final String EVAL_ENSEIGNEMENTS_TABLE = "enseignements";

	/**
	 * Déclaration des router préfixs
	 */
	public final static String VSCO_PATHPREFIX = "/viescolaire";
	public final static String ABSC_PATHPREFIX = "/viescolaire/absences";
	public final static String EVAL_PATHPREFIX = "/viescolaire/evaluations";

	/**
	 * Déclaration des JSON Schéma validator
	 */
	public static final String SCHEMA_NOTES_CREATE = "eval_createNote";
	public static final String SCHEMA_NOTES_UPDATE = "eval_updateNote";
	public static final String SCHEMA_DEVOIRS_CREATE = "eval_createDevoir";
	public static final String SCHEMA_COMPETENCES_DEVOIR = "eval_createCompetence";
	public static final String SCHEMA_DEVOIRS_UPDATE = "eval_updateDevoir";
    public static final String SCHEMA_COMPETENCE_NOTE_CREATE = "eval_createCompetenceNote";
    public static final String SCHEMA_COMPETENCE_NOTE_UPDATE = "eval_updateCompetenceNote";

	public static final String SCHEMA_APPEL_CREATE = "absc_createAppel";
	public static final String SCHEMA_APPEL_UPDATE = "absc_updateAppel";
	public static final String SCHEMA_EVENEMENT_CREATE = "absc_createEvenement";
	public static final String SCHEMA_EVENEMENt_UPDATE = "absc_updateEvenement";

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
		addController(new CVscoCoursController());
		addController(new CVscoEleveController());
		addController(new CVsoPersonnelController());
		addController(new CVscoClasseController());

		/*
			CONTROLEURS ABSENCES
		 */
		addController(new CAbscEleveController());
		addController(new CAbscMotifController());
		addController(new CAbscAppelController());
		addController(new CAbscEvenementController());

		/*
			CONTROLEURS NOTES
		 */
		addController(new CEvalCompetenceController());
		addController(new CEvalDevoirController());
		addController(new CEvalEnseignementController());
		addController(new CEvalExportPDFController(eb, notification));
		addController(new CEvalNoteController());
		addController(new CEvalUtilsController());
	}

}
