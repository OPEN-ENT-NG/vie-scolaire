UPDATE ONLY notes.domaines SET code_domaine = 'CPD_FRA' WHERE codification = 'D1.1';
UPDATE ONLY notes.domaines SET code_domaine = 'CPD_ETR' WHERE codification = 'D1.2';
UPDATE ONLY notes.domaines SET code_domaine = 'CPD_SCI' WHERE codification = 'D1.3';
UPDATE ONLY notes.domaines SET code_domaine = 'CPD_ART' WHERE codification = 'D1.4';
UPDATE ONLY notes.domaines SET code_domaine = 'MET_APP' WHERE codification = 'D2';
UPDATE ONLY notes.domaines SET code_domaine = 'FRM_CIT' WHERE codification = 'D3';
UPDATE ONLY notes.domaines SET code_domaine = 'SYS_NAT' WHERE codification = 'D4';
UPDATE ONLY notes.domaines SET code_domaine = 'REP_MND' WHERE codification = 'D5';

INSERT INTO notes.enseignement_complement(libelle, code) VALUES ('Aucun', 'AUC');
INSERT INTO notes.enseignement_complement(libelle, code) VALUES ('Langues et cultures de l''Antiquité', 'LCA');
INSERT INTO notes.enseignement_complement(libelle, code)   VALUES ('Langues et cultures européennes', 'LCE');
INSERT INTO notes.enseignement_complement(libelle, code) VALUES ('Langue et culture régionale', 'LCR');
INSERT INTO notes.enseignement_complement(libelle, code)   VALUES ('Chant choral', 'CHK');
INSERT INTO notes.enseignement_complement(libelle, code) VALUES ('Découverte professionnelle', 'PRO');
INSERT INTO notes.enseignement_complement(libelle, code) VALUES ('Langue des signes française', 'LSF');
INSERT INTO notes.enseignement_complement(libelle, code) VALUES ('Langue vivante étrangère', 'LVE');

UPDATE notes.cycle SET value_cycle=4 WHERE libelle='Cycle 4';
UPDATE notes.cycle SET value_cycle=3 WHERE libelle='Cycle 3';

INSERT INTO notes.calc_millesime(code_level, increment) VALUES ('6EME', 0);
INSERT INTO notes.calc_millesime(code_level, increment) VALUES ('5EME', 2);
INSERT INTO notes.calc_millesime(code_level, increment) VALUES ('4EME', 1);
INSERT INTO notes.calc_millesime(code_level, increment) VALUES ('3EME', 0);