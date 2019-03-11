CREATE INDEX idx_amp_idEleve_idMatiere
    ON notes.appreciation_matiere_periode USING btree
    (id_eleve ASC NULLS LAST, id_matiere ASC NULLS LAST)
    TABLESPACE pg_default;