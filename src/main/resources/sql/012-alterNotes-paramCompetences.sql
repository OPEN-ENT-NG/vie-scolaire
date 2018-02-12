ALTER TABLE notes.competences
    ADD COLUMN id_etablissement CHARACTER VARYING(36);

ALTER TABLE notes.rel_competences_enseignements
  ADD CONSTRAINT uq_rel_competences_enseignements
UNIQUE (id_competence, id_enseignement);

ALTER TABLE notes.rel_competences_domaines
  ADD CONSTRAINT uq_rel_competences_domaines
UNIQUE (id_competence, id_domaine);

CREATE TABLE notes.perso_competences
(
  id_competence     INTEGER NOT NULL,
  id_etablissement  CHARACTER VARYING(36) NOT NULL,
  nom               CHARACTER VARYING(36),
  masque            BOOLEAN DEFAULT FALSE ,
  CONSTRAINT perso_competences_pk PRIMARY KEY (id_competence, id_etablissement),
  CONSTRAINT fk_id_competence FOREIGN KEY (id_competence)
  REFERENCES notes.competences (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION
);

alter table notes.rel_competences_domaines
drop constraint fk_competence_id,
add constraint fk_competence_id
   foreign key (id_competence)
   references notes.competences(id)
   on delete cascade;

alter table notes.rel_competences_enseignements
drop constraint fk_competence_id,
add constraint fk_competence_id
   foreign key (id_competence)
   references notes.competences(id)
   on delete cascade;
