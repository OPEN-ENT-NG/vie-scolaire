CREATE SCHEMA notes;

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

CREATE TABLE notes.dispense
(
  id bigint NOT NULL,
  libelle character varying(255),
  description text,
  CONSTRAINT dispense_pk PRIMARY KEY (id)
);

CREATE TABLE notes.enseignements
(
  id bigserial NOT NULL,
  nom character varying(255),
  CONSTRAINT enseignements_pk PRIMARY KEY (id)
);

CREATE TABLE notes.competences
(
  id bigint NOT NULL,
  nom text NOT NULL,
  description text,
  id_parent integer,
  id_type integer NOT NULL,
  id_enseignement integer,
  owner character varying(36),
  created timestamp without time zone,
  modified timestamp without time zone,
  CONSTRAINT competences_pk PRIMARY KEY (id),
  CONSTRAINT fk_enseignements_id FOREIGN KEY (id_enseignement)
      REFERENCES notes.enseignements (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE notes.etat
(
  id bigserial NOT NULL,
  libelle character varying(255),
  CONSTRAINT etat_pk PRIMARY KEY (id)
);


CREATE TABLE notes.typecompetences
(
  id bigserial NOT NULL,
  nom character varying(255),
  CONSTRAINT typecompetences_pk PRIMARY KEY (id)
);

CREATE TABLE notes.type
(
  id bigserial NOT NULL,
  nom character varying(255),
  id_etablissement character varying(255),
  default_type boolean,
  CONSTRAINT type_pk PRIMARY KEY (id)
);

CREATE TABLE notes.devoirs
(
  id bigint NOT NULL,
  name character varying(255),
  owner character varying(36) NOT NULL,
  created timestamp without time zone DEFAULT now() NOT NULL,
  modified timestamp without time zone DEFAULT now() NOT NULL,
  coefficient numeric,
  libelle character varying(255),
  id_classe character varying(255) NOT NULL,
  id_sousmatiere bigint,
  id_periode bigint NOT NULL,
  id_type bigint NOT NULL,
  id_etablissement character varying(255) NOT NULL,
  id_etat bigint NOT NULL,
  diviseur integer NOT NULL,
  id_matiere character varying(255),
  ramenersur boolean,
  datepublication date,
  date date,
  CONSTRAINT devoirs_pk PRIMARY KEY (id),
  CONSTRAINT fk_types_id FOREIGN KEY (id_type)
      REFERENCES notes.type (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_periode_id FOREIGN KEY (id_periode)
      REFERENCES viesco.periode (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_etat_id FOREIGN KEY (id_etat)
      REFERENCES notes.etat (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_sousmatiere_id FOREIGN KEY (id_sousmatiere)
      REFERENCES notes.type_sousmatiere (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT owner_fk FOREIGN KEY (owner)
      REFERENCES notes.users (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE notes.shares
(
  member_id character varying(36) NOT NULL,
  resource_id bigint NOT NULL,
  action character varying(255) NOT NULL,
  CONSTRAINT share PRIMARY KEY (member_id, resource_id, action),
  CONSTRAINT fk_member_id FOREIGN KEY (member_id)
      REFERENCES notes.members (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_resource_id FOREIGN KEY (resource_id)
      REFERENCES notes.devoirs (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE notes.notes
(
  id bigint NOT NULL,
  id_eleve character varying(255) NOT NULL,
  id_devoir bigint NOT NULL,
  valeur numeric NOT NULL,
  id_dispense bigint,
  owner character varying(255),
  modified timestamp without time zone,
  created timestamp without time zone,
  appreciation text,
  CONSTRAINT notes_pk PRIMARY KEY (id),
  CONSTRAINT fk_devoirs_id FOREIGN KEY (id_devoir)
      REFERENCES notes.devoirs (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_dispense_id FOREIGN KEY (id_dispense)
      REFERENCES notes.dispense (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE notes.competences_notes
(
  id bigint NOT NULL,
  id_devoir integer,
  id_competence integer,
  evaluation integer,
  owner character varying(36),
  id_eleve character(36),
  created timestamp without time zone,
  modified timestamp without time zone,
  CONSTRAINT competences_notes_pk PRIMARY KEY (id),
  CONSTRAINT fk_competence_id FOREIGN KEY (id_competence)
      REFERENCES notes.competences (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_devoirs_id FOREIGN KEY (id_devoir)
      REFERENCES notes.devoirs (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE notes.competences_devoirs
(
  id bigint NOT NULL,
  id_devoir integer,
  id_competence integer,
  owner character varying(36),
  created timestamp without time zone,
  modified timestamp without time zone,
  CONSTRAINT competences_devoirs_pk PRIMARY KEY (id),
  CONSTRAINT fk_competence_id FOREIGN KEY (id_competence)
      REFERENCES notes.competences (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_devoirs_id FOREIGN KEY (id_devoir)
      REFERENCES notes.devoirs (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
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


-- index
CREATE INDEX idx_compretences_idparent ON notes.competences USING btree(idparent);
CREATE INDEX competences_notes_id_devoir_id_eleve_idx ON notes.competences_notes USING btree(iddevoir, ideleve);

CREATE INDEX "fki_FK type" ON notes.devoirs USING btree(idtype);
CREATE INDEX "fki_foreignEtat" ON notes.devoirs USING btree(idetat);
CREATE INDEX "fki_foreignPeriode" ON notes.devoirs USING btree(idperiode);

CREATE INDEX "fki_foreignDispense" ON notes.notes USING btree(iddispense);