CREATE TABLE presences.dispense
(
    id bigserial NOT NULL,
    id_eleve character varying(100) NOT NULL,
	id_etablissement character varying(100) NOT NULL,
    id_matiere character varying(100) NOT NULL,
    date_debut date NOT NULL,
    date_fin date NOT NULL,
    commentaire character varying(500),
    presence boolean NOT NULL,
    PRIMARY KEY (id)
)