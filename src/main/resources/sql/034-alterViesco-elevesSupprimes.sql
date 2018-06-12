
ALTER TABLE viesco.rel_groupes_personne_supp DROP COLUMN user_type;

ALTER TABLE viesco.personnes_supp
    RENAME date_suppression TO delete_date;

ALTER TABLE viesco.personnes_supp
    ADD COLUMN user_type character varying;

ALTER TABLE viesco.personnes_supp
    ADD COLUMN birth_date character varying(36);