CREATE TABLE presences.sanction_type
(
	id						bigserial NOT NULL,
	structure_id			CHARACTER VARYING(36) NOT NULL,
	label					CHARACTER VARYING,
	require_period_absence	BOOLEAN DEFAULT FALSE,
	CONSTRAINT sanction_type_pk PRIMARY KEY (id)
);

CREATE TABLE presences.sanction
(
  id					bigserial NOT NULL,
  owner_id				CHARACTER VARYING(36) NOT NULL,
  structure_id			CHARACTER VARYING(36) NOT NULL,
  timestamp_at			timestamp without time zone,
  hour_is_selected		BOOLEAN DEFAULT FALSE,

  student_id			CHARACTER VARYING(36) NOT NULL,
  responsable_id		CHARACTER VARYING(36) NOT NULL,
  sanction_type_id				BIGINT NOT NULL,

  incident_id			BIGINT,
  period_absence_id		BIGINT,

  description			CHARACTER VARYING,
  is_done				BOOLEAN DEFAULT FALSE,

  created          TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
  modified         TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),

  CONSTRAINT sanction_pk PRIMARY KEY (id),
  CONSTRAINT fk_sanction_type_id
        FOREIGN KEY (sanction_type_id)
        REFERENCES presences.sanction_type(id),
	CONSTRAINT fk_incident_id
        FOREIGN KEY (incident_id)
        REFERENCES presences.incident(id),
	CONSTRAINT fk_period_absence_id
        FOREIGN KEY (period_absence_id)
        REFERENCES presences.periodes_absence(id)
);

ALTER TABLE presences.periodes_absence
ADD COLUMN is_sanction BOOLEAN DEFAULT FALSE;

ALTER TABLE presences.motif
ADD COLUMN is_sanction BOOLEAN DEFAULT FALSE;