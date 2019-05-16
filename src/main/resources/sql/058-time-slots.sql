CREATE TABLE viesco.time_slots (
  id_structure character varying(36) NOT NULL,
  id character varying(36) NOT NULL,
  CONSTRAINT unique_idStructure UNIQUE (id_structure)
);