CREATE TABLE notes.modalites
(
  libelle character varying(100),
  id      character not null,
  CONSTRAINT pk_modalite PRIMARY KEY (id)
);

INSERT INTO notes.modalites (id, libelle) VALUES ('S', 'Tronc commun');
INSERT INTO notes.modalites (id, libelle) VALUES ('O', 'Option obligatoire');
INSERT INTO notes.modalites (id, libelle) VALUES ('F', 'Option facultative');
INSERT INTO notes.modalites (id, libelle) VALUES ('L', 'Ajout académique au programme');
INSERT INTO notes.modalites (id, libelle) VALUES ('R', 'Enseignement religieux');
INSERT INTO notes.modalites (id, libelle) VALUES ('X', 'Mesure spécifique');


CREATE TABLE notes.services
(
  id_etablissement  character varying(36) NOT NULL,
  id_enseignant     character varying(36) NOT NULL,
  id_matiere        character varying(36) NOT NULL,
  id_groupe         character varying(36) NOT NULL,
  modalite          character varying NOT NULL DEFAULT 'S',
  evaluable         boolean NOT NULL DEFAULT TRUE,
  ordre             character varying,
  CONSTRAINT pk_services PRIMARY KEY (id_enseignant, id_matiere, id_groupe),
  CONSTRAINT fk_modalite FOREIGN KEY (modalite)
  REFERENCES notes.modalites (id) MATCH SIMPLE
  ON UPDATE CASCADE ON DELETE CASCADE
);