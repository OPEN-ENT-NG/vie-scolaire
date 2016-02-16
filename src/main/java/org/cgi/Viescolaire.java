package org.cgi;

import org.cgi.viescolaire.CVscoCoursController;
import org.cgi.viescolaire.CVscoEleveController;
import org.entcore.common.http.BaseServer;

public class Viescolaire extends BaseServer {

	public final static String VSCO_SCHEMA = "viesco";
	public final static String NOTE_SCHEMA = "notes";
	public final static String ABSC_SCHEMA = "abs";

	public final static String VSCO_COURS_TABLE = "cours";
	public final static String VSCO_ELEVE_TABLE = "eleve";

	@Override
	public void start() {
		super.start();

		/*
			DISPLAY CONTROLLER
		 */
		addController(new DisplayController());

		/*
			CONTROLLER VIE SCOLAIRE COURS
		 */
		addController(new CVscoCoursController());

		/*
			CONTROLLER VIE SCOLAIRE ELEVES
		 */
		addController(new CVscoEleveController());
	}

}
