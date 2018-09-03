CREATE TABLE notes.competence_niveau_final
(
	id_periode bigint NOT NULL,
	id_eleve character varying(255) NOT NULL,
	niveau_final integer,
	id_competence bigint NOT NULL,
	id_matiere character varying(255) NOT NULL,
	id_classe character varying(255) NOT NULL,
	CONSTRAINT pk_niveau_final PRIMARY KEY (id_periode, id_eleve, id_competence, id_matiere, id_classe),
	CONSTRAINT fk_periode_id FOREIGN KEY (id_periode)
	REFERENCES viesco.rel_type_periode (id) MATCH SIMPLE
	ON UPDATE CASCADE ON DELETE CASCADE,
	CONSTRAINT fk_competences_id FOREIGN KEY (id_competence)
	REFERENCES notes.competences (id) MATCH SIMPLE
	ON UPDATE CASCADE ON DELETE CASCADE,
	CONSTRAINT competence_niveau_final UNIQUE (id_periode, id_eleve, id_competence, id_matiere, id_classe)
	);