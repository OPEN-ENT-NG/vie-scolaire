ALTER TABLE notes.visibilite_moyenne_bfc RENAME TO visibility;

ALTER TABLE notes.visibility
 ADD COLUMN id_visibility int NOT NULL DEFAULT 1;

ALTER TABLE notes.visibility  ADD CONSTRAINT visibility_pkey UNIQUE (id_etablissement, id_visibility),
 DROP CONSTRAINT visibilite_moyenne_bfc_pk;
