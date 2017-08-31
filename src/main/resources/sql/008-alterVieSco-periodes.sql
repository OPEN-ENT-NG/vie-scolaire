ALTER TABLE viesco.periode
    ADD COLUMN id_classe character(36) NOT NULL,
    ADD COLUMN id_type bigint NOT NULL,
    ALTER timestamp_dt SET NOT NULL,
    ALTER timestamp_fn SET NOT NULL,
    ALTER date_fin_saisie SET NOT NULL,
    DROP COLUMN libelle,
    ADD CONSTRAINT fk_type_periode FOREIGN KEY (id_type)
    REFERENCES viesco.rel_type_periode (id) MATCH SIMPLE
    ON UPDATE NO ACTION ON DELETE NO ACTION;


CREATE TABLE viesco.rel_type_periode
(
    id BIGSERIAL NOT NULL,
    type bigint NOT NULL,
    ordre bigint NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT id_unique UNIQUE (id),
    CONSTRAINT type_ordre_unique UNIQUE (type, ordre)
);


INSERT INTO viesco.rel_type_periode (type, ordre) VALUES (2, 1);
INSERT INTO viesco.rel_type_periode (type, ordre) VALUES (2, 2);
INSERT INTO viesco.rel_type_periode (type, ordre) VALUES (3, 1);
INSERT INTO viesco.rel_type_periode (type, ordre) VALUES (3, 2);
INSERT INTO viesco.rel_type_periode (type, ordre) VALUES (3, 3);