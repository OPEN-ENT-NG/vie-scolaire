CREATE TABLE viesco.absences_et_retards
(
	id_periode bigint NOT NULL,
	id_eleve character varying(255) NOT NULL,
	abs_totale integer,
	abs_totale_heure integer,
	abs_non_just integer,
	abs_non_just_heure integer,
	abs_just integer,
	abs_just_heure integer,
	retard integer,
	CONSTRAINT pk_absences_et_retards PRIMARY KEY (id_periode, id_eleve),
	CONSTRAINT fk_periode_id FOREIGN KEY (id_periode)
	REFERENCES viesco.rel_type_periode (id) MATCH SIMPLE
	ON UPDATE CASCADE ON DELETE CASCADE
	);