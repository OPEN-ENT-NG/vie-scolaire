package org.cgi;

import org.cgi.absences.controller.CAbscAppelController;
import org.cgi.absences.controller.CAbscEleveController;
import org.cgi.absences.controller.CAbscEvenementController;
import org.cgi.absences.controller.CAbscMotifController;
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

	public final static String ABSC_APPEL_TABLE = "pv_appel";
	public final static String ABSC_MOTIF_TABLE = "motif";
	public final static String ABSC_EVENEMENT_TABLE = "evenement";

	public final static String VSCO_PATHPREFIX = "/viescolaire";
	public final static String ABSC_PATHPREFIX = "/viescolaire/absences";

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
		addController(new CAbscMotifController());
		addController(new CAbscAppelController());
		addController(new CAbscEvenementController());

		/*
			CONTROLEURS NOTES
		 */
	}

}
