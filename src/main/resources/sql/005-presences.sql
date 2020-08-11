CREATE TABLE viesco.cours
(
  id bigserial NOT NULL,
  id_etablissement character varying(36),
  timestamp_dt timestamp without time zone,
  timestamp_fn timestamp without time zone,
  salle character varying(42),
  id_matiere character varying(36),
  edt_classe character varying(36),
  edt_date character varying(36),
  edt_salle character varying(36),
  edt_matiere character varying(36),
  edt_id_cours character varying(36),
  id_classe character varying(36),
  id_personnel character varying(36),
  CONSTRAINT cours_pkey PRIMARY KEY (id)
);