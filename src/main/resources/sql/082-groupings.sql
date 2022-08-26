CREATE TABLE viesco.grouping
(
    id character varying(36) NOT NULL,
    name character varying(36),
    structure_id character varying(36),
    created_at timestamp without time zone DEFAULT now(),
    updated_at timestamp without time zone DEFAULT now(),
    CONSTRAINT grouping_pkey PRIMARY KEY (id)
);

CREATE FUNCTION viesco.grouping_date_update() RETURNS trigger AS $grouping_date_update$
BEGIN
    NEW.updated_at := now();
RETURN NEW;
END;
$grouping_date_update$ LANGUAGE plpgsql;



CREATE TRIGGER TR_UPD_REL_GROUPING
AFTER UPDATE ON viesco.grouping
FOR EACH ROW EXECUTE PROCEDURE viesco.grouping_date_update();
