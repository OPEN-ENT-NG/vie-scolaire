ALTER TABLE notes.thematique_bilan_periodique ADD COLUMN id_etablissement character varying(36);
ALTER TABLE notes.rel_elt_bilan_periodique_groupe ALTER COLUMN externalid_groupe DROP NOT NULL;