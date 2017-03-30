-- Rajout de la date de fin de saisie sur les periodes
ALTER TABLE viesco.periode
 ADD date_fin_saisie timestamp without time zone;

-- Gestion des personnes supprimées
CREATE TABLE viesco.personnes_supp
(
  id_user character varying(36) NOT NULL,
  display_name character varying,
  date_suppression timestamp without time zone DEFAULT now(),
  CONSTRAINT personnes_supp_pk PRIMARY KEY (id_user)
);

CREATE TABLE viesco.rel_groupes_personne_supp
(
  id_groupe character varying(36),
  id_user character varying(36),
  type_groupe integer,
  user_type character varying,
  CONSTRAINT fk_personnes_supp_id FOREIGN KEY (id_user)
      REFERENCES viesco.personnes_supp (id_user) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE
);

CREATE TABLE viesco.rel_structures_personne_supp
(
  id_structure character varying(36),
  id_user character varying(36),
  CONSTRAINT personnes_supp_fk FOREIGN KEY (id_user)
  REFERENCES viesco.personnes_supp (id_user) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE CASCADE
);