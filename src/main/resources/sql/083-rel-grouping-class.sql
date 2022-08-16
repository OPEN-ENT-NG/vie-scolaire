CREATE TABLE viesco.rel_grouping_class
(
    grouping_id character varying(36) NOT NULL,
    student_division_id character varying(36) NOT NULL,
    created_at  timestamp without time zone NOT NULL DEFAULT now(),
    updated_at  timestamp without time zone NOT NULL DEFAULT now(),
    FOREIGN KEY (grouping_id) REFERENCES viesco.grouping(id) ON DELETE CASCADE,
    PRIMARY KEY (grouping_id, student_division_id)
);

CREATE FUNCTION viesco.rel_grouping_update() RETURNS trigger AS $rel_grouping_update$
BEGIN
    NEW.updated_at := now();
    RETURN NEW;
END;
$rel_grouping_update$ LANGUAGE plpgsql;

CREATE TRIGGER TR_UPD_REL_GROUPING
AFTER UPDATE ON viesco.rel_grouping_class
FOR EACH ROW EXECUTE PROCEDURE viesco.rel_grouping_update();
