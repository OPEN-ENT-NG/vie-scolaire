DO $$
BEGIN
    EXECUTE 'ALTER TABLE viesco.time_slots DISABLE TRIGGER reset_time_slots';
EXCEPTION WHEN undefined_object THEN
    -- Trigger n'existe pas, on ignore
    NULL;
END$$;
