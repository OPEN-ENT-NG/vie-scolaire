CREATE SCHEMA notes;
CREATE EXTENSION IF NOT EXISTS unaccent;

-- tables
CREATE TABLE notes.users
(
  id character varying(36) NOT NULL,
  username character varying(255),
  CONSTRAINT users_pk PRIMARY KEY (id)
);

CREATE TABLE notes.groups
(
  id character varying(36) NOT NULL,
  name character varying(255),
  CONSTRAINT groups_pk PRIMARY KEY (id)
);

CREATE TABLE notes.members
(
  id character varying(36) NOT NULL,
  user_id character varying(36),
  group_id character varying(36),
  CONSTRAINT members_pk PRIMARY KEY (id),
  CONSTRAINT fk_group_id FOREIGN KEY (group_id)
  REFERENCES notes.groups (id) MATCH SIMPLE
  ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_users_id FOREIGN KEY (user_id)
  REFERENCES notes.users (id) MATCH SIMPLE
  ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE notes.cycle
(
  id bigserial NOT NULL,
  libelle character varying,
  CONSTRAINT cycle_pk PRIMARY KEY (id)
);

CREATE TABLE notes.domaines
(
  id bigserial NOT NULL,
  id_parent bigint,
  id_cycle bigint,
  codification character varying,
  libelle character varying,
  type character varying,
  evaluated boolean NOT NULL DEFAULT false,
  CONSTRAINT domaines_pk PRIMARY KEY (id),
  CONSTRAINT fk_domaines_id_cycle FOREIGN KEY (id_cycle)
  REFERENCES notes.cycle (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE CASCADE
);

CREATE TABLE notes.enseignements
(
  id bigserial NOT NULL,
  nom character varying(255),
  CONSTRAINT enseignements_pk PRIMARY KEY (id)
);

CREATE TABLE notes.competences
(
  id bigserial NOT NULL,
  nom text NOT NULL,
  id_parent integer,
  id_type integer NOT NULL,
  id_cycle bigint DEFAULT 1,
  CONSTRAINT competences_pk PRIMARY KEY (id)
);

CREATE INDEX idx_compretences_idparent ON notes.competences USING btree (id_parent);


CREATE TABLE notes.type
(
  id bigserial NOT NULL,
  nom character varying(255),
  id_etablissement character varying(36),
  default_type boolean,
  CONSTRAINT type_pk PRIMARY KEY (id)
);

CREATE TABLE notes.devoirs
(
  id bigserial NOT NULL,
  name character varying(255),
  owner character varying(36) NOT NULL,
  created timestamp without time zone NOT NULL DEFAULT now(),
  modified timestamp without time zone NOT NULL DEFAULT now(),
  coefficient numeric,
  libelle character varying(255),
  id_sousmatiere bigint,
  id_periode bigint NOT NULL,
  id_type bigint NOT NULL,
  id_etablissement character varying(36) NOT NULL,
  diviseur integer NOT NULL,
  id_matiere character varying(255),
  ramener_sur boolean,
  date_publication date,
  date date,
  is_evaluated boolean DEFAULT true,
  id_etat bigint,
  CONSTRAINT devoirs_pk PRIMARY KEY (id),
  CONSTRAINT fk_periode_id FOREIGN KEY (id_periode)
  REFERENCES viesco.periode (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_sousmatiere_id FOREIGN KEY (id_sousmatiere)
  REFERENCES viesco.type_sousmatiere (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_type_id FOREIGN KEY (id_type)
  REFERENCES notes.type (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT owner_fk FOREIGN KEY (owner)
  REFERENCES notes.users (id) MATCH SIMPLE
  ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE INDEX "fki_FK type" ON notes.devoirs USING btree(id_type);
CREATE INDEX "fki_foreignPeriode" ON notes.devoirs USING btree (id_periode);

CREATE TABLE notes.devoirs_shares
(
  member_id character varying(36) NOT NULL,
  resource_id bigint NOT NULL,
  action character varying(255) NOT NULL,
  CONSTRAINT devoirs_shares_pk PRIMARY KEY (member_id, resource_id, action),
  CONSTRAINT fk_devoirs_id FOREIGN KEY (resource_id)
  REFERENCES notes.devoirs (id) MATCH SIMPLE
  ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE notes.notes
(
  id bigserial NOT NULL,
  id_eleve character varying(255) NOT NULL,
  id_devoir bigint NOT NULL,
  valeur numeric NOT NULL,
  owner character varying(255),
  modified timestamp without time zone,
  created timestamp without time zone,
  CONSTRAINT notes_pk PRIMARY KEY (id),
  CONSTRAINT fk_devoirs_id FOREIGN KEY (id_devoir)
  REFERENCES notes.devoirs (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT elevenote_per_devoir UNIQUE (id_devoir, id_eleve)
);


CREATE INDEX idx_notes_ideleve ON notes.notes USING btree (id_eleve);
CREATE INDEX idx_notes_ideleve_iddevoir ON notes.notes USING btree (id_eleve, id_devoir);

-- Table: notes.appreciations
CREATE TABLE notes.appreciations
(
  id bigserial NOT NULL,
  id_eleve character varying(255) NOT NULL,
  id_devoir bigint NOT NULL,
  valeur text NOT NULL,
  owner character varying(255),
  modified timestamp without time zone,
  created timestamp without time zone,
  CONSTRAINT appreciations_pk PRIMARY KEY (id),
  CONSTRAINT fk_devoirs_id FOREIGN KEY (id_devoir)
      REFERENCES notes.devoirs (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT eleveappreciation_per_devoir UNIQUE (id_devoir, id_eleve)
)
WITH (
  OIDS=FALSE
);
CREATE INDEX idx_appreciations_ideleve  ON notes.appreciations  USING btree  (id_eleve COLLATE pg_catalog."default");

CREATE INDEX idx_appreciations_ideleve_iddevoir  ON notes.appreciations  USING btree
  (id_eleve COLLATE pg_catalog."default", id_devoir);

-- End

CREATE TABLE notes.competences_notes
(
  id bigserial NOT NULL,
  id_devoir integer,
  id_competence integer,
  evaluation integer,
  owner character varying(36),
  id_eleve character(36),
  created timestamp without time zone DEFAULT now(),
  modified timestamp without time zone,
  CONSTRAINT competences_notes_pk PRIMARY KEY (id),
  CONSTRAINT fk_competence_id FOREIGN KEY (id_competence)
  REFERENCES notes.competences (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT fk_devoirs_id FOREIGN KEY (id_devoir)
  REFERENCES notes.devoirs (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE CASCADE
);
CREATE INDEX competences_notes_id_devoir_id_eleve_idx ON notes.competences_notes USING btree(id_devoir, id_eleve);

CREATE TABLE notes.competences_devoirs
(
  id bigserial NOT NULL,
  id_devoir integer,
  id_competence integer,
  CONSTRAINT competences_devoirs_pk PRIMARY KEY (id),
  CONSTRAINT fk_competence_id FOREIGN KEY (id_competence)
  REFERENCES notes.competences (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_devoir_id FOREIGN KEY (id_devoir)
  REFERENCES notes.devoirs (id) MATCH FULL
  ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE notes.rel_competences_domaines
(
  id_competence bigint,
  id_domaine bigint,
  CONSTRAINT fk_competence_id FOREIGN KEY (id_competence)
  REFERENCES notes.competences (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_domaines_id FOREIGN KEY (id_domaine)
  REFERENCES notes.domaines (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE notes.rel_devoirs_groupes
(
  id_groupe character varying(36) NOT NULL,
  id_devoir bigint NOT NULL,
  type_groupe int NOT NULL,
  CONSTRAINT rel_devoirs_groupes_pk PRIMARY KEY (id_groupe, id_devoir),
  CONSTRAINT fk_devoir_id FOREIGN KEY (id_devoir)
  REFERENCES notes.devoirs (id) MATCH FULL
  ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE notes.rel_groupe_cycle
(
  id_groupe character varying(36),
  id_cycle bigint,
  type_groupe int NOT NULL,
  CONSTRAINT rel_groupe_cycle_pk PRIMARY KEY (id_groupe,id_cycle)
);

CREATE TABLE notes.rel_professeurs_remplacants
(
  id_titulaire character varying(36) NOT NULL,
  id_remplacant character varying(36) NOT NULL,
  date_debut timestamp without time zone NOT NULL,
  date_fin timestamp without time zone NOT NULL,
  id_etablissement character varying(36)
);

CREATE TABLE notes.rel_competences_enseignements
(
  id_competence bigint,
  id_enseignement bigint,
  CONSTRAINT fk_competence_id FOREIGN KEY (id_competence)
  REFERENCES notes.competences (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_enseignement_id FOREIGN KEY (id_enseignement)
  REFERENCES notes.enseignements (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION
);
CREATE TABLE notes.niveau_competences
(
  id bigserial NOT NULL UNIQUE,
  libelle character varying(36) NOT NULL,
  ordre integer NOT NULL,
  couleur character varying(36),
  id_cycle bigint NOT NULL,
  CONSTRAINT id PRIMARY KEY (id),
  CONSTRAINT id_cycle FOREIGN KEY (id_cycle)
  REFERENCES notes.cycle (id) MATCH SIMPLE
  ON UPDATE CASCADE ON DELETE CASCADE
);



CREATE TABLE notes.echelle_conversion_niv_note
(
  id bigserial NOT NULL UNIQUE,
  valmin real NOT NULL,
  valmax real,
  id_structure character varying(36),
  id_niveau bigint NOT NULL,
  CONSTRAINT pk_id PRIMARY KEY (id),
  CONSTRAINT id_niveau FOREIGN KEY (id_niveau)
  REFERENCES notes.niveau_competences (id) MATCH SIMPLE
  ON UPDATE RESTRICT ON DELETE RESTRICT
);



-- functions and triggers
CREATE OR REPLACE FUNCTION notes.insert_users_members() RETURNS trigger AS
$$
BEGIN
  IF (TG_OP = 'INSERT') THEN
    INSERT INTO notes.members (id, user_id) VALUES (NEW.id, NEW.id);
    RETURN NEW;
  END IF;
  RETURN NULL;
END;
$$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION notes.insert_groups_members() RETURNS trigger AS
$$
BEGIN
  IF (TG_OP = 'INSERT') THEN
    INSERT INTO notes.members (id, group_id) VALUES (NEW.id, NEW.id);
    RETURN NEW;
  END IF;
  RETURN NULL;
END;
$$
LANGUAGE plpgsql;

CREATE TRIGGER users_trigger
AFTER INSERT
  ON notes.users
FOR EACH ROW
EXECUTE PROCEDURE notes.insert_users_members();

CREATE TRIGGER groups_trigger
AFTER INSERT
  ON notes.groups
FOR EACH ROW
EXECUTE PROCEDURE notes.insert_groups_members();

CREATE OR REPLACE FUNCTION notes.merge_users(key character varying, data character varying) RETURNS void AS
$$
BEGIN
  LOOP
    UPDATE notes.users SET username = data WHERE id = key;
    IF found THEN
      RETURN;
    END IF;
    BEGIN
      INSERT INTO notes.users(id,username) VALUES (key, data);
      RETURN;
      EXCEPTION WHEN unique_violation THEN
    END;
  END LOOP;
END;
$$
LANGUAGE plpgsql;