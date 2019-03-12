CREATE INDEX idx_amp_idEleve_idMatiere_idPeriode
    ON notes.appreciation_matiere_periode USING btree
    (id_eleve ASC NULLS LAST, id_matiere ASC NULLS LAST, id_periode ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE INDEX idx_posi_idEleve_idMatiere_idPeriode
    ON notes.positionnement USING btree
    (id_eleve ASC NULLS LAST, id_matiere ASC NULLS LAST, id_periode ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE INDEX idx_moy_idEleve_idMatiere_idPeriode
    ON notes.moyenne_finale USING btree
    (id_eleve ASC NULLS LAST, id_matiere ASC NULLS LAST, id_periode ASC NULLS LAST)
    TABLESPACE pg_default;


CREATE INDEX idx_posi_idEleve_idMatiere
    ON notes.positionnement USING btree
    (id_eleve ASC NULLS LAST, id_matiere ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE INDEX idx_moy_idEleve_idMatiere
    ON notes.moyenne_finale USING btree
    (id_eleve ASC NULLS LAST, id_matiere ASC NULLS LAST)
    TABLESPACE pg_default;