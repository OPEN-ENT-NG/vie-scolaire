CREATE TABLE viesco.model_subject_libelle (
  id bigserial NOT NULL,
  title character varying,
  id_etablissement character varying(36) NOT NULL,
  CONSTRAINT model_libelle_subject_pkey PRIMARY KEY (id)
);


CREATE TABLE viesco.subject_libelle (
  id_model bigserial NOT NULL,
  libelle character varying,
  external_id_subject character varying(36) NOT NULL,
  CONSTRAINT subject_libelle_unique UNIQUE (external_id_subject, id_model),
  FOREIGN KEY (id_model)
	REFERENCES viesco.model_subject_libelle (id) MATCH SIMPLE
	ON UPDATE CASCADE ON DELETE CASCADE
);

