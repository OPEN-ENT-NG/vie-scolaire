CREATE TABLE notes.avis_conseil_bilan_periodique
(
  id bigserial NOT NULL,
	libelle character varying(150) NOT NULL,
  type_avis bigint NOT NULL,

  CONSTRAINT avis_conseil_bilan_periodique_pk PRIMARY KEY (id)
);

INSERT INTO notes.avis_conseil_bilan_periodique(id, libelle, type_avis) VALUES (1, 'Félicitations', 1);
INSERT INTO notes.avis_conseil_bilan_periodique(id, libelle, type_avis) VALUES (2, 'Compliments', 1);
INSERT INTO notes.avis_conseil_bilan_periodique(id, libelle, type_avis) VALUES (3, 'Encouragements', 1);

INSERT INTO notes.avis_conseil_bilan_periodique(id, libelle, type_avis) VALUES (4, 'Admis en classe supérieur', 2);
INSERT INTO notes.avis_conseil_bilan_periodique(id, libelle, type_avis) VALUES (5, 'Redoublement', 2);
INSERT INTO notes.avis_conseil_bilan_periodique(id, libelle, type_avis) VALUES (6, 'Réorientation', 2);