DROP TABLE notes.avis_conseil_orientation;

CREATE TABLE notes.avis_conseil_orientation
(
  id_avis_conseil_bilan bigint NOT NULL,
    id_eleve character varying(255) NOT NULL,
  id_periode bigint NOT NULL,

  CONSTRAINT avis_conseil_orientation_pk PRIMARY KEY (id_eleve, id_periode),
  CONSTRAINT fk_id_avis_conseil_bilan FOREIGN KEY (id_avis_conseil_bilan)
  REFERENCES notes.avis_conseil_bilan_periodique (id) MATCH FULL ON UPDATE CASCADE ON DELETE CASCADE
);