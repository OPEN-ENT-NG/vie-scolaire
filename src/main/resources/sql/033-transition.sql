CREATE OR REPLACE FUNCTION notes.clone_schema(source_schema text, dest_schema text) RETURNS void AS
$BODY$
DECLARE
  objeto text;
  buffer text;
BEGIN
  IF NOT EXISTS(
      SELECT schema_name
      FROM information_schema.schemata
      WHERE schema_name = dest_schema
  )
  THEN
    EXECUTE 'CREATE SCHEMA ' || dest_schema ;

    FOR objeto IN
    SELECT TABLE_NAME::text FROM information_schema.TABLES WHERE table_schema = source_schema
    LOOP
      buffer := dest_schema || '.' || objeto;
      EXECUTE 'CREATE TABLE ' || buffer || ' (LIKE ' || source_schema || '.' || objeto || ' INCLUDING CONSTRAINTS INCLUDING INDEXES INCLUDING DEFAULTS)';
      EXECUTE 'INSERT INTO ' || buffer || '(SELECT * FROM ' || source_schema || '.' || objeto || ')';
    END LOOP;
  END IF;
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

CREATE TABLE notes.transition
(
  id_etablissement character varying(36),
  date  TIMESTAMP WITHOUT TIME ZONE DEFAULT now()
);

ALTER TABLE notes.devoirs DROP CONSTRAINT IF EXISTS fk_periode_id ;
ALTER TABLE notes.devoirs DROP CONSTRAINT IF EXISTS fk_devoirs_type_periode;
ALTER TABLE notes.devoirs ADD CONSTRAINT fk_devoirs_type_periode FOREIGN KEY (id_periode)
REFERENCES viesco.rel_type_periode (id) MATCH SIMPLE
ON UPDATE NO ACTION ON DELETE NO ACTION;

