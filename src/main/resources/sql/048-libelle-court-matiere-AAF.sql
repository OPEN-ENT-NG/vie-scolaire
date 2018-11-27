CREATE TABLE notes.matiere
(
id BIGSERIAL NOT NULL,
code character varying(10) NOT NULL,
libelle_court character varying(10),
libelle_long character varying(100),
PRIMARY KEY (id),
CONSTRAINT id_unique UNIQUE (id),
CONSTRAINT code_unique UNIQUE (code)
);








