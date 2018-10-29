ALTER TABLE viesco.periode
  ADD COLUMN date_conseil_classe timestamp without time zone ,
  ADD COLUMN publication_bulletin boolean NOT NULL DEFAULT false;
UPDATE viesco.periode SET date_conseil_classe = date_fin_saisie ;
ALTER TABLE viesco.periode
ALTER COLUMN date_conseil_classe SET NOT NULL;