DROP TABLE presences.mailing_event;
DROP TABLE presences.mailing;

CREATE TABLE presences.mailing
(
  id					bigserial NOT NULL,
  owner_id				CHARACTER VARYING(36) NOT NULL,
  structure_id			CHARACTER VARYING(36) NOT NULL,
  
  student_id			CHARACTER VARYING(36) NOT NULL,
  recipient_id			CHARACTER VARYING(36) NOT NULL,
  element_type_id				BIGINT NOT NULL,
  type_id				BIGINT NOT NULL,

  punishment_id			BIGINT,
  sanction_id			BIGINT,
  
  file_id			CHARACTER VARYING(36),
  content_text		CHARACTER VARYING,
	
  created          TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
  modified         TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
  
  CONSTRAINT mailing_pk PRIMARY KEY (id),
  CONSTRAINT fk_mailing_type_id
        FOREIGN KEY (type_id)
        REFERENCES presences.mailing_type(id),
	CONSTRAINT fk_punishment_id
        FOREIGN KEY (punishment_id)
        REFERENCES presences.punishment(id) ON DELETE SET NULL,
	CONSTRAINT fk_sanction_id
        FOREIGN KEY (sanction_id)
        REFERENCES presences.sanction(id) ON DELETE SET NULL
);

CREATE TABLE presences.mailing_event
(
	mailing_id		BIGINT NOT NULL,
	event_id			BIGINT NOT NULL,
	CONSTRAINT fk_mailing_id
        FOREIGN KEY (mailing_id)
        REFERENCES presences.mailing(id),	
	CONSTRAINT fk_event_id
        FOREIGN KEY (event_id)
        REFERENCES presences.evenement(id) ON DELETE CASCADE
);

CREATE TABLE presences.mailing_absence_period
(
	mailing_id		BIGINT NOT NULL,
	absence_period_id			BIGINT NOT NULL,
	CONSTRAINT fk_mailing_id
        FOREIGN KEY (mailing_id)
        REFERENCES presences.mailing(id),
	CONSTRAINT fk_absence_period_id
        FOREIGN KEY (absence_period_id)
        REFERENCES presences.periodes_absence(id) ON DELETE CASCADE
);