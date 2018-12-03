BEGIN;

CREATE INDEX IF NOT EXISTS competences_notes_id_eleve_idx
    ON notes.competences_notes USING btree
    (id_eleve COLLATE pg_catalog."default" bpchar_pattern_ops)
    TABLESPACE pg_default;

END;