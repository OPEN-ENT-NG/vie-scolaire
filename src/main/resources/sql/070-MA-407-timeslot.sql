ALTER TABLE viesco.time_slots
    ADD COLUMN end_of_half_day TIME without time zone;

CREATE TABLE viesco.slots
(
    id character varying (36),
    structure_id character varying (36),
    name text,
    start_hour TIME without time zone,
    end_hour TIME without time zone,
    CONSTRAINT fk_structure_id FOREIGN KEY (structure_id) REFERENCES viesco.time_slots (id_structure)
);

-- reset time slot's default end of half day and reset new slot
CREATE OR REPLACE FUNCTION viesco.reset_time_slots() RETURNS TRIGGER AS
    $BODY$
    BEGIN
		UPDATE viesco.time_slots SET end_of_half_day = null;
		DELETE from viesco.slots;
		RETURN OLD;
    END
    $BODY$
LANGUAGE plpgsql;

CREATE TRIGGER reset_time_slots AFTER UPDATE OF id ON viesco.time_slots
FOR EACH ROW EXECUTE PROCEDURE viesco.reset_time_slots();
