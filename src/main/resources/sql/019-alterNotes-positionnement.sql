CREATE TABLE notes.positionnement
(
    id_periode bigint NOT NULL,
    id_eleve character varying COLLATE pg_catalog."default" NOT NULL,
    positionnement INTEGER NOT NULL,
    id_classe character varying COLLATE pg_catalog."default" NOT NULL,
    id_matiere character varying COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT pk_positionnement PRIMARY KEY (id_periode, id_classe, id_matiere, id_eleve),
    CONSTRAINT fk_id_periode FOREIGN KEY (id_periode)
        REFERENCES viesco.periode (id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE
);