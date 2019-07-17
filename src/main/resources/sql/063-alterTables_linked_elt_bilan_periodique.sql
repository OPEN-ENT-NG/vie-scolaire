ALTER TABLE notes.rel_groupe_appreciation_elt_eleve
ADD CONSTRAINT fk_id_elt_bilan_periodique FOREIGN KEY(id_elt_bilan_periodique)
REFERENCES notes.elt_bilan_periodique(id) ON UPDATE NO ACTION ON DELETE CASCADE;

ALTER TABLE notes.appreciation_elt_bilan_periodique_classe
DROP CONSTRAINT fk_elt_bilan_periodique_id,
ADD CONSTRAINT fk_elt_bilan_periodique_id FOREIGN KEY(id_elt_bilan_periodique)
REFERENCES notes.elt_bilan_periodique(id)
ON UPDATE NO ACTION
ON DELETE CASCADE;

ALTER TABLE notes.appreciation_elt_bilan_periodique_eleve
DROP CONSTRAINT fk_elt_bilan_periodique_id,
ADD CONSTRAINT fk_elt_bilan_periodique_id FOREIGN KEY(id_elt_bilan_periodique)
REFERENCES notes.elt_bilan_periodique(id)
ON UPDATE NO ACTION
ON DELETE CASCADE;