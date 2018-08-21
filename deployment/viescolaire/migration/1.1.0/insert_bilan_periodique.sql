INSERT INTO notes.type_elt_bilan_periodique(nom, code) VALUES ('Enseignements pratiques interdisciplinaires', 'EPI');
INSERT INTO notes.type_elt_bilan_periodique(nom, code) VALUES ('Accompagnement personnalisé', 'AP');
INSERT INTO notes.type_elt_bilan_periodique(nom, code) VALUES ('Parcours', 'Parcours');

INSERT INTO notes.thematique_bilan_periodique(libelle, code, type_elt_bilan_periodique, personnalise) VALUES ('Parcours avenir', 'PAR_AVN', 3, false);
INSERT INTO notes.thematique_bilan_periodique(libelle, code, type_elt_bilan_periodique, personnalise) VALUES ('Parcours citoyen', 'PAR_CIT', 3, false);
INSERT INTO notes.thematique_bilan_periodique(libelle, code, type_elt_bilan_periodique, personnalise) VALUES ('Parcours d''éducation artistique et culturelle', 'PAR_ART', 3, false);
INSERT INTO notes.thematique_bilan_periodique(libelle, code, type_elt_bilan_periodique, personnalise) VALUES ('Parcours éducatif de santé', 'PAR_SAN', 3, false);

INSERT INTO notes.thematique_bilan_periodique(libelle, code, type_elt_bilan_periodique, personnalise) VALUES ('Corps, santé, bien-être et sécurité', 'EPI_SAN', 1, false);
INSERT INTO notes.thematique_bilan_periodique(libelle, code, type_elt_bilan_periodique, personnalise) VALUES ('Culture et création artistiques', 'EPI_ART', 1, false);
INSERT INTO notes.thematique_bilan_periodique(libelle, code, type_elt_bilan_periodique, personnalise) VALUES ('Transition écologique et développement durable', 'EPI_EDD', 1, false);
INSERT INTO notes.thematique_bilan_periodique(libelle, code, type_elt_bilan_periodique, personnalise) VALUES ('Information, communication, citoyenneté', 'EPI_EDD', 1, false);
INSERT INTO notes.thematique_bilan_periodique(libelle, code, type_elt_bilan_periodique, personnalise) VALUES ('Langues et cultures de l''Antiquité', 'EPI_LGA', 1, false);
INSERT INTO notes.thematique_bilan_periodique(libelle, code, type_elt_bilan_periodique, personnalise) VALUES ('Langues et cultures étrangères ou, le cas échéant, régionales', 'EPI_LGE', 1, false);
INSERT INTO notes.thematique_bilan_periodique(libelle, code, type_elt_bilan_periodique, personnalise) VALUES ('Monde économique et professionnel', 'EPI_PRO', 1, false);
INSERT INTO notes.thematique_bilan_periodique(libelle, code, type_elt_bilan_periodique, personnalise) VALUES ('Sciences, technologie et société', 'EPI_STS', 1, false);