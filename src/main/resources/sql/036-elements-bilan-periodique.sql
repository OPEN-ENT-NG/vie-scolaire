
CREATE TABLE notes.type_elt_bilan_periodique (
  id bigserial NOT NULL,
	nom character varying(255) NOT NULL,
	code character varying(255) NOT NULL,
  CONSTRAINT type_elt_bilan_periodique_pk PRIMARY KEY (id)
);

CREATE TABLE notes.thematique_bilan_periodique (
  id bigserial NOT NULL,
	libelle character varying(150) NOT NULL,
  code character varying(50) NOT NULL,
  type_elt_bilan_periodique bigint NOT NULL,
  personnalise boolean NOT NULL,
  CONSTRAINT thematique_bilan_periodique_pk PRIMARY KEY (id),
  CONSTRAINT fk_type_elt_bilan_periodique FOREIGN KEY (type_elt_bilan_periodique)
  REFERENCES notes.type_elt_bilan_periodique (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE notes.elt_bilan_periodique (
  id bigserial NOT NULL,
	intitule character varying(150),
  id_thematique bigint,
  description character varying(600),
  type_elt_bilan_periodique bigint NOT NULL,
  id_etablissement character varying(36) NOT NULL,
  CONSTRAINT elt_bilan_periodique_pk PRIMARY KEY (id),
  CONSTRAINT fk_type_elt_bilan_periodique FOREIGN KEY (type_elt_bilan_periodique)
  REFERENCES notes.type_elt_bilan_periodique (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_thematique_bilan_periodique FOREIGN KEY (id_thematique)
  REFERENCES notes.thematique_bilan_periodique (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE notes.rel_elt_bilan_periodique_intervenant_matiere (
  id_elt_bilan_periodique bigint NOT NULL,
  id_intervenant character varying(255) NOT NULL,
  id_matiere character varying(36),
  CONSTRAINT elt_bilan_period_interv_mat_unique UNIQUE (id_elt_bilan_periodique, id_intervenant, id_matiere),
  CONSTRAINT fk_elt_bilan_periodique_id FOREIGN KEY (id_elt_bilan_periodique)
  REFERENCES notes.elt_bilan_periodique (id) MATCH SIMPLE
  ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE notes.rel_elt_bilan_periodique_groupe (
  id_elt_bilan_periodique bigint NOT NULL,
  id_groupe character varying(255) NOT NULL,
  externalId_groupe character varying(255) NOT NULL,
  CONSTRAINT elt_bilan_period_groupe_unique UNIQUE (id_elt_bilan_periodique, id_groupe),
  CONSTRAINT fk_elt_bilan_periodique_id FOREIGN KEY (id_elt_bilan_periodique)
  REFERENCES notes.elt_bilan_periodique (id) MATCH SIMPLE
  ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE notes.appreciation_elt_bilan_periodique_eleve (
  id bigserial NOT NULL,
  id_eleve character varying(255) NOT NULL,
  id_elt_bilan_periodique bigint NOT NULL,
  id_periode bigint NOT NULL,
  commentaire character varying(600),
  CONSTRAINT appreciation_elt_bilan_period_eleve_pk PRIMARY KEY (id),
  CONSTRAINT appreciation_elt_bilan_period_eleve_unique UNIQUE (id_elt_bilan_periodique, id_eleve, id_periode),
  CONSTRAINT fk_elt_bilan_periodique_id FOREIGN KEY (id_elt_bilan_periodique)
  REFERENCES notes.elt_bilan_periodique (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_id_periode FOREIGN KEY (id_periode)
  REFERENCES viesco.periode (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE notes.appreciation_elt_bilan_periodique_classe (
  id bigserial NOT NULL,
  id_elt_bilan_periodique bigint NOT NULL,
  id_periode bigint NOT NULL,
  id_groupe character varying(255) NOT NULL,
  externalid_groupe character varying(255) NOT NULL,
  commentaire character varying(600),
  CONSTRAINT appreciation_elt_bilan_period_classe_pk PRIMARY KEY (id),
  CONSTRAINT appreciation_elt_bilan_period_classe_unique UNIQUE (id_elt_bilan_periodique, id_periode, id_groupe),
  CONSTRAINT fk_elt_bilan_periodique_id FOREIGN KEY (id_elt_bilan_periodique)
  REFERENCES notes.elt_bilan_periodique (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_id_periode FOREIGN KEY (id_periode)
  REFERENCES viesco.periode (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- CREATE TABLE notes.rel_groupe_appreciation_elt_classe (
--   id_groupe character varying(255) NOT NULL,
--   externalId_groupe character varying(255) NOT NULL,
--   id_elt_bilan_periodique bigint NOT NULL,
--   id_periode bigint NOT NULL,
--   CONSTRAINT groupe_appreciation_elt_classe_unique UNIQUE (id_groupe, id_elt_bilan_periodique, id_periode)
-- );

CREATE TABLE notes.rel_groupe_appreciation_elt_eleve (
  id_groupe character varying(255) NOT NULL,
  externalId_groupe character varying(255) NOT NULL,
  id_elt_bilan_periodique bigint NOT NULL,
  id_periode bigint NOT NULL,
  id_eleve character varying(255) NOT NULL,
  CONSTRAINT groupe_appreciation_elt_eleve_unique UNIQUE (id_groupe, id_elt_bilan_periodique, id_periode, id_eleve)
);