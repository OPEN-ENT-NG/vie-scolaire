ALTER TABLE viesco.periode ADD CONSTRAINT periode_uk UNIQUE (id_etablissement, id_classe, id_type);