ALTER TABLE notes.domaines
    ADD COLUMN dispensable boolean NOT NULL DEFAULT FALSE ;

CREATE TABLE notes.dispense_domaine_eleve (
  id_eleve character varying(50) NOT NULL,
  id_domaines bigint NOT NULL,
	dispense boolean,
  CONSTRAINT dispense_domaine_eleve_pkey PRIMARY KEY (id_eleve,id_domaines),
	CONSTRAINT fk_domaines_id FOREIGN KEY (id_domaines)
	REFERENCES notes.domaines (id) MATCH SIMPLE
	ON UPDATE NO ACTION ON DELETE NO ACTION
);