ALTER TABLE notes.appreciation_elt_bilan_periodique_classe  ADD CONSTRAINT fk_type_periode_id FOREIGN KEY (id_periode) REFERENCES viesco.rel_type_periode (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE,
  DROP CONSTRAINT fk_id_periode;
ALTER TABLE notes.appreciation_elt_bilan_periodique_eleve  ADD CONSTRAINT fk_type_periode_id FOREIGN KEY (id_periode) REFERENCES viesco.rel_type_periode (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE,
  DROP CONSTRAINT fk_id_periode;