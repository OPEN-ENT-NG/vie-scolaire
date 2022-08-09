CREATE TABLE viesco.rel_grouping_class
(
    grouping_id character varying(36) NOT NULL,
    class_id character varying(36) NOT NULL,
    group_id character varying(36) NOT NULL,
    created_at timestamp without time zone ,
    updated_at timestamp without time zone,
    CONSTRAINT rel_grouping_pkey PRIMARY KEY (grouping_id, class_id, group_id),
    FOREIGN KEY (grouping_id) REFERENCES viesco.grouping(id)
);