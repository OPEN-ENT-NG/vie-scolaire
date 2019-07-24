CREATE TABLE notes.archive_bfc
(
  id_classe character varying NOT NULL,
  id_eleve character varying NOT NULL,
  id_etablissement character varying NOT NULL,
  external_id_classe character varying NOT NULL,
  id_cycle bigint,
  id_file character varying NOT NULL,
  created          TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
  file_name CHARACTER VARYING NOT NULL,
  CONSTRAINT archive_bfc_pk PRIMARY KEY (id_file)
);