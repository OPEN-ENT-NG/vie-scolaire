CREATE TABLE notes.archive_bulletins
(
  id_classe character varying NOT NULL,
  id_eleve character varying NOT NULL,
  id_etablissement character varying NOT NULL,
  external_id_classe character varying NOT NULL,
  id_periode bigint,
  id_file character varying NOT NULL,
  created          TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
  CONSTRAINT archive_bulletins_pk PRIMARY KEY (id_file)
);