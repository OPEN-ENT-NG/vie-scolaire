CREATE TABLE presences.users
(
  id character varying(36) NOT NULL,
  username character varying(255),
  CONSTRAINT users_pk PRIMARY KEY (id)
);

CREATE TABLE presences.groups
(
  id character varying(36) NOT NULL,
  name character varying(255),
  CONSTRAINT groups_pk PRIMARY KEY (id)
);

CREATE TABLE presences.members
(
  id character varying(36) NOT NULL,
  user_id character varying(36),
  group_id character varying(36),
  CONSTRAINT members_pk PRIMARY KEY (id),
  CONSTRAINT fk_group_id FOREIGN KEY (group_id)
  REFERENCES presences.groups (id) MATCH SIMPLE
  ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_users_id FOREIGN KEY (user_id)
  REFERENCES presences.users (id) MATCH SIMPLE
  ON UPDATE CASCADE ON DELETE CASCADE
);

-- functions and triggers
CREATE OR REPLACE FUNCTION presences.insert_users_members() RETURNS trigger AS
$$
BEGIN
  IF (TG_OP = 'INSERT') THEN
    INSERT INTO presences.members (id, user_id) VALUES (NEW.id, NEW.id);
    RETURN NEW;
  END IF;
  RETURN NULL;
END;
$$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION presences.insert_groups_members() RETURNS trigger AS
$$
BEGIN
  IF (TG_OP = 'INSERT') THEN
    INSERT INTO presences.members (id, group_id) VALUES (NEW.id, NEW.id);
    RETURN NEW;
  END IF;
  RETURN NULL;
END;
$$
LANGUAGE plpgsql;

CREATE TRIGGER users_trigger
AFTER INSERT
  ON presences.users
FOR EACH ROW
EXECUTE PROCEDURE presences.insert_users_members();

CREATE TRIGGER groups_trigger
AFTER INSERT
  ON presences.groups
FOR EACH ROW
EXECUTE PROCEDURE presences.insert_groups_members();

CREATE OR REPLACE FUNCTION presences.merge_users(key character varying, data character varying) RETURNS void AS
$$
BEGIN
  LOOP
    UPDATE presences.users SET username = data WHERE id = key;
    IF found THEN
      RETURN;
    END IF;
    BEGIN
      INSERT INTO presences.users(id,username) VALUES (key, data);
      RETURN;
      EXCEPTION WHEN unique_violation THEN
    END;
  END LOOP;
END;
$$
LANGUAGE plpgsql;

CREATE TABLE viesco.cours
(
  id bigserial NOT NULL,
  id_etablissement character varying(36),
  timestamp_dt timestamp without time zone,
  timestamp_fn timestamp without time zone,
  salle character varying(42),
  id_matiere character varying(36),
  edt_classe character varying(36),
  edt_date character varying(36),
  edt_salle character varying(36),
  edt_matiere character varying(36),
  edt_id_cours character varying(36),
  id_classe character varying(36),
  id_personnel character varying(36),
  CONSTRAINT cours_pkey PRIMARY KEY (id)
);

CREATE SCHEMA presences;
CREATE EXTENSION IF NOT EXISTS unaccent;

CREATE TABLE presences.etablissements_actifs
(
  id_etablissement character varying(36) NOT NULL,
  actif boolean NOT NULL DEFAULT true,
  CONSTRAINT etablissement_actifs_pk PRIMARY KEY (id_etablissement)
);

CREATE TABLE presences.type_evt
(
  id bigserial NOT NULL,
  id_etablissemet character varying(36),
  libelle character varying,
  CONSTRAINT type_evt_pk PRIMARY KEY (id)
);

CREATE TABLE presences.motif
(
  id bigserial NOT NULL,
  id_etablissement character varying(36),
  libelle character varying(150),
  justifiant boolean,
  commentaire character varying(250),
  defaut boolean,
  CONSTRAINT motif_pk PRIMARY KEY (id)
);

CREATE TABLE presences.etat_appel
(
  id bigserial NOT NULL,
  id_etablissement character varying(36),
  libelle character varying,
  CONSTRAINT etat_appel_pk PRIMARY KEY (id)
);

