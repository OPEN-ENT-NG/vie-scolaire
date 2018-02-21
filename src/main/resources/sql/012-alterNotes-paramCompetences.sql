ALTER TABLE notes.competences
  ADD COLUMN id_etablissement CHARACTER VARYING(36);

ALTER TABLE notes.rel_competences_enseignements
  ADD CONSTRAINT uq_rel_competences_enseignements UNIQUE (id_competence, id_enseignement),
  DROP CONSTRAINT fk_competence_id,
  ADD CONSTRAINT fk_id_competence FOREIGN KEY (id_competence)
REFERENCES notes.competences (id) MATCH SIMPLE
ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE notes.rel_competences_domaines
  ADD CONSTRAINT uq_rel_competences_domaines UNIQUE (id_competence, id_domaine),
  DROP CONSTRAINT fk_competence_id,
  ADD CONSTRAINT fk_id_competence FOREIGN KEY (id_competence)
REFERENCES notes.competences (id) MATCH SIMPLE
ON UPDATE CASCADE ON DELETE CASCADE;

CREATE TABLE notes.perso_competences
(
  id_competence     INTEGER NOT NULL,
  id_etablissement  CHARACTER VARYING(36) NOT NULL,
  nom               TEXT,
  masque            BOOLEAN DEFAULT FALSE ,
  CONSTRAINT perso_competences_pk PRIMARY KEY (id_competence, id_etablissement),
  CONSTRAINT fk_id_competence FOREIGN KEY (id_competence)
  REFERENCES notes.competences (id) MATCH SIMPLE
  ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE OR REPLACE FUNCTION notes.deleteCompetence(idCompetence IN BIGINT, idEtablissement IN VARCHAR)
  RETURNS VARCHAR AS $$
DECLARE
  nbDevoir   INTEGER;
  isLast     BOOLEAN;
  isManuelle BOOLEAN;

BEGIN
  SELECT id_etablissement IS NOT NULL
  INTO isManuelle
  FROM notes.competences
  WHERE id = idCompetence;

  IF isManuelle
  THEN
    WITH isLastQuery AS
    (SELECT count(compDom1.id_competence) = 1 AS isLastOfDom
     FROM (SELECT *
           FROM notes.rel_competences_domaines
             RIGHT JOIN notes.competences
               ON id_competence = id
           WHERE competences.id_etablissement IS NULL OR competences.id_etablissement = idEtablissement) AS compDom1
       INNER JOIN
       (SELECT *
        FROM notes.rel_competences_domaines
        WHERE id_competence = idCompetence) AS compDom2
         ON compDom1.id_domaine = compDom2.id_domaine
     GROUP BY compDom1.id_domaine)

    SELECT bool_or(isLastOfDom)
    INTO isLast
    FROM isLastQuery;

    IF isLast
    THEN
      RAISE NOTICE 'DERNIERE';
      RETURN 'SUPP_KO_LAST';
    ELSE
      SELECT count(*)
      INTO nbDevoir
      FROM notes.competences_devoirs
      WHERE id_competence = idCompetence;

      IF (nbDevoir > 0)
      THEN
        RAISE NOTICE 'MASQUAGE';
        INSERT INTO notes.perso_competences (id_competence, id_etablissement, masque)
        VALUES (idCompetence, idEtablissement, TRUE)
        ON CONFLICT ON CONSTRAINT perso_competences_pk
          DO UPDATE
            SET masque = TRUE;
            RETURN 'MASQUAGE';

      ELSE
        RAISE NOTICE 'SUPPRESSION';
        DELETE FROM notes.competences
        WHERE id = idCompetence;
        RETURN 'SUPP_OK';
      END IF;

    END IF;
  ELSE
    RAISE NOTICE 'SUPPRPERSO';
    DELETE FROM notes.perso_competences WHERE id_competence = idCompetence AND id_etablissement = idEtablissement;
    RETURN 'SUPP_PERSO_OK';
  END IF;
END;
$$
LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION notes.updateDomaineCompetence(idCompetence IN BIGINT, idEtablissement IN VARCHAR, idDomaine IN BIGINT)
  RETURNS BOOLEAN AS $$
DECLARE
  isLast     BOOLEAN;
  isManuelle BOOLEAN;
  domExist  BOOLEAN;

BEGIN

  SELECT id_etablissement IS NOT NULL
  INTO isManuelle
  FROM notes.competences
  WHERE id = idCompetence;

  IF isManuelle
  THEN

    SELECT count(id_competence) = 1 INTO isLast
    FROM  notes.rel_competences_domaines AS compDom
      RIGHT JOIN notes.competences AS comp
        ON compDom.id_competence = comp.id
    WHERE (comp.id_etablissement IS NULL OR comp.id_etablissement = idEtablissement) AND compDom.id_domaine = idDomaine
    GROUP BY compDom.id_domaine;

    IF isLast
    THEN

      RAISE NOTICE 'DERNIERE';
      RETURN FALSE;

    ELSE
      RAISE NOTICE 'UPDATEDOM';

      WITH domExistQuery AS (SELECT id_domaine
                             FROM notes.rel_competences_domaines AS compDom
                               LEFT JOIN notes.competences AS comp ON comp.id = compDom.id_competence AND
                                                                      comp.id_etablissement = idEtablissement
                             WHERE comp.id = idCompetence AND compDom.id_domaine = idDomaine)

      SELECT EXISTS(SELECT 1 FROM domExistQuery) INTO domExist;

      IF domExist
      THEN

        RAISE NOTICE 'SUPPRDOM';
        DELETE FROM notes.rel_competences_domaines WHERE id_competence = idCompetence AND id_domaine = idDomaine;

      ELSE

        RAISE NOTICE 'ADDDOM';
        INSERT INTO notes.rel_competences_domaines (id_competence, id_domaine) VALUES (idCompetence, idDomaine);

      END IF;
      RETURN TRUE;
    END IF;

  ELSE

    RAISE NOTICE 'NOTMANUELLE';
    RETURN FALSE;

  END IF;

END;
$$
LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION notes.masqueCompetence(idCompetence IN BIGINT, idEtablissement IN VARCHAR, valMasque IN BOOLEAN)
  RETURNS VARCHAR AS $$
DECLARE
  isLast     BOOLEAN;
  nbDevoir   INTEGER;

BEGIN

  WITH isLastQuery AS
  (SELECT count(compDom1.id_competence) = 1 AS isLastOfDom
   FROM (SELECT *
         FROM notes.rel_competences_domaines
           RIGHT JOIN notes.competences
             ON id_competence = id
         WHERE competences.id_etablissement IS NULL OR competences.id_etablissement = idEtablissement) AS compDom1
     INNER JOIN
     (SELECT *
      FROM notes.rel_competences_domaines
      WHERE id_competence = idCompetence) AS compDom2
       ON compDom1.id_domaine = compDom2.id_domaine
   GROUP BY compDom1.id_domaine)

  SELECT bool_or(isLastOfDom)
  INTO isLast
  FROM isLastQuery;

  IF isLast
  THEN

    RAISE NOTICE 'DERNIERE';
    RETURN 'LAST';

  ELSE
    RAISE NOTICE 'ADDMASK';

    INSERT INTO notes.perso_competences (id_competence, id_etablissement, masque)
    VALUES (idCompetence, idEtablissement, TRUE)
    ON CONFLICT ON CONSTRAINT perso_competences_pk
      DO UPDATE
        SET masque = valMasque;
    SELECT count(*)
    INTO nbDevoir
    FROM notes.competences_devoirs
    WHERE id_competence = idCompetence;

    IF (nbDevoir > 0)
      THEN
      RETURN 'use';
      ELSE
      RETURN 'notUse';
    END IF;
  END IF;

END;
$$
LANGUAGE PLPGSQL;