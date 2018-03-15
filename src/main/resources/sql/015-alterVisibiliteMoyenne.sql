ALTER TABLE notes.visibilite_moyenne_bfc ALTER COLUMN visible DROP DEFAULT;

ALTER TABLE notes.visibilite_moyenne_bfc ALTER COLUMN visible TYPE integer
USING
CASE
	WHEN false THEN 0
	ELSE 1
END;

ALTER TABLE notes.visibilite_moyenne_bfc ALTER COLUMN visible SET DEFAULT 0;