CREATE TABLE presences.justificatif_appel
(
  id bigserial NOT NULL,
  libelle character varying,
  id_etablissement character varying(36),
  CONSTRAINT justificatif_appel_pk PRIMARY KEY (id)
);

CREATE TABLE presences.appel
(
  id bigserial NOT NULL,
  id_personnel character varying(36),
  id_cours bigint,
  id_etat bigint,
  id_justificatif bigint,
  owner character varying(36),
  created timestamp without time zone,
  modified timestamp without time zone,
  CONSTRAINT appel_pk PRIMARY KEY (id),
  CONSTRAINT fk_cours_id FOREIGN KEY (id_cours)
      REFERENCES viesco.cours (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT fk_etat_appel FOREIGN KEY (id_etat)
      REFERENCES presences.etat_appel (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_justificatif_appel_id FOREIGN KEY (id_justificatif)
      REFERENCES presences.justificatif_appel (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE presences.creneaux
(
  id bigserial NOT NULL,
  id_etablissement character varying(36),
  timestamp_dt timestamp without time zone,
  timestamp_fn timestamp without time zone,
  CONSTRAINT creneaux_pk PRIMARY KEY (id)
);

CREATE TABLE presences.absence_prev
(
  id bigserial NOT NULL,
  timestamp_dt timestamp without time zone,
  timestamp_fn timestamp without time zone,
  id_eleve character varying(36),
  id_motif bigint,
  CONSTRAINT absence_prev_pk PRIMARY KEY (id),
  CONSTRAINT fk_motif_id FOREIGN KEY (id_motif)
      REFERENCES presences.motif (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE presences.pj
(
  id bigserial NOT NULL,
  doc character varying(36),
  CONSTRAINT pj_pk PRIMARY KEY (id)
);

CREATE TABLE presences.restrictions_matieres
(
  id_absence_prev bigint,
  id_matiere character varying(36),
  CONSTRAINT fk_absence_prev FOREIGN KEY (id_absence_prev)
      REFERENCES presences.absence_prev (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE presences.evenement
(
  id bigserial NOT NULL,
  timestamp_arrive timestamp without time zone,
  timestamp_depart timestamp without time zone,
  commentaire character varying,
  saisie_cpe boolean,
  id_eleve character varying(36),
  id_appel bigint,
  id_type bigint,
  id_pj bigint,
  id_motif bigint,
  owner character varying(36),
  created timestamp without time zone,
  modified timestamp without time zone,
  CONSTRAINT evenement_pk PRIMARY KEY (id),
  CONSTRAINT fk_appel_id FOREIGN KEY (id_appel)
      REFERENCES presences.appel (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT fk_motif_id FOREIGN KEY (id_motif)
      REFERENCES presences.motif (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT fk_pj_id FOREIGN KEY (id_pj)
      REFERENCES presences.pj (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_type_evt_id FOREIGN KEY (id_type)
      REFERENCES presences.type_evt (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE
);

CREATE TABLE presences.evenement_hist
(
  id bigserial NOT NULL,
  timestamp_arrive timestamp without time zone,
  timestamp_depart timestamp without time zone,
  commentaire character varying,
  saisie_cpe boolean,
  id_eleve character varying(36),
  id_appel bigint,
  id_type bigint,
  id_pj bigint,
  id_personnel bigint,
  id_evenement bigint,
  timestamp_hist timestamp without time zone,
  CONSTRAINT evenement_hist_pk PRIMARY KEY (id),
  CONSTRAINT fk_appel_id FOREIGN KEY (id_appel)
      REFERENCES presences.appel (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT fk_evenement_id FOREIGN KEY (id_evenement)
      REFERENCES presences.evenement (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT fk_pj_id FOREIGN KEY (id_pj)
      REFERENCES presences.pj (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT fk_type_evt_id FOREIGN KEY (id_type)
      REFERENCES presences.type_evt (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE
);

CREATE TABLE presences.se_produit_sur
(
  id_evenement bigserial NOT NULL,
  id_creneaux bigint NOT NULL,
  CONSTRAINT se_produit_sur_pk PRIMARY KEY (id_evenement, id_creneaux),
  CONSTRAINT fk_creneaux_id FOREIGN KEY (id_creneaux)
      REFERENCES presences.creneaux (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT fk_evenement_id FOREIGN KEY (id_evenement)
      REFERENCES presences.evenement (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE
);