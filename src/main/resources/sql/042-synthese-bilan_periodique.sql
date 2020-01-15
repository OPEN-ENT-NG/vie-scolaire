CREATE TABLE notes.synthese_bilan_periodique
(
  synthese character varying(600),
  id bigserial NOT NULL,
  id_typePeriode bigint NOT NULL,
  id_eleve character varying(255) NOT NULL,
  id_etablissement character varying(255) NOT NULL,
  CONSTRAINT synthese_bilan_periodique_pk PRIMARY KEY (id_typePeriode, id_eleve,id_etablissement)
);