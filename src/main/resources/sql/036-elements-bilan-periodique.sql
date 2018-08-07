
CREATE TABLE notes.type_elt_bilan_periodique (
  id bigserial NOT NULL,
	nom character varying(255) NOT NULL,
	code character varying(255) NOT NULL,
  CONSTRAINT type_elt_bilan_periodique_pk PRIMARY KEY (id)
);

INSERT INTO notes.type_elt_bilan_periodique(nom, code) VALUES ('Enseignements pratiques interdisciplinaires', 'EPI');
INSERT INTO notes.type_elt_bilan_periodique(nom, code) VALUES ('Accompagnement personnalisé', 'AP');
INSERT INTO notes.type_elt_bilan_periodique(nom, code) VALUES ('Parcours', 'Parcours');

CREATE TABLE notes.thematique_bilan_periodique (
  id bigserial NOT NULL,
	libelle character varying(150) NOT NULL,
  code character varying(7) NOT NULL,
  type_elt_bilan_periodique bigint NOT NULL,
  CONSTRAINT thematique_bilan_periodique_pk PRIMARY KEY (id),
  CONSTRAINT fk_type_elt_bilan_periodique FOREIGN KEY (type_elt_bilan_periodique)
  REFERENCES notes.type_elt_bilan_periodique (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION
);

INSERT INTO notes.thematique_bilan_periodique(libelle, code, type_elt_bilan_periodique) VALUES ('Parcours avenir', 'PAR_AVN', 3);
INSERT INTO notes.thematique_bilan_periodique(libelle, code, type_elt_bilan_periodique) VALUES ('Parcours citoyen', 'PAR_CIT', 3);
INSERT INTO notes.thematique_bilan_periodique(libelle, code, type_elt_bilan_periodique) VALUES ('Parcours d''éducation artistique et culturelle', 'PAR_ART', 3);
INSERT INTO notes.thematique_bilan_periodique(libelle, code, type_elt_bilan_periodique) VALUES ('Parcours éducatif de santé', 'PAR_SAN', 3);

INSERT INTO notes.thematique_bilan_periodique(libelle, code, type_elt_bilan_periodique) VALUES ('Corps, santé, bien-être et sécurité', 'EPI_SAN', 1);
INSERT INTO notes.thematique_bilan_periodique(libelle, code, type_elt_bilan_periodique) VALUES ('Culture et création artistiques', 'EPI_ART', 1);
INSERT INTO notes.thematique_bilan_periodique(libelle, code, type_elt_bilan_periodique) VALUES ('Transition écologique et développement durable', 'EPI_EDD', 1);
INSERT INTO notes.thematique_bilan_periodique(libelle, code, type_elt_bilan_periodique) VALUES ('Information, communication, citoyenneté', 'EPI_EDD', 1);
INSERT INTO notes.thematique_bilan_periodique(libelle, code, type_elt_bilan_periodique) VALUES ('Langues et cultures de l''Antiquité', 'EPI_LGA', 1);
INSERT INTO notes.thematique_bilan_periodique(libelle, code, type_elt_bilan_periodique) VALUES ('Langues et cultures étrangères ou, le cas échéant, régionales', 'EPI_LGE', 1);
INSERT INTO notes.thematique_bilan_periodique(libelle, code, type_elt_bilan_periodique) VALUES ('Monde économique et professionnel', 'EPI_PRO', 1);
INSERT INTO notes.thematique_bilan_periodique(libelle, code, type_elt_bilan_periodique) VALUES ('Sciences, technologie et société', 'EPI_STS', 1);

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
  id_matiere character varying(36) NOT NULL,
  CONSTRAINT elt_bilan_period_interv_mat_unique UNIQUE (id_elt_bilan_periodique, id_intervenant, id_matiere),
  CONSTRAINT fk_elt_bilan_periodique_id FOREIGN KEY (id_elt_bilan_periodique)
  REFERENCES notes.elt_bilan_periodique (id) MATCH SIMPLE
  ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE notes.rel_elt_bilan_periodique_groupe (
  id_elt_bilan_periodique bigint NOT NULL,
  id_groupe character varying(255) NOT NULL,
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
  CONSTRAINT appreciation_elt_bilan_period_eleve_unique UNIQUE (id_elt_bilan_periodique, id_eleve),
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
  commentaire character varying(600),
  CONSTRAINT appreciation_elt_bilan_period_classe_pk PRIMARY KEY (id),
  CONSTRAINT fk_elt_bilan_periodique_id FOREIGN KEY (id_elt_bilan_periodique)
  REFERENCES notes.elt_bilan_periodique (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_id_periode FOREIGN KEY (id_periode)
  REFERENCES viesco.periode (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION
);