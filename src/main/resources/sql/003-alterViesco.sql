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

-- Rajout de la table bilan de fin de cycle : Chef établissement

CREATE TABLE notes.bilan_fin_cycle  (
    id bigserial NOT NULL,
    id_eleve character varying(255) NOT NULL,
    owner character varying(255) NOT NULL,
    id_etablissement character varying(36) NOT NULL,
    valeur real,
    id_domaine bigint,
    modified timestamp without time zone,
    created timestamp without time zone,
    CONSTRAINT bilan_fin_cycle_pk PRIMARY KEY (id),
    CONSTRAINT bilan_fin_cycle_unique UNIQUE (id_eleve, id_domaine, id_etablissement),
    CONSTRAINT fk_domaines_id FOREIGN KEY (id_domaine)
    REFERENCES notes.domaines (id) MATCH SIMPLE
    ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE notes.etablissements_actifs  (
    id_etablissement character varying(36) NOT NULL,
    actif boolean NOT NULL DEFAULT true,
    CONSTRAINT etablissement_actifs_pk PRIMARY KEY (id_etablissement)
);
