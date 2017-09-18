CREATE TABLE notes.perso_niveau_competences
(
  id_etablissement character varying(36) NOT NULL,
  id_niveau integer,
  couleur character varying(10),
  lettre character(2),
  id bigserial NOT NULL,
  modified timestamp without time zone,
  CONSTRAINT perso_niveau_competences_pk PRIMARY KEY (id),
  CONSTRAINT fk_id_niveau FOREIGN KEY (id_niveau)
  REFERENCES notes.niveau_competences (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE INDEX idx_perso_niveau_competences  ON notes.perso_niveau_competences  USING btree
  (id_etablissement COLLATE pg_catalog."default");

CREATE TABLE notes.use_perso
(
  id_user character varying(36) NOT NULL,
  id bigserial NOT NULL,
  CONSTRAINT use_perso_pkey PRIMARY KEY (id)
)