CREATE TABLE notes.eleves_ignores_lsu
(
  id_classe character varying NOT NULL,
  id_eleve character varying NOT NULL,
  id_periode bigint,
  created          TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
  CONSTRAINT eleves_ignores_lsu_pk PRIMARY KEY (id_eleve, id_periode, id_classe)
);