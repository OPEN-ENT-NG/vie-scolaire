BEGIN;
-- DROP FROREIGN KEYS AND COLUMN id_user CONSTRAINTS ON REL TABLES
   ALTER TABLE viesco.rel_structures_personne_supp DROP CONSTRAINT IF EXISTS personnes_supp_fk;
   ALTER TABLE viesco.rel_groupes_personne_supp DROP CONSTRAINT IF EXISTS fk_personnes_supp_id;
   ALTER TABLE viesco.personnes_supp DROP CONSTRAINT IF EXISTS personnes_supp_pk;
   ALTER TABLE viesco.rel_structures_personne_supp DROP COLUMN id_user;
   ALTER TABLE viesco.rel_groupes_personne_supp DROP COLUMN id_user;;


-- CREATE COLUMN ID
   ALTER TABLE viesco.personnes_supp ADD COLUMN id BIGSERIAL NOT NULL;
   ALTER TABLE viesco.rel_structures_personne_supp ADD COLUMN id BIGINT NOT NULL;
   ALTER TABLE viesco.rel_groupes_personne_supp ADD COLUMN id BIGINT NOT NULL;


-- NEW CONSTRAINT
   ALTER TABLE viesco.personnes_supp ADD CONSTRAINT personnes_supp_pk PRIMARY KEY (id);
   ALTER TABLE viesco.rel_structures_personne_supp ADD CONSTRAINT personnes_supp_fk FOREIGN KEY (id)
        REFERENCES viesco.personnes_supp (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE;
	ALTER TABLE viesco.rel_groupes_personne_supp ADD CONSTRAINT fk_personnes_supp_id FOREIGN KEY (id)
      REFERENCES viesco.personnes_supp (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE;


-- CREATE INDEX TO SPEED REQUEST
CREATE INDEX id_user_idx ON viesco.personnes_supp (id_user);
CREATE INDEX user_type_idx ON viesco.personnes_supp (user_type);
CREATE INDEX delete_date_idx ON viesco.personnes_supp (delete_date);
CREATE INDEX id_user_delete_date_idx ON viesco.personnes_supp (id_user,delete_date);
END;