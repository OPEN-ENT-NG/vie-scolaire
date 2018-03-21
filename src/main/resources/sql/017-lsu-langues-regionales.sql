CREATE SEQUENCE notes.langues_culture_regionale_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

CREATE TABLE notes.langues_culture_regionale (
    id bigint NOT NULL DEFAULT nextval('notes.langues_culture_regionale_id_seq'),
    libelle character varying(50) NOT NULL,
    code character varying(3)  NOT NULL,
    CONSTRAINT lcr_pkey PRIMARY KEY (id)
);

ALTER TABLE notes.eleve_enseignement_complement ADD COLUMN id_langue bigint DEFAULT NULL;
ALTER TABLE notes.eleve_enseignement_complement ADD CONSTRAINT fk_langue_id FOREIGN KEY (id_langue) REFERENCES notes.langues_culture_regionale (id);