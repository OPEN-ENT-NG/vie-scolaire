CREATE SCHEMA abs;

-- tables
CREATE TABLE abs.creneaux
(
  creneaux_id bigserial NOT NULL,
  fk4j_etab_id uuid,
  creneaux_timestamp_dt timestamp without time zone,
  creneaux_timestamp_fn timestamp without time zone,
  CONSTRAINT creneaux_pkey PRIMARY KEY (creneaux_id)
);

CREATE TABLE abs.etat_appel
(
  etat_appel_id bigserial NOT NULL,
  fk4j_etab_id uuid,
  etat_appel_libelle character varying(42),
  CONSTRAINT etat_appel_pkey PRIMARY KEY (etat_appel_id)
);

CREATE TABLE abs.justificatif_appel
(
  justificatif_appel_id bigserial NOT NULL,
  fk4j_etab_id uuid,
  justificatif_appel_libelle character varying(42),
  CONSTRAINT justificatif_appel_pkey PRIMARY KEY (justificatif_appel_id)
);

CREATE TABLE abs.users
(
  id character varying(36) NOT NULL,
  username character varying(255),
  CONSTRAINT users_pkey PRIMARY KEY (id)
);

CREATE TABLE abs.type_evt
(
  type_evt_id bigserial NOT NULL,
  fk4j_etab_id uuid,
  type_evt_libelle character varying(42),
  CONSTRAINT type_evt_pkey PRIMARY KEY (type_evt_id)
);

CREATE TABLE abs.pj
(
  pj_id bigserial NOT NULL,
  pj_doc character varying(250),
  CONSTRAINT pj_pkey PRIMARY KEY (pj_id)
);

CREATE TABLE abs.motif
(
  motif_id bigserial NOT NULL,
  fk4j_etab_id uuid,
  motif_libelle character varying(150),
  motif_justifiant boolean,
  motif_commentaire character varying(250),
  motif_defaut boolean,
  CONSTRAINT motif_pkey PRIMARY KEY (motif_id)
);

CREATE TABLE abs.appel
(
  id bigserial NOT NULL,
  fk_personnel_id bigint,
  fk_cours_id bigint,
  fk_etat_appel_id bigint,
  fk_justificatif_appel_id bigint,
  owner character varying(36),
  created timestamp without time zone,
  modified timestamp without time zone,
  CONSTRAINT appel_pkey PRIMARY KEY (id),
  CONSTRAINT appel_fk_cours_id_fkey FOREIGN KEY (fk_cours_id)
      REFERENCES viesco.cours (cours_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT appel_fk_etat_appel_id_fkey FOREIGN KEY (fk_etat_appel_id)
      REFERENCES abs.etat_appel (etat_appel_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT appel_fk_justificatif_applel_id_fkey FOREIGN KEY (fk_justificatif_appel_id)
      REFERENCES abs.justificatif_appel (justificatif_appel_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT appel_fk_personnel_id_fkey FOREIGN KEY (fk_personnel_id)
      REFERENCES viesco.personnel (personnel_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE abs.absence_prev
(
  absence_prev_id bigserial NOT NULL,
  absence_prev_restriction_matiere character varying(42),
  absence_prev_timestamp_dt timestamp without time zone,
  absence_prev_timestamp_fn timestamp without time zone,
  fk_eleve_id bigint,
  fk_motif_id bigint,
  CONSTRAINT absence_prev_pkey PRIMARY KEY (absence_prev_id),
  CONSTRAINT absence_prev_fk_eleve_id_fkey FOREIGN KEY (fk_eleve_id)
      REFERENCES viesco.eleve (eleve_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT absence_prev_fk_motif_id_fkey FOREIGN KEY (fk_motif_id)
      REFERENCES abs.motif (motif_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE abs.evenement
(
  id bigserial NOT NULL,
  evenement_timestamp_arrive timestamp without time zone,
  evenement_timestamp_depart timestamp without time zone,
  evenement_commentaire character varying(250),
  evenement_saisie_cpe boolean,
  fk_eleve_id bigint,
  fk_appel_id bigint,
  fk_type_evt_id bigint,
  fk_pj_pj bigint,
  fk_motif_id bigint,
  owner character varying(36),
  created timestamp without time zone,
  modified timestamp without time zone,
  CONSTRAINT evenement_pkey PRIMARY KEY (id),
  CONSTRAINT evenement_fk_appel_id_fkey FOREIGN KEY (fk_appel_id)
      REFERENCES abs.appel (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT evenement_fk_eleve_id_fkey FOREIGN KEY (fk_eleve_id)
      REFERENCES viesco.eleve (eleve_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT evenement_fk_motif_id_fkey FOREIGN KEY (fk_motif_id)
      REFERENCES abs.motif (motif_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT evenement_fk_pj_pj_fkey FOREIGN KEY (fk_pj_pj)
      REFERENCES abs.pj (pj_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT evenement_fk_type_evt_id_fkey FOREIGN KEY (fk_type_evt_id)
      REFERENCES abs.type_evt (type_evt_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE abs.evenement_hist
(
  evenement_hist_id bigserial NOT NULL,
  fk_personnel_id bigint,
  fk_evenement_id bigint,
  evenement_hist_description character varying(42),
  evenement_hist_detail character varying(1024),
  evenement_hist_timestamp_mod timestamp without time zone,
  CONSTRAINT evenement_hist_pkey PRIMARY KEY (evenement_hist_id),
  CONSTRAINT evenement_hist_fk_evenement_id_fkey FOREIGN KEY (fk_evenement_id)
      REFERENCES abs.evenement (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT evenement_hist_fk_personnel_id_fkey FOREIGN KEY (fk_personnel_id)
      REFERENCES viesco.personnel (personnel_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE abs.se_produit_sur
(
  evenement_id bigserial NOT NULL,
  creneaux_id bigint NOT NULL,
  CONSTRAINT se_produit_sur_pkey PRIMARY KEY (evenement_id, creneaux_id),
  CONSTRAINT se_produit_sur_creneaux_id_fkey FOREIGN KEY (creneaux_id)
      REFERENCES abs.creneaux (creneaux_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT se_produit_sur_evenement_id_fkey FOREIGN KEY (evenement_id)
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