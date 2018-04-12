-- Tables  d'aides à la saisie d'éléments du programme


CREATE TABLE notes.domaine_enseignement
(
  id bigint NOT NULL,
  id_cycle bigint,
  libelle character varying(255),
  CONSTRAINT aide_saisie_domaines_pk PRIMARY KEY (id),
  CONSTRAINT fk_cycle_id FOREIGN KEY (id_cycle)  REFERENCES notes.cycle (id) MATCH SIMPLE  ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE notes.sous_domaine_enseignement
(
  id bigint NOT NULL,
  libelle character varying(255),
  id_domaine bigint,
  CONSTRAINT aide_saisie_sous_domaines_pk PRIMARY KEY (id),
  CONSTRAINT fk_domaine_id FOREIGN KEY (id_domaine)  REFERENCES notes.domaine_enseignement (id) MATCH SIMPLE  ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE notes.proposition
(
  id bigint NOT NULL,
  libelle character varying(255),
  id_sous_domaine bigint,
  CONSTRAINT aide_saisie_proprosition_pk PRIMARY KEY (id),
  CONSTRAINT fk_sous_domaine_id FOREIGN KEY (id_sous_domaine)  REFERENCES notes.sous_domaine_enseignement (id) MATCH SIMPLE  ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE notes.element_programme
(
  texte character varying(1000),
  id_classe character varying(36) NOT NULL,
  id_periode bigint NOT NULL,
  id_matiere character varying(36) NOT NULL,
  id_user_create character varying(36),
  id_user_update character varying(36),
  CONSTRAINT element_programme_pk PRIMARY KEY (id_classe, id_periode, id_matiere),
  CONSTRAINT fk_periode_id FOREIGN KEY (id_periode) REFERENCES viesco.periode (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE
);
