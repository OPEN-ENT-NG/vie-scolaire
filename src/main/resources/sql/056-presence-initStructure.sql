CREATE TABLE presences.init_structure
(
  id               BIGSERIAL NOT NULL,
  id_structure     CHARACTER VARYING(36),
  created          TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
  modified         TIMESTAMP WITHOUT TIME ZONE,
  CONSTRAINT init_structure_pk PRIMARY KEY (id)
);