ALTER TABLE notes.elt_bilan_periodique
DROP CONSTRAINT fk_thematique_bilan_periodique,
ADD CONSTRAINT fk_thematique_bilan_periodique FOREIGN KEY(id_thematique)
REFERENCES notes.thematique_bilan_periodique(id) MATCH SIMPLE
ON UPDATE NO ACTION
ON DELETE CASCADE;