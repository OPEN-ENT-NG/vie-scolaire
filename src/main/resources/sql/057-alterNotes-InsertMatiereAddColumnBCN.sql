INSERT INTO notes.matiere(code, libelle_court, libelle_long) VALUES ('030202','AG2ND','ANGLAIS LV2 ND') ON CONFLICT ON CONSTRAINT libelle_court_unique DO NOTHING;
INSERT INTO notes.matiere(code, libelle_court, libelle_long) VALUES ('040600','HIGEO','HISTOIRE ET GEOGRAPHIE') ON CONFLICT ON CONSTRAINT libelle_court_unique DO NOTHING;;
INSERT INTO notes.matiere(code, libelle_court, libelle_long) VALUES ('030601','ESP1','ESPAGNOL LV1') ON CONFLICT ON CONSTRAINT libelle_court_unique DO NOTHING;
INSERT INTO notes.matiere(code, libelle_court, libelle_long) VALUES ('020100','LATIN','LATIN') ON CONFLICT ON CONSTRAINT libelle_court_unique DO NOTHING;
INSERT INTO notes.matiere(code, libelle_court, libelle_long) VALUES ('081300','EDmusic','EDUCATION MUSICALE') ON CONFLICT ON CONSTRAINT libelle_court_unique DO NOTHING;
INSERT INTO notes.matiere(code, libelle_court, libelle_long) VALUES ('030602','Espagnol','ESPAGNOL') ON CONFLICT ON CONSTRAINT libelle_court_unique DO NOTHING;
INSERT INTO notes.matiere(code, libelle_court, libelle_long) VALUES ('061300','Maths','MATHEMATIQUES') ON CONFLICT ON CONSTRAINT libelle_court_unique DO NOTHING;
INSERT INTO notes.matiere(code, libelle_court, libelle_long) VALUES ('068300','AP MATH','AIDE INDIVIDUALISEE EN MATHEMATIQUES') ON CONFLICT ON CONSTRAINT libelle_court_unique DO NOTHING;
INSERT INTO notes.matiere(code, libelle_court, libelle_long) VALUES ('008400','VDC','VIE DE CLASSE') ON CONFLICT ON CONSTRAINT libelle_court_unique DO NOTHING;
INSERT INTO notes.matiere(code, libelle_court, libelle_long) VALUES ('023100','AP FRANC','AIDE INDIVIDUALISEE EN FRANCAIS') ON CONFLICT ON CONSTRAINT libelle_court_unique DO NOTHING;
INSERT INTO notes.matiere(code, libelle_court, libelle_long) VALUES ('061300','MATH','MATHEMATIQUES') ON CONFLICT ON CONSTRAINT libelle_court_unique DO NOTHING;
INSERT INTO notes.matiere(code, libelle_court, libelle_long) VALUES ('020700','APFR','FRANCAIS') ON CONFLICT ON CONSTRAINT libelle_court_unique DO NOTHING;
INSERT INTO notes.matiere(code, libelle_court, libelle_long) VALUES ('061300','APMATH2','MATHEMATIQUES') ON CONFLICT ON CONSTRAINT libelle_court_unique DO NOTHING;

DELETE FROM notes.matiere WHERE libelle_court = 'ESP2''';
ALTER TABLE notes.matiere ADD COLUMN bcn BOOLEAN NOT NULL DEFAULT FALSE;
UPDATE notes.matiere SET bcn = true WHERE id <= 72 ;
UPDATE notes.matiere SET bcn = true WHERE libelle_court IN ('HIGEO','ESP1','LATIN');