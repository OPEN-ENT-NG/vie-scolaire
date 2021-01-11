CREATE TABLE viesco.trombinoscope_settings (
     structure_id character varying (36),
     active boolean NOT NULL,
     CONSTRAINT trombinoscope_settings_pkey PRIMARY KEY (structure_id)
);

CREATE TABLE viesco.trombinoscope(
    id bigserial,
    student_id character varying (36) NOT NULL,
    structure_id character varying (36) NOT NULL,
    picture_id character varying (36) NOT NULL,
    created_at timestamp without time zone DEFAULT now(),
    CONSTRAINT uniq_structure_student UNIQUE (structure_id, student_id)
);

CREATE TABLE viesco.trombinoscope_failure(
    id bigserial,
    path text,
    structure_id character varying (36) NOT NULL,
    picture_id character varying (36),
    created_at timestamp without time zone DEFAULT now()
);

CREATE OR REPLACE FUNCTION viesco.delete_trombinoscope_failure() RETURNS TRIGGER AS
    $BODY$
   DECLARE

    BEGIN
        DELETE FROM viesco.trombinoscope_failure WHERE picture_id = NEW.picture_id;
        RETURN NEW;
    END
    $BODY$
LANGUAGE plpgsql;

CREATE TRIGGER delete_viesco_trombinoscope_failure AFTER INSERT ON viesco.trombinoscope
    FOR EACH ROW EXECUTE PROCEDURE viesco.delete_trombinoscope_failure();

