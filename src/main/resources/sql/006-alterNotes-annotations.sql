-- Rajout des tables pour la gestion des annotations

CREATE TABLE notes.annotations (
  id bigserial NOT NULL,
  libelle character varying(255),
  libelle_court character varying(30),
  id_etablissement character varying(36) NOT NULL,
  CONSTRAINT annotations_pk PRIMARY KEY (id)
);

CREATE TABLE notes.rel_annotations_devoirs  (
    id_devoir bigint NOT NULL,
    id_annotation bigint NOT NULL,
    id_eleve character varying(255) NOT NULL,
    CONSTRAINT annotations_unique UNIQUE (id_eleve, id_devoir),
    CONSTRAINT fk_devoir_id FOREIGN KEY (id_devoir)
    REFERENCES notes.devoirs (id) MATCH SIMPLE
    ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_annotations_id FOREIGN KEY (id_annotation)
    REFERENCES notes.annotations (id) MATCH SIMPLE
    ON UPDATE CASCADE ON DELETE CASCADE
);

