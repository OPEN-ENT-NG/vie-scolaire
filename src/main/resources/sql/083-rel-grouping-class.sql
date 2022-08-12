CREATE TABLE viesco.rel_grouping_class
(
    grouping_id character varying(36) NOT NULL,
    class_id    character varying(36),
    group_id    character varying(36),
    created_at  timestamp without time zone NOT NULL DEFAULT now(),
    updated_at  timestamp without time zone NOT NULL DEFAULT now(),
    FOREIGN KEY (grouping_id) REFERENCES viesco.grouping(id),
    CONSTRAINT rel_grouping_class_unique_class_or_group
    CHECK ((group_id IS NOT NULL OR class_id IS NOT NULL) AND NOT (group_id IS NOT NULL AND class_id IS NOT NULL))
);

CREATE FUNCTION rel_grouping_update() RETURNS trigger AS $rel_grouping_update$
BEGIN
    NEW.updated_at := now();
    RETURN NEW;
END;
$rel_grouping_update$ LANGUAGE plpgsql;

CREATE TRIGGER TR_UPD_REL_GROUPING
AFTER UPDATE ON viesco.rel_grouping_class
FOR EACH ROW EXECUTE PROCEDURE rel_grouping_update();