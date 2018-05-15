ALTER TABLE presences.sanction_type
ADD COLUMN period_absence_motif_id BIGINT,
ADD CONSTRAINT fk_sanction_type_motif_id FOREIGN KEY (period_absence_motif_id) REFERENCES presences.motif(id);