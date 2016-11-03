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

package fr.openent.evaluations.service.impl;

import fr.wseduc.webutils.Either;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.neo4j.Neo4jResult;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlStatementsBuilder;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

/*
 * Temp
 */
public class InitDataService {
	private final Sql sql;
	private final Neo4j neo4j = Neo4j.getInstance();
	private static final Logger log = LoggerFactory.getLogger(InitDataService.class);
	//Period plug
	private static final String[] LIBELLE_P = new String[]{"1er Trimestre", "2eme Trimestre", "3eme Trimestre", "Fin d''annee"};
	private static final String[] S_DATE_P = new String[]{"2015-09-01", "2015-11-30", "2016-03-21", "2016-09-01"};
	private static final String[] E_DATE_P = new String[]{"2015-11-27", "2016-03-18", "2016-07-01", "2016-12-18"};
	//Type plug
	private static final String[] NAME_T = new String[]{"Examen", "Evaluation"};
	private static final Boolean[] DEFAULT_T = new Boolean[]{false, true};

	public InitDataService() {
		sql = Sql.getInstance();
	}

	public void initData() {
		if (LIBELLE_P.length == S_DATE_P.length && S_DATE_P.length == E_DATE_P.length &&
				NAME_T.length == DEFAULT_T.length) {
			//init data for whole struct in relation to user
			neo4j.execute("MATCH (n:Structure)<-[r:ADMINISTRATIVE_ATTACHMENT]-(u:User) RETURN distinct n.id as id", new JsonObject(), Neo4jResult.validResultHandler(new Handler<Either<String, JsonArray>>() {
				@Override
				public void handle(Either<String, JsonArray> event) {
					if (event.isRight()) {
						final JsonArray ja = event.right().getValue();
						if (ja.size() > 0) {
							for (int i = 0; i < ja.size(); i++) {
								final String structId = ((JsonObject) ja.get(i)).getString("id");

								final SqlStatementsBuilder s = new SqlStatementsBuilder();

								initPeriod(structId, s);
								initType(structId, s);

								sql.transaction(s.build(), new Handler<Message<JsonObject>>() {
									@Override
									public void handle(Message<JsonObject> res) {
										if (!"ok".equals(res.body().getString("status"))) {
											log.error("Can't init data : " + res.body().getString("message", ""));
										}
									}
								});
							}
						} else {
							log.error("Can't init data : no structure found");
						}

					} else {
						log.error("Can't init data : " + event.left().getValue());
					}
				}
			}));
		} else {
			log.error("Can't init data : bad init datas");
		}
	}

	private void initPeriod(String structId, SqlStatementsBuilder s) {
		final String query =
				"INSERT INTO viesco.periode (libelle, timestamp_dt, timestamp_fn, id_etablissement) SELECT ?, to_date(?,'YYYY-MM-DD'), to_date(?,'YYYY-MM-DD'), ? WHERE NOT EXISTS " +
						"(SELECT * FROM viesco.periode WHERE libelle = ? AND timestamp_dt = to_date(?,'YYYY-MM-DD') AND timestamp_fn = to_date(?,'YYYY-MM-DD') AND id_etablissement = ?);";
		for (int j = 0; j < LIBELLE_P.length; j++) {
			JsonArray ar = new JsonArray()
					.add(LIBELLE_P[j]).add(S_DATE_P[j]).add(E_DATE_P[j]).add(structId)
					.add(LIBELLE_P[j]).add(S_DATE_P[j]).add(E_DATE_P[j]).add(structId);
			s.prepared(query, ar);
		}
	}

	private void initType(String structId, SqlStatementsBuilder s) {
		final String query =
				"INSERT INTO notes.type (nom, id_etablissement, default_type) SELECT ?, ?, ? WHERE NOT EXISTS " +
						"(SELECT * FROM notes.type WHERE nom = ? AND id_etablissement = ? AND default_type = ?);";
		for (int j = 0; j < NAME_T.length; j++) {
			JsonArray ar = new JsonArray()
					.add(NAME_T[j]).add(structId).add(DEFAULT_T[j])
					.add(NAME_T[j]).add(structId).add(DEFAULT_T[j]);
			s.prepared(query, ar);
		}
	}
}

