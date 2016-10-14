CREATE SCHEMA notes;

-- tables
CREATE TABLE notes.users
(
  id character varying(36) NOT NULL,
  username character varying(255),
  CONSTRAINT users_pkey PRIMARY KEY (id)
);

CREATE TABLE notes.groups
(
  id character varying(36) NOT NULL,
  name character varying(255),
  CONSTRAINT groups_pkey PRIMARY KEY (id)
);

CREATE TABLE notes.members
(
  id character varying(36) NOT NULL,
  user_id character varying(36),
  group_id character varying(36),
  CONSTRAINT members_pkey PRIMARY KEY (id),
  CONSTRAINT group_fk FOREIGN KEY (group_id)
      REFERENCES notes.groups (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT user_fk FOREIGN KEY (user_id)
      REFERENCES notes.users (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE notes.matiere
(
  id bigserial NOT NULL,
  evaluable boolean,
  matiere character varying(255) NOT NULL,
  idetablissement character varying(255),
  idprofesseur character varying(255),
  CONSTRAINT matiere_pkey PRIMARY KEY (id)
);

CREATE TABLE notes.dispense
(
  id bigserial NOT NULL,
  libelle character varying(255),
  description text,
  CONSTRAINT dispense_pkey PRIMARY KEY (id)
);

CREATE TABLE notes.periode
(
  id bigserial NOT NULL,
  libelle character varying(255),
  datedebut date,
  datefin date,
  idetablissement character varying(255),
  CONSTRAINT periode_pkey PRIMARY KEY (id)
);

CREATE TABLE notes.enseignements
(
  id bigserial NOT NULL,
  nom character varying(255),
  CONSTRAINT "PK enseignements" PRIMARY KEY (id)
);

CREATE TABLE notes.competences
(
  id bigserial NOT NULL,
  nom text NOT NULL,
  description text,
  idparent bigint,
  idtype bigint NOT NULL,
  idenseignement bigint,
  owner character varying(36),
  created timestamp without time zone,
  modified timestamp without time zone,
  CONSTRAINT "PK competence" PRIMARY KEY (id),
  CONSTRAINT "FK enseignements" FOREIGN KEY (idenseignement)
      REFERENCES notes.enseignements (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE notes.etat
(
  id bigserial NOT NULL,
  libelle character varying(255),
  CONSTRAINT pketat PRIMARY KEY (id)
);

CREATE TABLE notes.typesousmatiere
(
  id bigserial NOT NULL,
  libelle character varying(255),
  CONSTRAINT "PK" PRIMARY KEY (id)
);

CREATE TABLE notes.typecompetences
(
  id bigserial NOT NULL,
  nom character varying(255),
  CONSTRAINT typecompetences_pkey PRIMARY KEY (id)
);

CREATE TABLE notes.type
(
  id bigserial NOT NULL,
  nom character varying(255),
  idetablissement character varying(255),
  "default" boolean,
  CONSTRAINT "PKTYPE" PRIMARY KEY (id)
);

CREATE TABLE notes.sousmatiere
(
  id bigserial NOT NULL,
  id_typesousmatiere bigint NOT NULL,
  id_matiere character varying(255) NOT NULL,
  CONSTRAINT sousmatiere_pkey PRIMARY KEY (id),
  CONSTRAINT sousmatiere_id_typesousmatiere_fkey FOREIGN KEY (id_typesousmatiere)
      REFERENCES notes.typesousmatiere (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
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
  idclasse character varying(255) NOT NULL,
  idsousmatiere bigint,
  idperiode bigint NOT NULL,
  idtype bigint NOT NULL,
  idetablissement character varying(255) NOT NULL,
  idetat bigint NOT NULL,
  diviseur integer NOT NULL,
  idmatiere character varying(255),
  ramenersur boolean,
  datepublication date,
  date date,
  CONSTRAINT devoirs_pkey PRIMARY KEY (id),
  CONSTRAINT "FK type" FOREIGN KEY (idtype)
      REFERENCES notes.type (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT "fkPeriode" FOREIGN KEY (idperiode)
      REFERENCES notes.periode (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT "foreignEtat" FOREIGN KEY (idetat)
      REFERENCES notes.etat (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT "foreignSousMatiere" FOREIGN KEY (idsousmatiere)
      REFERENCES notes.typesousmatiere (id) MATCH SIMPLE
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
  CONSTRAINT member_fk FOREIGN KEY (member_id)
      REFERENCES notes.members (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT resource_fk FOREIGN KEY (resource_id)
      REFERENCES notes.devoirs (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE notes.notes
(
  id bigserial NOT NULL,
  ideleve character varying(255) NOT NULL,
  iddevoir bigint NOT NULL,
  valeur numeric NOT NULL,
  iddispense bigint,
  owner character varying(255),
  modified timestamp without time zone,
  created timestamp without time zone,
  appreciation text,
  CONSTRAINT notes_pkey PRIMARY KEY (id),
  CONSTRAINT "foreignDevoir" FOREIGN KEY (iddevoir)
      REFERENCES notes.devoirs (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT "foreignDispense" FOREIGN KEY (iddispense)
      REFERENCES notes.dispense (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE notes.competences_notes
(
  id bigserial NOT NULL,
  iddevoir bigint,
  idcompetence bigint,
  evaluation integer,
  owner character varying(36),
  ideleve character(36),
  created timestamp without time zone,
  modified timestamp without time zone,
  CONSTRAINT "PK competences notes" PRIMARY KEY (id),
  CONSTRAINT "FK competence" FOREIGN KEY (idcompetence)
      REFERENCES notes.competences (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT "FK devoir" FOREIGN KEY (iddevoir)
      REFERENCES notes.devoirs (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE notes.competences_devoirs
(
  id bigserial NOT NULL,
  iddevoir bigint,
  idcompetence bigint,
  owner character varying(36),
  created timestamp without time zone,
  modified timestamp without time zone,
  CONSTRAINT "PK devoirs competences" PRIMARY KEY (id),
  CONSTRAINT "FK competence" FOREIGN KEY (idcompetence)
      REFERENCES notes.competences (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT "FK devoirs" FOREIGN KEY (iddevoir)
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

CREATE INDEX sousmatiere_id_typesousmatiere_idx ON notes.sousmatiere USING btree(id_typesousmatiere, id_matiere);