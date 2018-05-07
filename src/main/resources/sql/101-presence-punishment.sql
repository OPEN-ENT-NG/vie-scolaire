CREATE TABLE presences.presence_period_type
(
  id						bigserial NOT NULL,
  structure_id			CHARACTER VARYING(36) NOT NULL,
  label					CHARACTER VARYING,

  CONSTRAINT presence_period_type_pk PRIMARY KEY (id)
);

CREATE TABLE presences.presence_period
(
  id					bigserial NOT NULL,
  structure_id		CHARACTER VARYING(36) NOT NULL,
  start_timestamp		timestamp without time zone,
  end_timestamp		timestamp without time zone,
  type_id			BIGINT NOT NULL,
  CONSTRAINT presence_period_pk PRIMARY KEY (id),
  CONSTRAINT fk_type_id
  FOREIGN KEY (type_id)
  REFERENCES presences.presence_period_type(id)
);

CREATE TABLE presences.punishment_type
(
  id						bigserial NOT NULL,
  structure_id			CHARACTER VARYING(36) NOT NULL,
  label					CHARACTER VARYING,
  require_presence_period		BOOLEAN DEFAULT FALSE,
  CONSTRAINT punishment_type_pk PRIMARY KEY (id)
);

CREATE TABLE presences.punishment
(
  id					bigserial NOT NULL,
  owner_id				CHARACTER VARYING(36) NOT NULL,
  structure_id			CHARACTER VARYING(36) NOT NULL,
  timestamp_at			timestamp without time zone,
  hour_is_selected		BOOLEAN DEFAULT FALSE,

  student_id			CHARACTER VARYING(36) NOT NULL,
  responsable_id		CHARACTER VARYING(36) NOT NULL,
  type_id	BIGINT NOT NULL,

  incident_id			BIGINT,
  presence_period_id	BIGINT,

  description			CHARACTER VARYING,
  is_executed				BOOLEAN DEFAULT FALSE,

  created          TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
  modified         TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),

  CONSTRAINT punishment_pk PRIMARY KEY (id),
  CONSTRAINT fk_punishment_type_id
  FOREIGN KEY (type_id)
  REFERENCES presences.punishment_type(id),
  CONSTRAINT fk_incident_id
  FOREIGN KEY (incident_id)
  REFERENCES presences.incident(id),
  CONSTRAINT fk_presence_period_id
  FOREIGN KEY (presence_period_id)
  REFERENCES presences.presence_period(id)
);
