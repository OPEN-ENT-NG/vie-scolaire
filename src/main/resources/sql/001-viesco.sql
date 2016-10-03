CREATE SCHEMA viesco;

-- Only one scripts table (not one per schema)
CREATE TABLE viesco.scripts
(
  filename character varying(255) NOT NULL,
  passed timestamp without time zone NOT NULL DEFAULT now(),
  CONSTRAINT scripts_pkey PRIMARY KEY (filename)
);

CREATE TABLE viesco.personnel
(
  personnel_id bigserial NOT NULL,
  fk4j_user_id uuid,
  personnel_externalid bigint,
  personnel_nom character varying(42),
  personnel_prenom character varying(42),
  personnel_profil character varying(42),
  personnel_enseigne boolean,
  fk4j_etab_id uuid,
  CONSTRAINT personnel_pkey PRIMARY KEY (personnel_id)
);

CREATE TABLE viesco.periode
(
  periode_id bigserial NOT NULL,
  fk4j_etab_id uuid,
  periode_libelle character varying(42),
  periode_timestamp_dt timestamp without time zone,
  periode_timestamp_fn timestamp without time zone,
  CONSTRAINT periode_pkey PRIMARY KEY (periode_id)
);

CREATE TABLE viesco.eleve
(
  eleve_id bigserial NOT NULL,
  fk4j_user_id uuid,
  eleve_externalid bigint,
  eleve_nom character varying(42),
  eleve_prenom character varying(42),
  CONSTRAINT eleve_pkey PRIMARY KEY (eleve_id)
);

CREATE TABLE viesco.type_classe
(
  type_classe_id bigserial NOT NULL,
  type_classe_libelle character varying(42),
  CONSTRAINT type_classe_pkey PRIMARY KEY (type_classe_id)
);

CREATE TABLE viesco.classe
(
  classe_id bigserial NOT NULL,
  fk4j_classe_id uuid,
  fk4j_etab_id uuid,
  classe_externalid character varying(42),
  classe_libelle character varying(42),
  fk_type_classe_id bigint,
  CONSTRAINT classe_pkey PRIMARY KEY (classe_id),
  CONSTRAINT classe_fk_type_classe_id_fkey FOREIGN KEY (fk_type_classe_id)
      REFERENCES viesco.type_classe (type_classe_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE viesco.cours
(
  cours_id bigserial NOT NULL,
  fk4j_etab_id uuid,
  cours_timestamp_dt timestamp without time zone,
  cours_timestamp_fn timestamp without time zone,
  cours_salle character varying(42),
  cours_matiere character varying(42),
  fk_edt_classe character varying(42),
  fk_edt_date character varying(42),
  fk_edt_salle character varying(42),
  fk_edt_matiere character varying(42),
  fk_edt_id_cours character varying(42),
  fk_classe_id bigint,
  CONSTRAINT cours_pkey PRIMARY KEY (cours_id),
  CONSTRAINT cours_fk_classe_id_fkey FOREIGN KEY (fk_classe_id)
      REFERENCES viesco.classe (classe_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);


CREATE TABLE viesco.rel_eleve_classe
(
  fk_classe_id bigint NOT NULL,
  fk_eleve_id bigint NOT NULL,
  CONSTRAINT rel_eleve_classe_pkey PRIMARY KEY (fk_classe_id, fk_eleve_id),
  CONSTRAINT rel_eleve_classe_fk_classe_id_fkey FOREIGN KEY (fk_classe_id)
      REFERENCES viesco.classe (classe_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT rel_eleve_classe_fk_eleve_id_fkey FOREIGN KEY (fk_eleve_id)
      REFERENCES viesco.eleve (eleve_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE viesco.rel_personnel_cours
(
  fk_personnel_id bigint NOT NULL,
  fk_cours_id bigint NOT NULL,
  CONSTRAINT rel_personnel_cours_pkey PRIMARY KEY (fk_personnel_id, fk_cours_id),
  CONSTRAINT rel_personnel_cours_fk_cours_id_fkey FOREIGN KEY (fk_cours_id)
      REFERENCES viesco.cours (cours_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT rel_personnel_cours_fk_personnel_id_fkey FOREIGN KEY (fk_personnel_id)
      REFERENCES viesco.personnel (personnel_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);