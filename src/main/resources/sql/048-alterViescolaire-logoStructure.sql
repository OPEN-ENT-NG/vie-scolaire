BEGIN;
CREATE TABLE viesco.logo_etablissement
(
	id_etablissement character varying(36),
	path character varying(255) NOT NULL,
	CONSTRAINT pk_logo_structure PRIMARY KEY (id_etablissement)
	);

CREATE TABLE viesco.nom_et_signature_CE
(
	id_etablissement character varying(36),
	path character varying(255),
	name character varying(255),
	CONSTRAINT pk_nom_et_signature_CE PRIMARY KEY (id_etablissement)
	);
END;