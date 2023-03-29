CREATE TABLE viesco.settings (
    structure_id character varying (36) NOT NULL,
    initialized boolean NOT NULL DEFAULT FALSE,
    CONSTRAINT settings_pkey PRIMARY KEY(structure_id)
);


INSERT INTO viesco.settings (structure_id)
SELECT id FROM viesco.structures
WHERE structures.id NOT IN (
    SELECT structure_id
    FROM viesco.settings
);