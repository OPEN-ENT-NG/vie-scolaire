-- Constraint: duplicate_coteaching

-- ALTER TABLE viesco.multi_teaching DROP CONSTRAINT duplicate_coteaching;

ALTER TABLE viesco.multi_teaching
    ADD CONSTRAINT duplicate_coteaching UNIQUE (structure_id, main_teacher_id, second_teacher_id, class_or_group_id, start_date, is_coteaching);
