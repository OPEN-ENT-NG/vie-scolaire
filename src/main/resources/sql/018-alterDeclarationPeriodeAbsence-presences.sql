CREATE TABLE presences.attachments_declaration
(
    id_declaration BIGINT NOT NULL,
    id_attachment character varying(36) NOT NULL,
	CONSTRAINT fk_declaration_id
        FOREIGN KEY (id_declaration)
        REFERENCES presences.declaration(id)
);

ALTER TABLE presences.declaration
ADD COLUMN fullday_dt BOOLEAN DEFAULT FALSE;

ALTER TABLE presences.declaration
ADD COLUMN fullday_fn BOOLEAN DEFAULT FALSE;