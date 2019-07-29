CREATE TABLE notes.arhive_bulletins_complet
(
  id_etablissement character varying NOT NULL,
  date_archive     TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
  CONSTRAINT arhive_bulletins_complet_pk PRIMARY KEY (id_etablissement)
);