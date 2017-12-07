ALTER TABLE notes.domaines ADD code_domaine character varying(8);

ALTER TABLE notes.cycle ADD value_cycle bigint;

CREATE SEQUENCE notes.bfc_synthese_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

CREATE SEQUENCE notes.eleve_enscpl_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

CREATE SEQUENCE notes.enscpl_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

CREATE TABLE notes.enseignement_complement (
    id bigint NOT NULL DEFAULT nextval('notes.enscpl_id_seq'),
    libelle character varying(50) NOT NULL,
    code character varying(3)  NOT NULL,
    CONSTRAINT enscpl_pkey PRIMARY KEY (id)
);

CREATE TABLE notes.eleve_enseignement_complement
(
    id bigint NOT NULL DEFAULT nextval('notes.eleve_enscpl_id_seq'),
    id_eleve character varying(36) NOT NULL,
    id_enscpl bigint NOT NULL,
    niveau bigint,
    owner character varying(36) NOT NULL,
    CONSTRAINT eleve_enscpl_pk PRIMARY KEY (id),
    CONSTRAINT unique_ideleve UNIQUE (id_eleve),
    CONSTRAINT fk_enscpl_id FOREIGN KEY (id_enscpl)
        REFERENCES notes.enseignement_complement (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);


CREATE TABLE notes.bfc_synthese
(
    id bigint NOT NULL DEFAULT nextval('notes.bfc_synthese_id_seq'),
    id_eleve character varying(36) NOT NULL,
    owner character varying(36) NOT NULL,
    id_cycle bigint NOT NULL,
    texte character varying(1500) NOT NULL,
    date_creation timestamp without time zone NOT NULL DEFAULT now(),
    modified timestamp without time zone,
    CONSTRAINT id_bfc_synthese PRIMARY KEY (id),
    CONSTRAINT unique_ideleve_idcycle UNIQUE (id_eleve, id_cycle),
    CONSTRAINT fk_cycle_id FOREIGN KEY (id_cycle)
        REFERENCES notes.cycle (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);