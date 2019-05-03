CREATE TABLE notes.avis_conseil_bilan_periodique
(
  id bigserial NOT NULL,
	libelle character varying(150) NOT NULL,
  type_avis bigint NOT NULL,

  CONSTRAINT avis_conseil_bilan_periodique_pk PRIMARY KEY (id)
);

INSERT INTO notes.avis_conseil_bilan_periodique(libelle, type_avis)
VALUES ('Félicitations', 1),
        ('Compliments', 1),
        ('Encouragements', 1),
        ('Félicitations à l’unanimité du conseil', 1),
        ('Admis(e) en 2nd générale', 2),
        ('Admis(e) en 2nd professionnelle', 2),
        ('Admis(e) en CAP', 2),
        ('Admis(e) en 3ème', 2),
        ('Admis(e) en 4ème', 2),
        ('Admis(e) en 5ème', 2),
        ('Redoublement', 2);