CREATE TABLE notes.sts_file (
id bigserial NOT NULL,
id_etablissement character varying(50) NOT NULL,
name_file character varying(100) NOT NULL,
content text NOT NULL,
creation_date TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
CONSTRAINT sts_file_history_pkey PRIMARY KEY (id)
);

CREATE INDEX id_id_etab_idx ON notes.sts_file(id DESC, id_etablissement);