CREATE TABLE viesco.rel_cours_groupes (
  id_groupe character varying(36) NOT NULL,
  id_cours bigint NOT NULL,
  CONSTRAINT rel_cours_groupes_pk PRIMARY KEY (id_groupe, id_cours),
  CONSTRAINT fk_cours_id FOREIGN KEY (id_cours)
        REFERENCES viesco.cours (id) MATCH FULL
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE viesco.rel_cours_users (
  id_user character varying(36) NOT NULL,
  id_cours bigint NOT NULL,
  CONSTRAINT rel_cours_users_pk PRIMARY KEY (id_user, id_cours),
  CONSTRAINT fk_cours_id FOREIGN KEY (id_cours)
        REFERENCES viesco.cours (id) MATCH FULL
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

INSERT INTO viesco.rel_cours_groupes SELECT id_classe,id FROM viesco.cours where id_classe IS NOT NULL;
INSERT INTO viesco.rel_cours_users SELECT id_personnel,id FROM viesco.cours where id_personnel IS NOT NULL;

-- ALTER TABLE viesco.cours DROP COLUMN id_classe;
-- ALTER TABLE viesco.cours DROP COLUMN id_personnel;