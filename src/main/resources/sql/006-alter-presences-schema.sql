CREATE TABLE presences.declaration
(
  id               BIGSERIAL NOT NULL,
  titre            CHARACTER VARYING,
  timestamp_dt     TIMESTAMP WITHOUT TIME ZONE,
  timestamp_fn     TIMESTAMP WITHOUT TIME ZONE,
  commentaire      CHARACTER VARYING,
  id_eleve         CHARACTER VARYING(36),
  id_etablissement CHARACTER VARYING(36),
  traitee          BOOLEAN DEFAULT FALSE ,
  owner            CHARACTER VARYING(36),
  created          TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
  modified         TIMESTAMP WITHOUT TIME ZONE,
  CONSTRAINT declaration_pk PRIMARY KEY (id)
);