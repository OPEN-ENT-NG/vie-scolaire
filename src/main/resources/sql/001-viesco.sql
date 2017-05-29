CREATE SCHEMA viesco;
CREATE EXTENSION IF NOT EXISTS unaccent;

-- Only one scripts table (not one per schema)
CREATE TABLE viesco.scripts
(
  filename character varying(255) NOT NULL,
  passed timestamp without time zone NOT NULL DEFAULT now(),
  CONSTRAINT scripts_pkey PRIMARY KEY (filename)
);

CREATE TABLE viesco.type_sousmatiere
(
  id bigserial NOT NULL,
  libelle character varying(255),
  CONSTRAINT typesousmatiere_pk PRIMARY KEY (id)
);

CREATE TABLE viesco.sousmatiere
(
  id bigserial NOT NULL,
  id_type_sousmatiere bigint NOT NULL,
  id_matiere character varying(255) NOT NULL,
  CONSTRAINT sousmatiere_pk PRIMARY KEY (id),
  CONSTRAINT fk_typesousmatiere_id FOREIGN KEY (id_type_sousmatiere)
  REFERENCES viesco.type_sousmatiere (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE viesco.periode
(
  id bigserial NOT NULL,
  id_etablissement character varying(36),
  libelle character varying(42),
  timestamp_dt timestamp without time zone,
  timestamp_fn timestamp without time zone,
  CONSTRAINT periode_pk PRIMARY KEY (id)
);


CREATE INDEX sousmatiere_id_typesousmatiere_idx ON viesco.sousmatiere USING btree(id_type_sousmatiere, id_matiere);