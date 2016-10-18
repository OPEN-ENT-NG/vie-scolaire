CREATE SCHEMA abs;

-- tables
CREATE TABLE abs.creneaux
(
  id bigint NOT NULL,
  id_etablissement uuid,
  timestamp_dt timestamp without time zone,
  timestamp_fn timestamp without time zone,
  CONSTRAINT creneaux_pk PRIMARY KEY (id)
);

CREATE TABLE abs.etat_appel
(
  id bigint NOT NULL,
  id_etablissement uuid,
  libelle character varying(42),
  CONSTRAINT etat_appel_pk PRIMARY KEY (id)
);

CREATE TABLE abs.justificatif_appel
(
  id bigint NOT NULL,
  id_etablissement uuid,
  libelle character varying(42),
  CONSTRAINT justificatif_appel_pk PRIMARY KEY (id)
);

CREATE TABLE abs.users
(
  id character varying(36) NOT NULL,
  username character varying(255),
  CONSTRAINT users_pk PRIMARY KEY (id)
);

CREATE TABLE abs.type_evt
(
  id bigint NOT NULL,
  id_etablissement uuid,
  libelle character varying(42),
  CONSTRAINT type_evt_pk PRIMARY KEY (id)
);

CREATE TABLE abs.pj
(
  id bigint NOT NULL,
  doc character varying(250),
  CONSTRAINT pj_pk PRIMARY KEY (id)
);

CREATE TABLE abs.motif
(
  id bigint NOT NULL,
  id_etablissement uuid,
  libelle character varying(150),
  justifiant boolean,
  commentaire character varying(250),
  default_motif boolean,
  CONSTRAINT motif_pk PRIMARY KEY (id)
);

CREATE TABLE abs.appel
(
  id bigint NOT NULL,
  fk_personnel_id bigint,
  fk_cours_id bigint,
  fk_etat_appel_id bigint,
  fk_justificatif_appel_id bigint,
  owner character varying(36),
  created timestamp without time zone,
  modified timestamp without time zone,
  CONSTRAINT appel_pk PRIMARY KEY (id),
  CONSTRAINT fk_cours_id FOREIGN KEY (fk_cours_id)
      REFERENCES viesco.cours (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_etat_appel_id FOREIGN KEY (fk_etat_appel_id)
      REFERENCES abs.etat_appel (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_justificatif_appel_id FOREIGN KEY (fk_justificatif_appel_id)
      REFERENCES abs.justificatif_appel (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_personnel_id FOREIGN KEY (fk_personnel_id)
      REFERENCES viesco.personnel (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE abs.absence_prev
(
  id bigint NOT NULL,
  restriction_matiere character varying(42),
  timestamp_dt timestamp without time zone,
  timestamp_fn timestamp without time zone,
  fk_eleve_id bigint,
  fk_motif_id bigint,
  CONSTRAINT absence_prev_pk PRIMARY KEY (id),
  CONSTRAINT fk_eleve_id FOREIGN KEY (fk_eleve_id)
      REFERENCES viesco.eleve (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_motif_id FOREIGN KEY (fk_motif_id)
      REFERENCES abs.motif (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE abs.evenement
(
  id bigint NOT NULL,
  timestamp_arrive timestamp without time zone,
  timestamp_depart timestamp without time zone,
  commentaire character varying(250),
  saisie_cpe boolean,
  fk_eleve_id bigint,
  fk_appel_id bigint,
  fk_type_evt_id bigint,
  fk_pj_id bigint,
  fk_motif_id bigint,
  owner character varying(36),
  created timestamp without time zone,
  modified timestamp without time zone,
  CONSTRAINT evenement_pk PRIMARY KEY (id),
  CONSTRAINT fk_appel_id FOREIGN KEY (fk_appel_id)
      REFERENCES abs.appel (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_eleve_id FOREIGN KEY (fk_eleve_id)
      REFERENCES viesco.eleve (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_motif_id FOREIGN KEY (fk_motif_id)
      REFERENCES abs.motif (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_pj_id FOREIGN KEY (fk_pj_id)
      REFERENCES abs.pj (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_type_evt_id FOREIGN KEY (fk_type_evt_id)
      REFERENCES abs.type_evt (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE abs.evenement_hist
(
  id bigint NOT NULL,
  fk_personnel_id bigint,
  fk_evenement_id bigint,
  description character varying(42),
  detail character varying(1024),
  timestamp_mod timestamp without time zone,
  CONSTRAINT evenement_hist_pk PRIMARY KEY (id),
  CONSTRAINT fk_evenement_id FOREIGN KEY (fk_evenement_id)
      REFERENCES abs.evenement (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_personnel_id FOREIGN KEY (fk_personnel_id)
      REFERENCES viesco.personnel (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE abs.se_produit_sur
(
  fk_evenement_id bigint NOT NULL,
  fk_creneaux_id bigint NOT NULL,
  CONSTRAINT se_produit_sur_pk PRIMARY KEY (fk_evenement_id, fk_creneaux_id),
  CONSTRAINT fk_creneaux_id FOREIGN KEY (fk_creneaux_id)
      REFERENCES abs.creneaux (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_evenement_id FOREIGN KEY (fk_evenement_id)
      REFERENCES abs.evenement (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- functions
CREATE OR REPLACE FUNCTION abs.merge_users(key character varying, data character varying) RETURNS void AS
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