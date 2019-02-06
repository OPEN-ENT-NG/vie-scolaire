CREATE TABLE viesco.setting_period (
  id bigserial NOT NULL,
  start_date timestamp without time zone NOT NULL,
  end_date timestamp without time zone NOT NULL,
  description character varying,
  id_structure character varying(36) NOT NULL,
  code character varying UNIQUE,
  is_opening boolean NOT NULL,
  CONSTRAINT period_exclusion_pkey PRIMARY KEY (id)
);