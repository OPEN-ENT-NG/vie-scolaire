CREATE TABLE notes.appreciation_classe
(
  appreciation character varying(300),
  id_classe character varying(36) NOT NULL,
  id_periode bigint NOT NULL,
  id_matiere character varying(36) NOT NULL,
  CONSTRAINT appreciation_classe_pk PRIMARY KEY (id_classe, id_periode, id_matiere),
  CONSTRAINT fk_periode_id FOREIGN KEY (id_periode) REFERENCES viesco.periode (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE
);