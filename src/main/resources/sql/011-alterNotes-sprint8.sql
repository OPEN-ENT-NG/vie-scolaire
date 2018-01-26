-- Gestion ordre d'affichage des compétences sur un devoir (MN-462)
ALTER TABLE notes.competences_devoirs
    ADD COLUMN index bigint;

-- Gestion affichage de la moyenne du domaine sur le BFC (MN-468)
CREATE TABLE notes.visibilite_moyenne_bfc
(
    id_etablissement character varying(36) NOT NULL,
    visible boolean NOT NULL DEFAULT false,
    CONSTRAINT visibilite_moyenne_bfc_pk PRIMARY KEY (id_etablissement)
)