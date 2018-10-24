CREATE TABLE notes.appreciation_CPE_bilan_periodique
(
  appreciation character varying(600),
  id bigserial NOT NULL,
  id_periode bigint NOT NULL,
  id_eleve character varying(255) NOT NULL,
  CONSTRAINT appreciation_CPE_bilan_periodique_pk PRIMARY KEY (id_periode, id_eleve)
);