DELETE FROM notes.avis_conseil_bilan_periodique;

INSERT INTO notes.avis_conseil_bilan_periodique(id,libelle, type_avis)
VALUES (1,'Félicitations', 1),
        (2,'Compliments', 1),
        (3,'Encouragements', 1),
        (7,'Félicitations à l’unanimité du conseil', 1),
        (4,'Admis(e) en 2nd générale', 2),
        (5,'Admis(e) en 2nd professionnelle', 2),
        (6,'Admis(e) en CAP', 2),
        (8,'Admis(e) en 3ème', 2),
        (9,'Admis(e) en 4ème', 2),
        (10,'Admis(e) en 5ème', 2),
        (11,'Redoublement', 2);