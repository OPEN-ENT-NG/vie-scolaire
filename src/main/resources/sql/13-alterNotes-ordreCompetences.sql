CREATE TABLE notes.perso_order_item_enseignement
(
    id_competence integer NOT NULL,
    id_etablissement character varying(36)  NOT NULL,
    id_enseignement integer NOT NULL,
    index bigint,
    CONSTRAINT order_items_etablissement_pk PRIMARY KEY (id_competence, id_etablissement, id_enseignement),
    CONSTRAINT fk_rel_competences_enseignement_id FOREIGN KEY (id_competence, id_enseignement)
        REFERENCES notes.rel_competences_enseignements (id_competence, id_enseignement) MATCH SIMPLE
        ON UPDATE CASCADE ON DELETE CASCADE
);