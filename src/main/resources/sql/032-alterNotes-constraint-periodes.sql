ALTER TABLE notes.element_programme  ADD CONSTRAINT fk_type_periode_id FOREIGN KEY (id_periode) REFERENCES viesco.rel_type_periode (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE,
  DROP CONSTRAINT fk_periode_id;

ALTER TABLE notes.moyenne_finale  ADD CONSTRAINT fk_type_periode_id FOREIGN KEY (id_periode) REFERENCES viesco.rel_type_periode (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE,
  DROP CONSTRAINT fk_id_periode;

ALTER TABLE notes.appreciation_classe  ADD CONSTRAINT fk_type_periode_id FOREIGN KEY (id_periode) REFERENCES viesco.rel_type_periode (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE,
  DROP CONSTRAINT fk_periode_id;

ALTER TABLE notes.positionnement  ADD CONSTRAINT fk_type_periode_id FOREIGN KEY (id_periode) REFERENCES viesco.rel_type_periode (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE,
  DROP CONSTRAINT fk_id_periode;

ALTER TABLE notes.appreciation_matiere_periode  ADD CONSTRAINT fk_type_periode_id FOREIGN KEY (id_periode) REFERENCES viesco.rel_type_periode (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE,
  DROP CONSTRAINT fk_id_periode;