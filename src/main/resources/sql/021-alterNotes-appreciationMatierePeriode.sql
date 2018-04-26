CREATE TABLE notes.appreciation_matiere_periode
(
    id_periode bigint NOT NULL,
    id_eleve character varying NOT NULL,
    appreciation_matiere_periode character varying(300) NOT NULL,
    id_classe character varying NOT NULL,
    id_matiere character varying NOT NULL,
    CONSTRAINT pk_appreciation_matiere_periode PRIMARY KEY (id_periode, id_classe, id_matiere, id_eleve),
    CONSTRAINT fk_id_periode FOREIGN KEY (id_periode)
        REFERENCES viesco.periode (id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE
);