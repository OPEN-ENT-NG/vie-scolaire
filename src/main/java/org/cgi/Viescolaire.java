package org.cgi;

import org.cgi.absences.controller.CAbscEleveController;
import org.cgi.viescolaire.controller.CVscoClasseController;
import org.cgi.viescolaire.controller.CVscoCoursController;
import org.cgi.viescolaire.controller.CVscoEleveController;
import org.cgi.viescolaire.controller.CVsoPersonnelController;
import org.entcore.common.http.BaseServer;

public class Viescolaire extends BaseServer {

	public final static String VSCO_SCHEMA = "viesco";
	public final static String NOTE_SCHEMA = "notes";
	public final static String ABSC_SCHEMA = "abs";

	public final static String VSCO_COURS_TABLE = "cours";
	public final static String VSCO_ELEVE_TABLE = "eleve";
	public final static String VSCO_PERSONNEL_TABLE = "personnel";
	public final static String VSCO_CLASSE_TABLE = "classe";

	@Override
	public void start() {
		super.start();

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

		/*
			CONTROLEURS NOTES
		 */
	}

}
