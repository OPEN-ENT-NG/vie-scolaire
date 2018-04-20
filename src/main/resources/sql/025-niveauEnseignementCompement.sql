CREATE TABLE notes.niveau_ens_complement
(
	id bigint NOT NULL,
	libelle character varying(50) NOT NULL,
	niveau int NOT NULL,
	bareme_brevet int NOT NULL,
	CONSTRAINT niveau_ens_compl_pk PRIMARY KEY(id)

);

INSERT INTO notes.niveau_ens_complement (id,libelle,niveau,bareme_brevet)
VALUES (1,'Objectif atteint',1,10),(2,'Objectif dépassé',2,20);

ALTER TABLE notes.niveau_competences ADD bareme_brevet int ;

UPDATE notes.niveau_competences SET bareme_brevet = ordre * 10;

ALTER TABLE notes.eleve_enseignement_complement RENAME COLUMN "niveau" TO "id_niveau";
ALTER TABLE notes.eleve_enseignement_complement ADD CONSTRAINT fk_niveau_id FOREIGN KEY(id_niveau)
 REFERENCES notes.niveau_ens_complement (id) MATCH SIMPLE
 ON UPDATE NO ACTION ON DELETE NO ACTION;