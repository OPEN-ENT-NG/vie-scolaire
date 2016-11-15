CREATE SCHEMA abs;

-- tables
CREATE TABLE abs.creneaux
(
  id bigserial NOT NULL,
  id_etablissement character varying(36),
  timestamp_dt timestamp without time zone,
  timestamp_fn timestamp without time zone,
  CONSTRAINT creneaux_pk PRIMARY KEY (id)
);

CREATE TABLE abs.etat_appel
(
  id bigserial NOT NULL,
  id_etablissement character varying(36),
  libelle character varying(42),
  CONSTRAINT etat_appel_pk PRIMARY KEY (id)
);

CREATE TABLE abs.justificatif_appel
(
  id bigserial NOT NULL,
  id_etablissement character varying(36),
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
  id bigserial NOT NULL,
  id_etablissement character varying(36),
  libelle character varying(42),
  CONSTRAINT type_evt_pk PRIMARY KEY (id)
);

CREATE TABLE abs.pj
(
  id bigserial NOT NULL,
  doc character varying(250),
  CONSTRAINT pj_pk PRIMARY KEY (id)
);

CREATE TABLE abs.motif
(
  id bigserial NOT NULL,
  id_etablissement character varying(36),
  libelle character varying(150),
  justifiant boolean,
  commentaire character varying(250),
  default_motif boolean,
  CONSTRAINT motif_pk PRIMARY KEY (id)
);

CREATE TABLE abs.appel
(
  id bigserial NOT NULL,
  id_personnel bigint,
  id_cours bigint,
  id_etat_appel bigint,
  id_justificatif_appel bigint,
  owner character varying(36),
  created timestamp without time zone,
  modified timestamp without time zone,
  CONSTRAINT appel_pk PRIMARY KEY (id),
  CONSTRAINT fk_cours_id FOREIGN KEY (id_cours)
  REFERENCES viesco.cours (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_etat_appel_id FOREIGN KEY (id_etat_appel)
  REFERENCES abs.etat_appel (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_justificatif_appel_id FOREIGN KEY (id_justificatif_appel)
  REFERENCES abs.justificatif_appel (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_personnel_id FOREIGN KEY (id_personnel)
  REFERENCES viesco.personnel (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE abs.absence_prev
(
  id bigserial NOT NULL,
  restriction_matiere character varying(42),
  timestamp_dt timestamp without time zone,
  timestamp_fn timestamp without time zone,
  id_eleve bigint,
  id_motif bigint,
  CONSTRAINT absence_prev_pk PRIMARY KEY (id),
  CONSTRAINT fk_eleve_id FOREIGN KEY (id_eleve)
  REFERENCES viesco.eleve (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_motif_id FOREIGN KEY (id_motif)
  REFERENCES abs.motif (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE abs.evenement
(
  id bigserial NOT NULL,
  timestamp_arrive timestamp without time zone,
  timestamp_depart timestamp without time zone,
  commentaire character varying(250),
  saisie_cpe boolean,
  id_eleve bigserial,
  id_appel bigint,
  id_type_evt bigint,
  id_pj bigint,
  id_motif bigint,
  owner character varying(36),
  created timestamp without time zone,
  modified timestamp without time zone,
  CONSTRAINT evenement_pk PRIMARY KEY (id),
  CONSTRAINT fk_appel_id FOREIGN KEY (id_appel)
  REFERENCES abs.appel (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_eleve_id FOREIGN KEY (id_eleve)
  REFERENCES viesco.eleve (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_motif_id FOREIGN KEY (id_motif)
  REFERENCES abs.motif (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_pj_id FOREIGN KEY (id_pj)
  REFERENCES abs.pj (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_type_evt_id FOREIGN KEY (id_type_evt)
  REFERENCES abs.type_evt (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE abs.evenement_hist
(
  id bigserial NOT NULL,
  id_personnel bigint,
  id_evenement bigint,
  description character varying(42),
  detail character varying(1024),
  timestamp_mod timestamp without time zone,
  CONSTRAINT evenement_hist_pk PRIMARY KEY (id),
  CONSTRAINT fk_evenement_id FOREIGN KEY (id_evenement)
  REFERENCES abs.evenement (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_personnel_id FOREIGN KEY (id_personnel)
  REFERENCES viesco.personnel (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE abs.se_produit_sur
(
  id_evenement bigint NOT NULL,
  id_creneaux bigint NOT NULL,
  CONSTRAINT se_produit_sur_pk PRIMARY KEY (id_creneaux, id_evenement),
  CONSTRAINT fk_creneaux_id FOREIGN KEY (id_creneaux)
  REFERENCES abs.creneaux (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_evenement_id FOREIGN KEY (id_evenement)
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