CREATE TABLE viesco.settings (
    structure_id character varying (36) NOT NULL,
    initialized boolean NOT NULL DEFAULT FALSE,
    CONSTRAINT settings_pkey PRIMARY KEY(structure_id)
);