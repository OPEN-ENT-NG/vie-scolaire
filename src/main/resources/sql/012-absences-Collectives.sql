
ALTER TABLE presences.motif
  ADD COLUMN collectif boolean,
  ALTER COLUMN id_categorie DROP NOT NULL,
  ALTER COLUMN id_categorie DROP DEFAULT
;

CREATE SEQUENCE presences.periodes_absence_id_seq
  INCREMENT 1
  START 259
  MINVALUE 1
  MAXVALUE 9223372036854775807
  CACHE 1;

CREATE TABLE presences.periodes_absence
(
  id bigint NOT NULL DEFAULT nextval('presences.periodes_absence_id_seq'::regclass),
  timestamp_dt timestamp without time zone,
  timestamp_fn timestamp without time zone,
  commentaire character varying(250),
  collectif boolean,
  id_motif bigint,
  CONSTRAINT periode_absence_pk PRIMARY KEY (id),
  CONSTRAINT fk_motif_id FOREIGN KEY (id_motif)
  REFERENCES presences.motif (id) MATCH SIMPLE
  ON UPDATE NO ACTION
  ON DELETE NO ACTION
);

CREATE TABLE presences.rel_periodes_absence_groupes
(
  id_groupe character varying(255) NOT NULL,
  id_periode_absence bigint NOT NULL,
  CONSTRAINT rel_periodes_absence_groupes_pk PRIMARY KEY (id_groupe, id_periode_absence),
  CONSTRAINT fk_periode_absence_id FOREIGN KEY (id_periode_absence)
  REFERENCES presences.periodes_absence (id) MATCH FULL
  ON UPDATE CASCADE
  ON DELETE CASCADE
);

CREATE TABLE presences.rel_periodes_absence_eleves
(
  id_eleve character varying(255) NOT NULL,
  id_periode_absence bigint NOT NULL,
  CONSTRAINT rel_periodes_absence_eleves_pk PRIMARY KEY (id_eleve, id_periode_absence),
  CONSTRAINT fk_periode_absence_id FOREIGN KEY (id_periode_absence)
  REFERENCES presences.periodes_absence (id) MATCH FULL
  ON UPDATE CASCADE
  ON DELETE CASCADE
);