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
  id bigint NOT NULL,
  fk4j_user_id uuid,
  externalid bigint,
  nom character varying(42),
  prenom character varying(42),
  profil character varying(42),
  enseigne boolean,
  id_etablissement uuid,
  CONSTRAINT personnel_pk PRIMARY KEY (id)
);

CREATE TABLE viesco.matiere
(
  id bigint NOT NULL,
  evaluable boolean,
  matiere character varying(255) NOT NULL,
  id_etablissement character varying(255),
  id_professeur character varying(255),
  CONSTRAINT matiere_pk PRIMARY KEY (id)
);


CREATE TABLE viesco.sousmatiere
(
  id bigserial NOT NULL,
  id_typesousmatiere bigint NOT NULL,
  id_matiere character varying(255) NOT NULL,
  CONSTRAINT sousmatiere_pk PRIMARY KEY (id)
);


CREATE TABLE viesco.type_sousmatiere
(
  id bigserial NOT NULL,
  libelle character varying(255),
  CONSTRAINT typesousmatiere_pk PRIMARY KEY (id)
);

CREATE TABLE viesco.periode
(
  id bigserial NOT NULL,
  id_etablissement uuid,
  libelle character varying(42),
  timestamp_dt timestamp without time zone,
  timestamp_fn timestamp without time zone,
  CONSTRAINT periode_pk PRIMARY KEY (id)
);

CREATE TABLE viesco.eleve
(
  id bigserial NOT NULL,
  fk4j_user_id uuid,
  externalid bigint,
  nom character varying(42),
  prenom character varying(42),
  CONSTRAINT eleve_pk PRIMARY KEY (id)
);

CREATE TABLE viesco.type_classe
(
  id bigserial NOT NULL,
  libelle character varying(42),
  CONSTRAINT type_classe_pk PRIMARY KEY (id)
);

CREATE TABLE viesco.classe
(
  id bigint NOT NULL,
  fk4j_classe_id uuid,
  id_etablissement uuid,
  externalid character varying(42),
  libelle character varying(42),
  fk_type_classe_id bigint,
  CONSTRAINT classe_pk PRIMARY KEY (id),
  CONSTRAINT fk_type_classe_id FOREIGN KEY (fk_type_classe_id)
      REFERENCES viesco.type_classe (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE viesco.cours
(
  id bigint NOT NULL,
  id_etablissement uuid,
  timestamp_dt timestamp without time zone,
  timestamp_fn timestamp without time zone,
  salle character varying(42),
  matiere character varying(42),
  fk_edt_classe character varying(42),
  fk_edt_date character varying(42),
  fk_edt_salle character varying(42),
  fk_edt_matiere character varying(42),
  fk_edt_id_cours character varying(42),
  fk_classe_id bigint,
  CONSTRAINT cours_pk PRIMARY KEY (id),
  CONSTRAINT fk_classe_id FOREIGN KEY (fk_classe_id)
      REFERENCES viesco.classe (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);


CREATE TABLE viesco.rel_eleve_classe
(
  fk_classe_id bigint NOT NULL,
  fk_eleve_id bigint NOT NULL,
  CONSTRAINT rel_eleve_classe_pk PRIMARY KEY (fk_classe_id, fk_eleve_id),
  CONSTRAINT fk_classe_id FOREIGN KEY (fk_classe_id)
      REFERENCES viesco.classe (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_eleve_id FOREIGN KEY (fk_eleve_id)
      REFERENCES viesco.eleve (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE viesco.rel_personnel_cours
(
  fk_personnel_id bigint NOT NULL,
  fk_cours_id bigint NOT NULL,
  CONSTRAINT rel_personnel_cours_pk PRIMARY KEY (fk_personnel_id, fk_cours_id),
  CONSTRAINT fk_cours_id FOREIGN KEY (fk_cours_id)
      REFERENCES viesco.cours (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_personnel_id FOREIGN KEY (fk_personnel_id)
      REFERENCES viesco.personnel (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE INDEX sousmatiere_id_typesousmatiere_idx ON viesco.sousmatiere USING btree(id_typesousmatiere, id_matiere);