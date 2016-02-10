package org.cgi;

//import org.cgi.viescolaire.CVscoCoursController;
import org.entcore.common.http.BaseServer;

public class Viescolaire extends BaseServer {

	public final static String VSCO_SCHEMA = "viesco";
	public final static String NOTE_SCHEMA = "notes";
	public final static String ABSC_SCHEMA = "abs";

	public final static String VSCO_COURS_TABLE = "cours";

	@Override
	public void start() {
		super.start();

		/*
			DISPLAY CONTROLLER
		 */
		addController(new DisplayController());

		/*
			CONTROLLER VIE SCOLAIRE
		 */
		//addController(new CVscoCoursController());
	}

}
