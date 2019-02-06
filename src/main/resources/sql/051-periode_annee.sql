CREATE TABLE viesco.periode_annee
(
    id bigserial NOT NULL,
    start_date timestamp without time zone NOT NULL,
    end_date timestamp without time zone NOT NULL,
    id_structure character varying(36) NOT NULL,
    CONSTRAINT periode_annee_pkey PRIMARY KEY (id_structure)
);