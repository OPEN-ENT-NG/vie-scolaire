package org.cgi;

import org.entcore.common.http.BaseServer;

public class Viescolaire extends BaseServer {

	@Override
	public void start() {
		super.start();
		addController(new DisplayController());
	}

}
