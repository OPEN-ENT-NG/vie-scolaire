CREATE TABLE presences.incident_type
(
  id               	bigserial NOT NULL,
  id_etablissement  CHARACTER VARYING(36) NOT NULL,
  libelle         	CHARACTER VARYING,
  CONSTRAINT incident_type_pk PRIMARY KEY (id)
);

CREATE TABLE presences.incident_gravite
(
  id_etablissement          CHARACTER VARYING(36) NOT NULL,
  niveau            BIGINT NOT NULL,
  libelle         	CHARACTER VARYING,
  CONSTRAINT incident_gravite_pk PRIMARY KEY (niveau, id_etablissement)
);

CREATE TABLE presences.incident_lieu
(
  id               	bigserial NOT NULL,
    id_etablissement          CHARACTER VARYING(36) NOT NULL,
  libelle         	CHARACTER VARYING,
  CONSTRAINT incident_lieu_pk PRIMARY KEY (id)
);

CREATE TABLE presences.incident_partenaire
(
  id               	bigserial NOT NULL,
    id_etablissement          CHARACTER VARYING(36) NOT NULL,
  libelle         	CHARACTER VARYING,
  CONSTRAINT incident_partenaire_pk PRIMARY KEY (id)
);

CREATE TABLE presences.incident_protagoniste_type
(
  id               	bigserial NOT NULL,
    id_etablissement          CHARACTER VARYING(36) NOT NULL,
  libelle         	CHARACTER VARYING,
  CONSTRAINT incident_protagoniste_type_pk PRIMARY KEY (id)
);

CREATE TABLE presences.incident
(
  id               	bigserial NOT NULL,
  id_owner          CHARACTER VARYING(36) NOT NULL,
  id_etablissement          CHARACTER VARYING(36) NOT NULL,
  timestamp_at     		timestamp without time zone,
  hour_is_selected		BOOLEAN DEFAULT FALSE,

  id_type 	BIGINT NOT NULL,
  id_lieu         	BIGINT NOT NULL,
  id_partenaire     BIGINT,

  niveau_gravite   	BIGINT NOT NULL,
  description      	CHARACTER VARYING,

  created          TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
  modified         TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
  is_traited        	BOOLEAN DEFAULT FALSE ,
  CONSTRAINT incident_pk PRIMARY KEY (id),
  CONSTRAINT fk_incident_type_id
        FOREIGN KEY (id_type)
        REFERENCES presences.incident_type(id),
	CONSTRAINT fk_incident_gravite_niveau
        FOREIGN KEY (niveau_gravite, id_etablissement)
        REFERENCES presences.incident_gravite(niveau, id_etablissement),
	CONSTRAINT fk_incident_lieu_id
        FOREIGN KEY (id_lieu)
        REFERENCES presences.incident_lieu(id),
	CONSTRAINT fk_incident_partenaire_id
        FOREIGN KEY (id_partenaire)
        REFERENCES presences.incident_partenaire(id)
);

CREATE TABLE presences.incident_protagoniste
(
  id_user               	CHARACTER VARYING(36) NOT NULL,
  id_incident         		BIGINT NOT NULL,
  id_type_protagoniste    	BIGINT NOT NULL,
  CONSTRAINT incident_protagoniste_pk PRIMARY KEY (id_user, id_incident),
  CONSTRAINT fk_incident_id
        FOREIGN KEY (id_incident)
        REFERENCES presences.incident(id),
  CONSTRAINT fk_incident_protagoniste_type_id
        FOREIGN KEY (id_type_protagoniste)
        REFERENCES presences.incident_protagoniste_type(id)
);
