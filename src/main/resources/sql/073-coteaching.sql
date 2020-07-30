CREATE TABLE viesco.multi_teaching
(
  id bigserial NOT NULL,
  structure_id character varying(36) NOT NULL,
  main_teacher_id character varying(36) NOT NULL,
  second_teacher_id character varying(36) NOT NULL,
  subject_id character varying(36) NOT NULL,
  class_or_group_id character varying(36) NOT NULL,
  start_date timestamp without time zone,
  end_date timestamp without time zone,
  entered_end_date timestamp without time zone,
  is_coteaching boolean DEFAULT FALSE,
  CONSTRAINT multi_teaching_pkey PRIMARY KEY (id)
);

CREATE TABLE viesco.modalites
(
  libelle character varying(100),
  id      character not null,
  CONSTRAINT pk_modalite PRIMARY KEY (id)
);

INSERT INTO viesco.modalites
SELECT * FROM notes.modalites;


CREATE TABLE viesco.services
(
  id_etablissement  character varying(36) NOT NULL,
  id_enseignant     character varying(36) NOT NULL,
  id_matiere        character varying(36) NOT NULL,
  id_groupe         character varying(36) NOT NULL,
  modalite          character varying NOT NULL DEFAULT 'S',
  evaluable         boolean NOT NULL DEFAULT FALSE,
  ordre             character varying,
  coefficient       INTEGER DEFAULT (1),
  CONSTRAINT pk_services PRIMARY KEY (id_enseignant, id_matiere, id_groupe),
  CONSTRAINT fk_modalite FOREIGN KEY (modalite)
  REFERENCES viesco.modalites (id) MATCH SIMPLE
  ON UPDATE CASCADE ON DELETE CASCADE
);
CREATE INDEX serviceIdStruct on viesco.services(id_etablissement) ;
CREATE Index serviceIdTeacher on viesco.services(id_enseignant) ;

 INSERT INTO viesco.services
SELECT * FROM notes.services;
