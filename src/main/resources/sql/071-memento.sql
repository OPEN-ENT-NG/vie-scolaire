CREATE SCHEMA memento;
CREATE EXTENSION IF NOT EXISTS unaccent;

CREATE TABLE memento.comments(
    id bigserial,
    student character varying (36) NOT NULL,
    owner character varying (36) NOT NULL,
    comment text,
    CONSTRAINT comments_pkey PRIMARY KEY (id),
    CONSTRAINT uniq_comment UNIQUE(owner, student)
);