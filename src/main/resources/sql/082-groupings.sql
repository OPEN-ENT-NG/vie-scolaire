CREATE TABLE viesco.grouping
(
    id character varying(36) NOT NULL,
    name character varying(36),
    structure_id character varying(36),
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    CONSTRAINT grouping_pkey PRIMARY KEY (id)
);
